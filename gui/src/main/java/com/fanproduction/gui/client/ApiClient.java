package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Function;

/**
 * HTTP-клиент для REST API.
 * <p>
 * Все 8 публичных методов ({@code get/post/put/delete} × {@code Class/TypeReference})
 * — однострочные фасады над {@link #execute(HttpRequest, Function)}.
 * <p>
 * Единая обработка ошибок:
 * <ul>
 *   <li>Сеть: {@link ConnectException}, {@link SocketTimeoutException},
 *       {@link IOException} → {@link RuntimeException} с дружелюбным текстом.</li>
 *   <li>Прерывание: восстанавливаем флаг через {@link Thread#interrupt()}.</li>
 *   <li>HTTP-статус вне 2xx → извлекаем {@code message} из JSON.</li>
 * </ul>
 * <p>
 * {@code throws Exception} сохранён для обратной совместимости с вызывающими
 * (они ловят широкий {@code Exception} и это нормально). Предупреждения
 * {@code RedundantThrows} подавлены — checked-исключения не выходят наружу,
 * но менять контракт всех вызывающих сейчас нецелесообразно (перенесено
 * в US13.3).
 */
@Slf4j
@SuppressWarnings("RedundantThrows")
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8083/api";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Getter
    @Setter
    private static String authToken;

    private ApiClient() {
        // Утилитный класс
    }

    public static void clearAuthToken() {
        authToken = null;
    }

    // ==========================================================
    // PUBLIC API — все 8 методов
    // ==========================================================

    public static <T> T get(String path, Class<T> responseClass) throws Exception {
        return execute(buildGet(path), body -> deserialize(body, responseClass));
    }

    public static <T> T get(String path, TypeReference<T> typeReference) throws Exception {
        return execute(buildGet(path), body -> deserialize(body, typeReference));
    }

    public static <T> T post(String path, Object body, Class<T> responseClass) throws Exception {
        return execute(buildPost(path, body), respBody -> deserialize(respBody, responseClass));
    }

    public static <T> T post(String path, Object body, TypeReference<T> typeReference) throws Exception {
        return execute(buildPost(path, body), respBody -> deserialize(respBody, typeReference));
    }

    public static <T> T put(String path, Object body, Class<T> responseClass) throws Exception {
        return execute(buildPut(path, body), respBody -> deserialize(respBody, responseClass));
    }

    public static <T> T put(String path, Object body, TypeReference<T> typeReference) throws Exception {
        return execute(buildPut(path, body), respBody -> deserialize(respBody, typeReference));
    }

    public static <T> T delete(String path, TypeReference<T> typeReference) throws Exception {
        return execute(buildDelete(path), respBody -> {
            if (respBody == null || respBody.isBlank()) {
                return null;
            }
            return deserialize(respBody, typeReference);
        });
    }

    public static void delete(String path) throws Exception {
        execute(buildDelete(path), body -> null);
    }

    public static void deleteWithCheck(String path) throws Exception {
        TypeReference<ApiResponse<Void>> typeRef = new TypeReference<>() {};
        ApiResponse<Void> response = delete(path, typeRef);
        if (response == null) {
            throw new RuntimeException("Ответ сервера пуст");
        }
        if (!response.isSuccess()) {
            throw new RuntimeException(response.getMessage());
        }
    }

    // ==========================================================
    // BUILDERS
    // ==========================================================

    private static HttpRequest buildGet(String path) {
        return requestBuilder(path).GET().build();
    }

    private static HttpRequest buildPost(String path, Object body) {
        String bodyJson = serializeBody(body);
        log.trace("POST {} body={}", path, bodyJson);
        return requestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
    }

    private static HttpRequest buildPut(String path, Object body) {
        String bodyJson = serializeBody(body);
        log.trace("PUT {} body={}", path, bodyJson);
        return requestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();
    }

    private static HttpRequest buildDelete(String path) {
        return requestBuilder(path).DELETE().build();
    }

    private static HttpRequest.Builder requestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");

        if (authToken != null && !authToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        return builder;
    }

    // ==========================================================
    // EXECUTION
    // ==========================================================

    /**
     * Единая точка обработки всех HTTP-вызовов.
     * <p>
     * Ловит checked-исключения из {@link HttpClient#send} и переводит их
     * в {@link RuntimeException} с осмысленными сообщениями.
     */
    private static <T> T execute(HttpRequest request, Function<String, T> deserializer) {
        try {
            log.debug("{} {}", request.method(), request.uri());

            HttpResponse<String> response = HTTP_CLIENT.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (isSuccess(response.statusCode())) {
                return deserializer.apply(response.body());
            }
            throw new RuntimeException(
                    extractErrorMessage(response.body(), response.statusCode()));

        } catch (ConnectException e) {
            throw new RuntimeException(
                    "Нет соединения с сервером. Проверьте, запущен ли API.", e);
        } catch (SocketTimeoutException e) {
            throw new RuntimeException(
                    "Сервер не отвечает. Превышено время ожидания.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Запрос прерван", e);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка соединения: " + e.getMessage(), e);
        }
    }

    private static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    // ==========================================================
    // SERIALIZATION
    // ==========================================================

    private static String serializeBody(Object body) {
        if (body == null) return "{}";
        try {
            return OBJECT_MAPPER.writeValueAsString(body);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Ошибка сериализации запроса: " + e.getMessage(), e);
        }
    }

    private static <T> T deserialize(String body, Class<T> responseClass) {
        try {
            return OBJECT_MAPPER.readValue(body, responseClass);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Ошибка разбора ответа сервера: " + e.getMessage(), e);
        }
    }

    private static <T> T deserialize(String body, TypeReference<T> typeReference) {
        try {
            return OBJECT_MAPPER.readValue(body, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(
                    "Ошибка разбора ответа сервера: " + e.getMessage(), e);
        }
    }

    // ==========================================================
    // ERROR EXTRACTION
    // ==========================================================

    private static String extractErrorMessage(String responseBody, int statusCode) {
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode node = OBJECT_MAPPER.readTree(responseBody);

                if (node.has("message")) {
                    String message = node.get("message").asText();
                    if (message != null && !message.isBlank() && !"Forbidden".equals(message)) {
                        return message;
                    }
                }
                if (node.has("error")) {
                    String error = node.get("error").asText();
                    if (error != null && !error.isBlank()) {
                        return error;
                    }
                }
            } catch (Exception ignored) {
                // Не JSON — фоллбэк ниже
            }
        }
        return "Ошибка сервера (HTTP " + statusCode + ")";
    }
}
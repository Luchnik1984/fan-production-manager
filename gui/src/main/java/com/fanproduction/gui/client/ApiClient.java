package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8083/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Getter
    @Setter
    private static String authToken;

    public static void clearAuthToken() {
        authToken = null;
    }

    private static HttpRequest.Builder createRequestBuilder(String path) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json");

        if (authToken != null && !authToken.isEmpty()) {
            builder.header("Authorization", "Bearer " + authToken);
        }

        return builder;
    }

    public static <T> T get(String path, Class<T> responseClass) throws Exception {
        HttpRequest request = createRequestBuilder(path)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseClass);
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    public static <T> T get(String path, TypeReference<T> typeReference) throws Exception {
        try {
            HttpRequest request = createRequestBuilder(path).GET().build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return objectMapper.readValue(response.body(), typeReference);
            } else {
                throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
            }
        } catch (ConnectException e) {
            throw new RuntimeException("Нет соединения с сервером. Проверьте, запущен ли API.", e);
        } catch (SocketTimeoutException e) {
            throw new RuntimeException("Сервер не отвечает. Превышено время ожидания.", e);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка соединения: " + e.getMessage(), e);
        }
    }

    public static <T> T post(String path, Object body, Class<T> responseClass) throws Exception {
        String bodyJson = objectMapper.writeValueAsString(body);
        System.out.println("POST " + BASE_URL + path);
        System.out.println("Body: " + bodyJson);

        HttpRequest request = createRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseClass);
        } else {
            String errorMessage = response.body();
            System.out.println("ERROR: " + errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    public static <T> T post(String path, Object body, TypeReference<T> typeReference) throws Exception {
        String bodyJson = objectMapper.writeValueAsString(body);

        HttpRequest request = createRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), typeReference);
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    public static <T> T put(String path, Object body, Class<T> responseClass) throws Exception {
        String bodyJson = objectMapper.writeValueAsString(body);
        System.out.println("=== PUT " + BASE_URL + path);
        System.out.println("Body: " + bodyJson);

        HttpRequest request = createRequestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response: " + response.body());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseClass);
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    public static <T> T put(String path, Object body, TypeReference<T> typeReference) throws Exception {
        String bodyJson = objectMapper.writeValueAsString(body);

        HttpRequest request = createRequestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), typeReference);
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    private static String extractErrorMessage(String responseBody) {
        try {
            JsonNode node = objectMapper.readTree(responseBody);
            // Сначала ищем поле "message" (оно содержит понятный текст)
            if (node.has("message")) {
                String message = node.get("message").asText();
                if (message != null && !message.isEmpty() && !"Forbidden".equals(message)) {
                    return message;
                }
            }
            // Если нет message или оно "Forbidden", ищем в trace
            if (node.has("trace")) {
                String trace = node.get("trace").asText();
                if (trace.contains("Account is not active")) {
                    return "Account is not active. Status: PENDING";
                }
                if (trace.contains("User not found")) {
                    return "User not found";
                }
            }
            // Если ничего не нашли, возвращаем error
            if (node.has("error")) {
                return node.get("error").asText();
            }
            return responseBody;
        } catch (Exception e) {
            return responseBody;
        }
    }

    /**
     * DELETE запрос
     */
    public static <T> T delete(String path, TypeReference<T> typeReference) throws Exception {
        HttpRequest request = createRequestBuilder(path)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (typeReference != null) {
                return objectMapper.readValue(response.body(), typeReference);
            }
            return null;
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    /**
     * DELETE запрос без возвращаемого типа
     */
    public static void delete(String path) throws Exception {
        HttpRequest request = createRequestBuilder(path)
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
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
}
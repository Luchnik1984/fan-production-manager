package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

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

    private static String authToken;

    public static void setAuthToken(String token) {
        authToken = token;
    }

    public static String getAuthToken() {
        return authToken;
    }

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
        HttpRequest request = createRequestBuilder(path)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), typeReference);
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
        }
    }

    public static <T> T post(String path, Object body, Class<T> responseClass) throws Exception {
        String bodyJson = objectMapper.writeValueAsString(body);

        HttpRequest request = createRequestBuilder(path)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return objectMapper.readValue(response.body(), responseClass);
        } else {
            throw new RuntimeException("API error: " + response.statusCode() + " - " + response.body());
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

        HttpRequest request = createRequestBuilder(path)
                .PUT(HttpRequest.BodyPublishers.ofString(bodyJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

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
}
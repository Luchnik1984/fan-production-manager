package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.ProductCardDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Клиент для работы с API карточек продукции.
 */
public class ProductCardClient {

    private static final String BASE_PATH = "/products";

    /**
     * Получить все карточки с пагинацией
     */
    public static ApiResponse<Map<String, Object>> getAllCards(int page, int size, String cardType) throws Exception {
        StringBuilder url = new StringBuilder(BASE_PATH + "?page=" + page + "&size=" + size);
        if (cardType != null && !cardType.isEmpty()) {
            url.append("&cardType=").append(cardType);
        }

        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        return ApiClient.get(url.toString(), typeRef);
    }

    /**
     * Получить карточку по ID
     */
    public static ApiResponse<ProductCardDto> getCardById(Long id) throws Exception {
        TypeReference<ApiResponse<ProductCardDto>> typeRef = new TypeReference<>() {};
        return ApiClient.get(BASE_PATH + "/" + id, typeRef);
    }

    /**
     * Создать новую карточку
     */
    public static ApiResponse<ProductCardDto> createCard(String cardType, String name, Map<String, Object> fields) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("cardType", cardType);
        request.put("name", name);
        request.put("fields", fields);

        TypeReference<ApiResponse<ProductCardDto>> typeRef = new TypeReference<>() {};
        return ApiClient.post(BASE_PATH, request, typeRef);
    }

    /**
     * Обновить карточку
     */
    public static ApiResponse<ProductCardDto> updateCard(Long id, String name, Map<String, Object> fields) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("cardType", "IGNORED");
        request.put("name", name);
        request.put("fields", fields);

        TypeReference<ApiResponse<ProductCardDto>> typeRef = new TypeReference<>() {};
        return ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
    }

    /**
     * Удалить карточку
     */
    public static ApiResponse<Void> deleteCard(Long id) throws Exception {
        TypeReference<ApiResponse<Void>> typeRef = new TypeReference<>() {};
        return ApiClient.delete(BASE_PATH + "/" + id, typeRef);
    }

    /**
     * Поиск карточек по имени
     */
    public static ApiResponse<List<ProductCardDto>> searchCards(String query) throws Exception {
        TypeReference<ApiResponse<List<ProductCardDto>>> typeRef = new TypeReference<>() {};
        return ApiClient.get(BASE_PATH + "/search?query=" + query, typeRef);
    }
}

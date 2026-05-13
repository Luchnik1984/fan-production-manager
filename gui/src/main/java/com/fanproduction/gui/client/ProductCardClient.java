package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.ArrayList;
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
     * Создать временную карточку
     */
    public static ApiResponse<ProductCardDto> createTemporaryCard(String cardType, String createdBy) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("cardType", cardType);
        request.put("name", "Временная карточка");
        request.put("isTemporary", true);
        request.put("fields", new HashMap<>());
        request.put("createdBy", createdBy);

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
     * Удалить карточку с проверкой
     */
    public static void deleteCardWithCheck(Long id) throws Exception {
        ApiResponse<Void> response = deleteCard(id);
        if (response == null || !response.isSuccess()) {
            String errorMsg = response != null ? response.getMessage() : "Неизвестная ошибка";
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * Поиск карточек по имени
     */
    public static ApiResponse<List<ProductCardDto>> searchCards(String query) throws Exception {
        TypeReference<ApiResponse<List<ProductCardDto>>> typeRef = new TypeReference<>() {};
        return ApiClient.get(BASE_PATH + "/search?query=" + query, typeRef);
    }

    /**
     * Получить все карточки определённого типа для выбора в ComboBox
     * @param cardType тип карточки (MOTOR_WHEEL, RADIAL_WHEEL, MOTOR)
     * @return список карточек
     */
    public static List<ProductCardDto> getCardsByType(String cardType) throws Exception {
        System.out.println("DEBUG: getCardsByType called with cardType = " + cardType);  // ← добавить отладку
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.get("/products?cardType=" + cardType + "&size=1000", typeRef);

        List<ProductCardDto> result = new ArrayList<>();

        if (response.isSuccess() && response.getData() != null) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getData().get("content");

            System.out.println("DEBUG: Received " + content.size() + " items for type " + cardType);

            for (Map<String, Object> item : content) {
                ProductCardDto dto = new ProductCardDto();
                dto.setId(((Number) item.get("id")).longValue());
                dto.setName((String) item.get("name"));
                dto.setCode((String) item.get("code"));
                dto.setCardType((String) item.get("cardType"));

                @SuppressWarnings("unchecked")
                Map<String, Object> fields = (Map<String, Object>) item.get("fields");
                dto.setFields(fields);

                result.add(dto);
            }
        }
        return result;
    }

}

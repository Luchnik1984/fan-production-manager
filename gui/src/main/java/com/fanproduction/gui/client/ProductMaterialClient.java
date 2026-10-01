package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductMaterialClient {

    private static final String BASE_PATH = "/materials/product";

    /**
     * Получить все материалы для карточки продукции
     */
    public static List<Map<String, Object>> getProductMaterials(Long productCardId) throws Exception {
        TypeReference<ApiResponse<List<Map<String, Object>>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<Map<String, Object>>> response = ApiClient.get(BASE_PATH + "/" + productCardId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load product materials: " + response.getMessage());
    }

    /**
     * Добавить материал к продукции
     */
    public static Map<String, Object> addMaterialToProduct(Long productCardId, Long materialId, Double quantityPerUnit, String note) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("materialId", materialId);
        request.put("quantityPerUnit", quantityPerUnit);
        if (note != null && !note.isEmpty()) {
            request.put("note", note);
        }

        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.post(BASE_PATH + "/" + productCardId, request, typeRef);

        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to add material: " + response.getMessage());
    }

    /**
     * Обновить количество материала
     */
    public static void updateMaterialQuantity(Long productCardId, Long materialId, Double quantityPerUnit) throws Exception {
        Map<String, Double> request = new HashMap<>();
        request.put("quantityPerUnit", quantityPerUnit);

        ApiClient.put(BASE_PATH + "/" + productCardId + "/" + materialId + "/quantity", request,
                new TypeReference<ApiResponse<Void>>() {});
    }

    /**
     * Удалить материал из продукции
     */
    public static void removeMaterialFromProduct(Long productCardId, Long materialId) throws Exception {
        ApiClient.delete(BASE_PATH + "/" + productCardId + "/" + materialId);
    }
}


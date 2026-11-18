package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateProductTypeRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductTypeRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductTypeDto;
import com.fanproduction.gui.dto.response.ProductTypeShortDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API типов продукции (/api/product-types).
 */
public class ProductTypeClient {

    private static final String BASE_PATH = "/product-types";

    /** Кэш краткого списка (загружается один раз). */
    private static List<ProductTypeShortDto> cachedShortList = null;

    private ProductTypeClient() {
    }

    // ==========================================================
    // READ
    // ==========================================================

    /**
     * Все типы, отсортированные по обозначению.
     */
    public static List<ProductTypeDto> getAllTypes() throws Exception {
        TypeReference<ApiResponse<List<ProductTypeDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductTypeDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить типы вентиляторов: " + response.getMessage());
    }

    /**
     * Дерево Тип → Вид → Серия.
     */
    public static List<ProductTypeDto> getTree() throws Exception {
        TypeReference<ApiResponse<List<ProductTypeDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductTypeDto>> response = ApiClient.get(BASE_PATH + "/tree", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить дерево типов: " + response.getMessage());
    }

    /**
     * Тип по ID.
     */
    public static ProductTypeDto getTypeById(Long id) throws Exception {
        TypeReference<ApiResponse<ProductTypeDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTypeDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Тип не найден: " + response.getMessage());
    }

    /**
     * Краткий список типов для ComboBox (кэшируется).
     */
    public static synchronized List<ProductTypeShortDto> getShortList() throws Exception {
        if (cachedShortList != null) {
            return cachedShortList;
        }
        TypeReference<ApiResponse<List<ProductTypeShortDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductTypeShortDto>> response = ApiClient.get(BASE_PATH + "/short-list", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            cachedShortList = response.getData();
            return cachedShortList;
        }
        throw new RuntimeException("Не удалось загрузить типы вентиляторов: " + response.getMessage());
    }

    // ==========================================================
    // CREATE / UPDATE / DELETE
    // ==========================================================

    public static ProductTypeDto createType(CreateProductTypeRequest request) throws Exception {
        TypeReference<ApiResponse<ProductTypeDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTypeDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать тип: " + response.getMessage());
    }

    public static ProductTypeDto updateType(Long id, UpdateProductTypeRequest request) throws Exception {
        TypeReference<ApiResponse<ProductTypeDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTypeDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить тип: " + response.getMessage());
    }

    public static void deleteType(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }

    // ==========================================================
    // CACHE
    // ==========================================================

    public static synchronized void clearCache() {
        cachedShortList = null;
    }
}

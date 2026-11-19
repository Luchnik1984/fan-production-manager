package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateProductSectionRequest;
import com.fanproduction.gui.dto.request.fan.MoveProductSectionRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductSectionRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductSectionDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API разделов продукции (/api/product-sections).
 */
public final class ProductSectionClient {

    private static final String BASE_PATH = "/product-sections";

    private ProductSectionClient() {
    }

    /**
     * Дерево разделов вместе с типами продукции.
     */
    public static List<ProductSectionDto> getTree() throws Exception {
        TypeReference<ApiResponse<List<ProductSectionDto>>> typeRef = new TypeReference<>() {
        };
        ApiResponse<List<ProductSectionDto>> response = ApiClient.get(BASE_PATH + "/tree", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить разделы продукции: " + response.getMessage());
    }

    /**
     * Плоский список разделов.
     */
    public static List<ProductSectionDto> getAll() throws Exception {
        TypeReference<ApiResponse<List<ProductSectionDto>>> typeRef = new TypeReference<>() {
        };
        ApiResponse<List<ProductSectionDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить разделы продукции: " + response.getMessage());
    }

    public static ProductSectionDto create(String name, Long parentId, Integer sortOrder) throws Exception {
        CreateProductSectionRequest request = new CreateProductSectionRequest(name, parentId, sortOrder);
        TypeReference<ApiResponse<ProductSectionDto>> typeRef = new TypeReference<>() {
        };
        ApiResponse<ProductSectionDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess()) {
            return response.getData();
        }
        throw new RuntimeException(response.getMessage());
    }

    public static ProductSectionDto update(Long id, String name, Integer sortOrder) throws Exception {
        UpdateProductSectionRequest request = new UpdateProductSectionRequest(name, sortOrder);
        TypeReference<ApiResponse<ProductSectionDto>> typeRef = new TypeReference<>() {
        };
        ApiResponse<ProductSectionDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess()) {
            return response.getData();
        }
        throw new RuntimeException(response.getMessage());
    }

    /**
     * Переместить раздел в другого родителя ({@code null} — в корень).
     */
    public static void move(Long id, Long newParentId) throws Exception {
        MoveProductSectionRequest request = new MoveProductSectionRequest(newParentId);
        ApiClient.put(BASE_PATH + "/" + id + "/move", request, new TypeReference<ApiResponse<Void>>() {
        });
    }

    public static void delete(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

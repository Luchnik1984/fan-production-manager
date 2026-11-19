package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateProductSpeciesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductSpeciesRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductSpeciesDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API видов продукции (/api/product-species).
 */
public class ProductSpeciesClient {

    private static final String BASE_PATH = "/product-species";

    private ProductSpeciesClient() {
    }

    public static List<ProductSpeciesDto> getSpeciesByType(Long typeId) throws Exception {
        TypeReference<ApiResponse<List<ProductSpeciesDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductSpeciesDto>> response = ApiClient.get(
                BASE_PATH + "?typeId=" + typeId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить виды: " + response.getMessage());
    }

    public static ProductSpeciesDto getSpeciesById(Long id) throws Exception {
        TypeReference<ApiResponse<ProductSpeciesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductSpeciesDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Вид не найден: " + response.getMessage());
    }

    public static ProductSpeciesDto createSpecies(CreateProductSpeciesRequest request) throws Exception {
        TypeReference<ApiResponse<ProductSpeciesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductSpeciesDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать вид: " + response.getMessage());
    }

    public static ProductSpeciesDto updateSpecies(Long id, UpdateProductSpeciesRequest request) throws Exception {
        TypeReference<ApiResponse<ProductSpeciesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductSpeciesDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить вид: " + response.getMessage());
    }

    public static void deleteSpecies(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

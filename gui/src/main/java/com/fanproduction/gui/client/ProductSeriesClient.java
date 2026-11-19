package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateProductSeriesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductSeriesRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductSeriesDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API серий продукции (/api/product-series).
 */
public class ProductSeriesClient {

    private static final String BASE_PATH = "/product-series";

    private ProductSeriesClient() {
    }

    public static List<ProductSeriesDto> getSeriesBySpecies(Long speciesId) throws Exception {
        TypeReference<ApiResponse<List<ProductSeriesDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductSeriesDto>> response = ApiClient.get(
                BASE_PATH + "?speciesId=" + speciesId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить серии: " + response.getMessage());
    }

    public static ProductSeriesDto getSeriesById(Long id) throws Exception {
        TypeReference<ApiResponse<ProductSeriesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductSeriesDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Серия не найдена: " + response.getMessage());
    }

    public static ProductSeriesDto createSeries(CreateProductSeriesRequest request) throws Exception {
        TypeReference<ApiResponse<ProductSeriesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductSeriesDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать серию: " + response.getMessage());
    }

    public static ProductSeriesDto updateSeries(Long id, UpdateProductSeriesRequest request) throws Exception {
        TypeReference<ApiResponse<ProductSeriesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductSeriesDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить серию: " + response.getMessage());
    }

    public static void deleteSeries(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

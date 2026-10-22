package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateFanSeriesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanSeriesRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FanSeriesDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API серий вентиляторов (/api/fan-series).
 */
public class FanSeriesClient {

    private static final String BASE_PATH = "/fan-series";

    private FanSeriesClient() {
    }

    public static List<FanSeriesDto> getSeriesBySpecies(Long speciesId) throws Exception {
        TypeReference<ApiResponse<List<FanSeriesDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanSeriesDto>> response = ApiClient.get(
                BASE_PATH + "?speciesId=" + speciesId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить серии: " + response.getMessage());
    }

    public static FanSeriesDto getSeriesById(Long id) throws Exception {
        TypeReference<ApiResponse<FanSeriesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanSeriesDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Серия не найдена: " + response.getMessage());
    }

    public static FanSeriesDto createSeries(CreateFanSeriesRequest request) throws Exception {
        TypeReference<ApiResponse<FanSeriesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanSeriesDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать серию: " + response.getMessage());
    }

    public static FanSeriesDto updateSeries(Long id, UpdateFanSeriesRequest request) throws Exception {
        TypeReference<ApiResponse<FanSeriesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanSeriesDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить серию: " + response.getMessage());
    }

    public static void deleteSeries(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

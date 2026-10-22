package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateFanSpeciesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanSpeciesRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FanSpeciesDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API видов вентиляторов (/api/fan-species).
 */
public class FanSpeciesClient {

    private static final String BASE_PATH = "/fan-species";

    private FanSpeciesClient() {
    }

    public static List<FanSpeciesDto> getSpeciesByType(Long typeId) throws Exception {
        TypeReference<ApiResponse<List<FanSpeciesDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanSpeciesDto>> response = ApiClient.get(
                BASE_PATH + "?typeId=" + typeId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить виды: " + response.getMessage());
    }

    public static FanSpeciesDto getSpeciesById(Long id) throws Exception {
        TypeReference<ApiResponse<FanSpeciesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanSpeciesDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Вид не найден: " + response.getMessage());
    }

    public static FanSpeciesDto createSpecies(CreateFanSpeciesRequest request) throws Exception {
        TypeReference<ApiResponse<FanSpeciesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanSpeciesDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать вид: " + response.getMessage());
    }

    public static FanSpeciesDto updateSpecies(Long id, UpdateFanSpeciesRequest request) throws Exception {
        TypeReference<ApiResponse<FanSpeciesDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanSpeciesDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить вид: " + response.getMessage());
    }

    public static void deleteSpecies(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

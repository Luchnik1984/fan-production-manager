package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CreateFanTypeRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanTypeRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FanTypeDto;
import com.fanproduction.gui.dto.response.FanTypeShortDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API типов вентиляторов (/api/fan-types).
 */
public class FanTypeClient {

    private static final String BASE_PATH = "/fan-types";

    /** Кэш краткого списка (загружается один раз). */
    private static List<FanTypeShortDto> cachedShortList = null;

    private FanTypeClient() {
    }

    // ==========================================================
    // READ
    // ==========================================================

    /**
     * Все типы, отсортированные по обозначению.
     */
    public static List<FanTypeDto> getAllTypes() throws Exception {
        TypeReference<ApiResponse<List<FanTypeDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTypeDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить типы вентиляторов: " + response.getMessage());
    }

    /**
     * Дерево Тип → Вид → Серия.
     */
    public static List<FanTypeDto> getTree() throws Exception {
        TypeReference<ApiResponse<List<FanTypeDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTypeDto>> response = ApiClient.get(BASE_PATH + "/tree", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить дерево типов: " + response.getMessage());
    }

    /**
     * Тип по ID.
     */
    public static FanTypeDto getTypeById(Long id) throws Exception {
        TypeReference<ApiResponse<FanTypeDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTypeDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Тип не найден: " + response.getMessage());
    }

    /**
     * Краткий список типов для ComboBox (кэшируется).
     */
    public static synchronized List<FanTypeShortDto> getShortList() throws Exception {
        if (cachedShortList != null) {
            return cachedShortList;
        }
        TypeReference<ApiResponse<List<FanTypeShortDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTypeShortDto>> response = ApiClient.get(BASE_PATH + "/short-list", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            cachedShortList = response.getData();
            return cachedShortList;
        }
        throw new RuntimeException("Не удалось загрузить типы вентиляторов: " + response.getMessage());
    }

    // ==========================================================
    // CREATE / UPDATE / DELETE
    // ==========================================================

    public static FanTypeDto createType(CreateFanTypeRequest request) throws Exception {
        TypeReference<ApiResponse<FanTypeDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTypeDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать тип: " + response.getMessage());
    }

    public static FanTypeDto updateType(Long id, UpdateFanTypeRequest request) throws Exception {
        TypeReference<ApiResponse<FanTypeDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTypeDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
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

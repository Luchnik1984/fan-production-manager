package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.request.CreateComponentRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;

import java.util.List;

/**
 * Клиент для работы с API компонентов.
 */
public class ComponentClient {

    private static final String BASE_PATH = "/components";

    // ========== Units of Measure ==========

    public static List<UnitOfMeasureDto> getAllUnits() throws Exception {
        TypeReference<ApiResponse<List<UnitOfMeasureDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<UnitOfMeasureDto>> response = ApiClient.get(BASE_PATH + "/units", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load units: " + response.getMessage());
    }


    // ========== Components ==========

    public static List<ComponentDto> getAllComponents() throws Exception {
        TypeReference<ApiResponse<List<ComponentDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ComponentDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load components: " + response.getMessage());
    }

    public static List<ComponentDto> getComponentsByClass(Long classId) throws Exception {
        TypeReference<ApiResponse<List<ComponentDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ComponentDto>> response = ApiClient.get(BASE_PATH + "?classId=" + classId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load components for class: " + response.getMessage());
    }

    public static ComponentDto getComponentById(Long id) throws Exception {
        TypeReference<ApiResponse<ComponentDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ComponentDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load component: " + response.getMessage());
    }

    public static ComponentDto createComponent(CreateComponentRequest request) throws Exception {
        TypeReference<ApiResponse<ComponentDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ComponentDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to create component: " + response.getMessage());
    }

    public static ComponentDto updateComponent(Long id, CreateComponentRequest request) throws Exception {
        TypeReference<ApiResponse<ComponentDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ComponentDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to update component: " + response.getMessage());
    }

    public static void deleteComponent(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }

    public static List<ComponentDto> searchComponents(String query) throws Exception {
        TypeReference<ApiResponse<List<ComponentDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ComponentDto>> response = ApiClient.get(BASE_PATH + "/search?query=" + query, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to search components: " + response.getMessage());
    }
}

package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.request.CreateComponentClassRequest;
import com.fanproduction.gui.dto.request.CreateComponentRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ComponentClassDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;

import java.util.List;
import java.util.Map;

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

    // ========== Component Classes ==========

    public static List<ComponentClassDto> getAllClasses() throws Exception {
        TypeReference<ApiResponse<List<ComponentClassDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ComponentClassDto>> response = ApiClient.get(BASE_PATH + "/classes", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load component classes: " + response.getMessage());
    }

    public static ComponentClassDto createClass(String name, String description) throws Exception {
        CreateComponentClassRequest request = new CreateComponentClassRequest(name, description);
        TypeReference<ApiResponse<ComponentClassDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ComponentClassDto> response = ApiClient.post(BASE_PATH + "/classes", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to create class: " + response.getMessage());
    }

    public static void deleteClass(Long id) throws Exception {
        ApiClient.delete(BASE_PATH + "/classes/" + id);
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
        ApiClient.delete(BASE_PATH + "/" + id);
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

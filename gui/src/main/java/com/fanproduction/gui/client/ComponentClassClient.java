package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ComponentClassDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentClassClient {

    private static final String BASE_PATH = "/components/classes";

    public static List<ComponentClassDto> getAllClasses() throws Exception {
        TypeReference<ApiResponse<List<ComponentClassDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ComponentClassDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load classes: " + response.getMessage());
    }

    public static ComponentClassDto createClass(Long categoryId, String name, String description, Long unitId) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("categoryId", categoryId);
        request.put("name", name);
        if (description != null && !description.isEmpty()) {
            request.put("description", description);
        }
        if (unitId != null) {
            request.put("unitId", unitId);
        }

        TypeReference<ApiResponse<ComponentClassDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ComponentClassDto> response = ApiClient.post("/components/classes", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to create class: " + response.getMessage());
    }

    public static void deleteClass(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

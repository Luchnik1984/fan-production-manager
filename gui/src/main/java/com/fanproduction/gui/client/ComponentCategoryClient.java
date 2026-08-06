package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ComponentCategoryDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentCategoryClient {

    private static final String BASE_PATH = "/components/categories";  // ← ИСПРАВЛЕНО

    public static List<ComponentCategoryDto> getAllCategories() throws Exception {
        TypeReference<ApiResponse<List<ComponentCategoryDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ComponentCategoryDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load categories: " + response.getMessage());
    }

    public static ComponentCategoryDto createCategory(String name, Long parentId, String description) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        if (parentId != null) {
            request.put("parentId", parentId);
        }
        if (description != null && !description.isEmpty()) {
            request.put("description", description);
        }

        TypeReference<ApiResponse<ComponentCategoryDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ComponentCategoryDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to create category: " + response.getMessage());
    }

    public static void deleteCategory(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }
}

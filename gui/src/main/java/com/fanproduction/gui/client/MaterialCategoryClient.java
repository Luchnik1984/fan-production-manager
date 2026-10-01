package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.MaterialCategoryDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaterialCategoryClient {

    private static final String BASE_PATH = "/materials/categories";

    public static List<MaterialCategoryDto> getAllCategories() throws Exception {
        TypeReference<ApiResponse<List<MaterialCategoryDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<MaterialCategoryDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load categories: " + response.getMessage());
    }

    public static List<MaterialCategoryDto> getRootCategories() throws Exception {
        TypeReference<ApiResponse<List<MaterialCategoryDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<MaterialCategoryDto>> response = ApiClient.get(BASE_PATH + "/root", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load root categories: " + response.getMessage());
    }

    public static List<MaterialCategoryDto> getChildCategories(Long parentId) throws Exception {
        TypeReference<ApiResponse<List<MaterialCategoryDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<MaterialCategoryDto>> response = ApiClient.get(BASE_PATH + "/" + parentId + "/children", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load child categories: " + response.getMessage());
    }

    public static MaterialCategoryDto createCategory(String name, Long parentId, String description) throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", name);
        if (parentId != null) {
            request.put("parentId", parentId);
        }
        if (description != null && !description.isEmpty()) {
            request.put("description", description);
        }

        TypeReference<ApiResponse<MaterialCategoryDto>> typeRef = new TypeReference<>() {};
        ApiResponse<MaterialCategoryDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to create category: " + response.getMessage());
    }

    public static void deleteCategory(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }

}

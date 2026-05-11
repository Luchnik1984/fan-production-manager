package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.request.CreateMaterialRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.MaterialDto;

import java.util.List;

public class MaterialClient {

    private static final String BASE_PATH = "/materials";

    public static List<MaterialDto> getAllMaterials() throws Exception {
        TypeReference<ApiResponse<List<MaterialDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<MaterialDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load materials: " + response.getMessage());
    }

    public static List<MaterialDto> getMaterialsByClass(Long classId) throws Exception {
        TypeReference<ApiResponse<List<MaterialDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<MaterialDto>> response = ApiClient.get(BASE_PATH + "?classId=" + classId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load materials: " + response.getMessage());
    }

    public static MaterialDto getMaterialById(Long id) throws Exception {
        TypeReference<ApiResponse<MaterialDto>> typeRef = new TypeReference<>() {};
        ApiResponse<MaterialDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to load material: " + response.getMessage());
    }

    public static MaterialDto createMaterial(CreateMaterialRequest request) throws Exception {
        TypeReference<ApiResponse<MaterialDto>> typeRef = new TypeReference<>() {};
        ApiResponse<MaterialDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to create material: " + response.getMessage());
    }

    public static MaterialDto updateMaterial(Long id, CreateMaterialRequest request) throws Exception {
        TypeReference<ApiResponse<MaterialDto>> typeRef = new TypeReference<>() {};
        ApiResponse<MaterialDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to update material: " + response.getMessage());
    }

    public static void deleteMaterial(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }

    public static List<MaterialDto> searchMaterials(String query) throws Exception {
        TypeReference<ApiResponse<List<MaterialDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<MaterialDto>> response = ApiClient.get(BASE_PATH + "/search?query=" + query, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Failed to search materials: " + response.getMessage());
    }
}
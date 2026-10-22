package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.UpdateFanTemplateVersionRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.DynamicFormMetadataDto;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Map;

/**
 * Клиент для REST API версий шаблонов (/api/fan-template-versions).
 */
public class FanTemplateVersionClient {

    private static final String BASE_PATH = "/fan-template-versions";

    private FanTemplateVersionClient() {
    }

    // ==========================================================
    // READ
    // ==========================================================

    public static FanTemplateVersionDto getVersionById(Long id) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Версия шаблона не найдена: " + response.getMessage());
    }

    public static DynamicFormMetadataDto getFormMetadata(Long templateVersionId) throws Exception {
        TypeReference<ApiResponse<DynamicFormMetadataDto>> typeRef = new TypeReference<>() {};
        ApiResponse<DynamicFormMetadataDto> response = ApiClient.get(
                BASE_PATH + "/" + templateVersionId + "/form", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить метаданные формы: " + response.getMessage());
    }

    public static List<String> validateVersion(Long versionId) throws Exception {
        TypeReference<ApiResponse<List<String>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<String>> response = ApiClient.get(
                BASE_PATH + "/" + versionId + "/validate", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        return List.of();
    }

    // ==========================================================
    // PREVIEW
    // ==========================================================

    public static String previewMarking(Long templateVersionId,
                                        Map<String, Object> values) throws Exception {
        TypeReference<ApiResponse<String>> typeRef = new TypeReference<>() {};
        Map<String, Object> request = new java.util.HashMap<>();
        request.put("values", values);
        ApiResponse<String> response = ApiClient.post(
                BASE_PATH + "/" + templateVersionId + "/preview", request, typeRef);
        if (response.isSuccess()) {
            return response.getData() != null ? response.getData() : "";
        }
        throw new RuntimeException("Preview failed: " + response.getMessage());
    }

    // ==========================================================
    // UPDATE DRAFT
    // ==========================================================

    public static FanTemplateVersionDto updateDraft(Long versionId,
                                                    UpdateFanTemplateVersionRequest request) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.put(
                BASE_PATH + "/" + versionId + "/draft", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить черновик: " + response.getMessage());
    }

    // ==========================================================
    // LIFECYCLE
    // ==========================================================

    public static FanTemplateVersionDto publishVersion(Long versionId) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.post(
                BASE_PATH + "/" + versionId + "/publish", null, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось опубликовать версию: " + response.getMessage());
    }

    public static FanTemplateVersionDto deprecateVersion(Long versionId) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.post(
                BASE_PATH + "/" + versionId + "/deprecate", null, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось деприкейтить версию: " + response.getMessage());
    }

    public static FanTemplateVersionDto archiveVersion(Long versionId) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.post(
                BASE_PATH + "/" + versionId + "/archive", null, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось архивировать версию: " + response.getMessage());
    }

    public static void deleteVersion(Long versionId) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + versionId);
    }
}

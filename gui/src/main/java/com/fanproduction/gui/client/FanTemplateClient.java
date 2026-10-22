package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CloneFanTemplateRequest;
import com.fanproduction.gui.dto.request.fan.CreateFanTemplateRequest;
import com.fanproduction.gui.dto.request.fan.CreateFanTemplateVersionRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanTemplateRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FanTemplateDto;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API шаблонов FAN (/api/fan-templates) и иерархии.
 */
public class FanTemplateClient {

    private static final String BASE_PATH = "/fan-templates";

    private FanTemplateClient() {
    }

    // ==========================================================
    // TEMPLATE — READ
    // ==========================================================

    public static List<FanTemplateDto> getAllTemplates() throws Exception {
        TypeReference<ApiResponse<List<FanTemplateDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTemplateDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить шаблоны: " + response.getMessage());
    }

    public static FanTemplateDto getTemplateById(Long id) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден: " + response.getMessage());
    }

    public static FanTemplateDto getTemplateWithVersions(Long id) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.get(
                BASE_PATH + "/" + id + "/with-versions", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден: " + response.getMessage());
    }

    public static FanTemplateDto getTemplateBySeries(Long seriesId) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.get(
                BASE_PATH + "/by-series/" + seriesId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден для серии: " + seriesId);
    }

    // ==========================================================
    // TEMPLATE — CREATE / UPDATE / DELETE / CLONE
    // ==========================================================

    public static FanTemplateDto createTemplate(CreateFanTemplateRequest request) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать шаблон: " + response.getMessage());
    }

    public static FanTemplateDto updateTemplate(Long id, UpdateFanTemplateRequest request) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить шаблон: " + response.getMessage());
    }

    public static void deleteTemplate(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }

    public static FanTemplateDto cloneTemplate(Long id, CloneFanTemplateRequest request) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.post(
                BASE_PATH + "/" + id + "/clone", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось клонировать шаблон: " + response.getMessage());
    }

    // ==========================================================
    // VERSIONS — READ
    // ==========================================================

    public static List<FanTemplateVersionDto> getVersionsByTemplate(Long templateId) throws Exception {
        TypeReference<ApiResponse<List<FanTemplateVersionDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTemplateVersionDto>> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить версии: " + response.getMessage());
    }

    public static FanTemplateVersionDto getPublishedVersion(Long templateId) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions/published", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        return null;  // нет опубликованной версии — это не ошибка
    }

    public static List<FanTemplateVersionDto> getAvailableVersions(Long templateId) throws Exception {
        TypeReference<ApiResponse<List<FanTemplateVersionDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTemplateVersionDto>> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions/available", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить версии: " + response.getMessage());
    }

    public static FanTemplateVersionDto getDraftVersion(Long templateId) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions/draft", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        return null;  // нет черновика — это не ошибка
    }

    // ==========================================================
    // VERSIONS — CREATE
    // ==========================================================

    /**
     * Создать новую DRAFT-версию.
     * <p>
     * Если {@code request == null} или его поля null — сервер скопирует
     * из текущей версии (автокопирование в FanTemplateServiceImpl).
     */
    public static FanTemplateVersionDto createNewVersion(Long templateId,
                                                         CreateFanTemplateVersionRequest request) throws Exception {
        TypeReference<ApiResponse<FanTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateVersionDto> response = ApiClient.post(
                BASE_PATH + "/" + templateId + "/versions", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать версию: " + response.getMessage());
    }
}

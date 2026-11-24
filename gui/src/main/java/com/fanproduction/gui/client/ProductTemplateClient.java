package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.fan.CloneProductTemplateRequest;
import com.fanproduction.gui.dto.request.fan.CreateProductTemplateRequest;
import com.fanproduction.gui.dto.request.fan.CreateProductTemplateVersionRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductTemplateRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductTemplateDto;
import com.fanproduction.gui.dto.response.ProductTemplateVersionDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент для REST API шаблонов FAN (/api/product-templates) и иерархии.
 */
public class ProductTemplateClient {

    private static final String BASE_PATH = "/product-templates";

    private ProductTemplateClient() {
    }

    // ==========================================================
    // TEMPLATE — READ
    // ==========================================================

    public static List<ProductTemplateDto> getAllTemplates() throws Exception {
        TypeReference<ApiResponse<List<ProductTemplateDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductTemplateDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить шаблоны: " + response.getMessage());
    }

    public static ProductTemplateDto getTemplateById(Long id) throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.get(BASE_PATH + "/" + id, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден: " + response.getMessage());
    }

    public static ProductTemplateDto getTemplateWithVersions(Long id) throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.get(
                BASE_PATH + "/" + id + "/with-versions", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден: " + response.getMessage());
    }

    public static ProductTemplateDto getTemplateBySeries(Long seriesId) throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.get(
                BASE_PATH + "/by-series/" + seriesId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден для серии: " + seriesId);
    }

    // ==========================================================
    // TEMPLATE — CREATE / UPDATE / DELETE / CLONE
    // ==========================================================

    /**
     * Экспорт шаблона: поля и правило текущей версии (US10.6).
     */
    public static com.fanproduction.gui.dto.response.TemplateExchangeDto exportTemplate(Long templateId)
            throws Exception {
        TypeReference<ApiResponse<com.fanproduction.gui.dto.response.TemplateExchangeDto>> typeRef =
                new TypeReference<>() {};
        ApiResponse<com.fanproduction.gui.dto.response.TemplateExchangeDto> response =
                ApiClient.get(BASE_PATH + "/" + templateId + "/export", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось экспортировать шаблон: " + response.getMessage());
    }

    /**
     * Импорт шаблона в указанную серию (US10.6).
     */
    public static ProductTemplateDto importTemplate(Long seriesId,
                                                    com.fanproduction.gui.dto.response.TemplateExchangeDto dto)
            throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.post(
                BASE_PATH + "/import?seriesId=" + seriesId, dto, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось импортировать шаблон: " + response.getMessage());
    }

    public static ProductTemplateDto createTemplate(CreateProductTemplateRequest request) throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать шаблон: " + response.getMessage());
    }

    public static ProductTemplateDto updateTemplate(Long id, UpdateProductTemplateRequest request) throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.put(BASE_PATH + "/" + id, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось обновить шаблон: " + response.getMessage());
    }

    public static void deleteTemplate(Long id) throws Exception {
        ApiClient.deleteWithCheck(BASE_PATH + "/" + id);
    }

    public static ProductTemplateDto cloneTemplate(Long id, CloneProductTemplateRequest request) throws Exception {
        TypeReference<ApiResponse<ProductTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateDto> response = ApiClient.post(
                BASE_PATH + "/" + id + "/clone", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось клонировать шаблон: " + response.getMessage());
    }

    // ==========================================================
    // VERSIONS — READ
    // ==========================================================

    public static List<ProductTemplateVersionDto> getVersionsByTemplate(Long templateId) throws Exception {
        TypeReference<ApiResponse<List<ProductTemplateVersionDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductTemplateVersionDto>> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить версии: " + response.getMessage());
    }

    public static ProductTemplateVersionDto getPublishedVersion(Long templateId) throws Exception {
        TypeReference<ApiResponse<ProductTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateVersionDto> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions/published", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        return null;  // нет опубликованной версии — это не ошибка
    }

    public static List<ProductTemplateVersionDto> getAvailableVersions(Long templateId) throws Exception {
        TypeReference<ApiResponse<List<ProductTemplateVersionDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<ProductTemplateVersionDto>> response = ApiClient.get(
                BASE_PATH + "/" + templateId + "/versions/available", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить версии: " + response.getMessage());
    }

    public static ProductTemplateVersionDto getDraftVersion(Long templateId) throws Exception {
        TypeReference<ApiResponse<ProductTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateVersionDto> response = ApiClient.get(
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
     * из текущей версии (автокопирование в ProductTemplateServiceImpl).
     */
    /**
     * Создать DRAFT-версию на основе выбранной версии (LEGACY/DEPRECATED/ARCHIVED).
     * <p>
     * Исходная версия не меняется — это позволяет «отредактировать старую
     * версию», не нарушая иммутабельность.
     */
    public static ProductTemplateVersionDto createVersionFrom(Long templateId,
                                                              Long sourceVersionId) throws Exception {
        TypeReference<ApiResponse<ProductTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateVersionDto> response = ApiClient.post(
                BASE_PATH + "/" + templateId + "/versions?sourceVersionId=" + sourceVersionId,
                null, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать версию: " + response.getMessage());
    }

    public static ProductTemplateVersionDto createNewVersion(Long templateId,
                                                         CreateProductTemplateVersionRequest request) throws Exception {
        TypeReference<ApiResponse<ProductTemplateVersionDto>> typeRef = new TypeReference<>() {};
        ApiResponse<ProductTemplateVersionDto> response = ApiClient.post(
                BASE_PATH + "/" + templateId + "/versions", request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось создать версию: " + response.getMessage());
    }
}

package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FanTemplateDto;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fanproduction.gui.dto.response.FanTypeDto;

import java.util.List;

/**
 * Клиент для REST API шаблонов FAN и иерархии.
 */
public class FanTemplateClient {

    private static final String BASE_TEMPLATES = "/fan-templates";
    private static final String BASE_TYPES = "/fan-types";

    // ========== ИЕРАРХИЯ ==========

    /**
     * Загрузить дерево Тип → Вид → Серия.
     */
    public static List<FanTypeDto> getTree() throws Exception {
        TypeReference<ApiResponse<List<FanTypeDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTypeDto>> response = ApiClient.get(BASE_TYPES + "/tree", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить дерево типов: " + response.getMessage());
    }

    // ========== ШАБЛОНЫ ==========

    /**
     * Получить все шаблоны (без версий).
     */
    public static List<FanTemplateDto> getAllTemplates() throws Exception {
        TypeReference<ApiResponse<List<FanTemplateDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTemplateDto>> response = ApiClient.get(BASE_TEMPLATES, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить шаблоны: " + response.getMessage());
    }

    /**
     * Получить шаблон по ID серии.
     */
    public static FanTemplateDto getTemplateBySeries(Long seriesId) throws Exception {
        TypeReference<ApiResponse<FanTemplateDto>> typeRef = new TypeReference<>() {};
        ApiResponse<FanTemplateDto> response = ApiClient.get(
                BASE_TEMPLATES + "/by-series/" + seriesId, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Шаблон не найден для серии: " + seriesId);
    }

    /**
     * Версии, доступные для создания карточек (PUBLISHED + LEGACY).
     */
    public static List<FanTemplateVersionDto> getAvailableVersions(Long templateId) throws Exception {
        TypeReference<ApiResponse<List<FanTemplateVersionDto>>> typeRef = new TypeReference<>() {};
        ApiResponse<List<FanTemplateVersionDto>> response = ApiClient.get(
                BASE_TEMPLATES + "/" + templateId + "/versions/available", typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить версии шаблона: " + response.getMessage());
    }
}

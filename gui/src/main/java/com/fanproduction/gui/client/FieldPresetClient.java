package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleDto;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FieldPresetDto;
import com.fanproduction.gui.dto.response.ProductTemplateVersionDto;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;

/**
 * Клиент REST API пресетов полей (/api/field-presets).
 */
public final class FieldPresetClient {

    private static final String BASE_PATH = "/field-presets";

    private FieldPresetClient() {
    }

    /**
     * Все пресеты (без полей — только заголовки списка).
     */
    public static List<FieldPresetDto> getAll() throws Exception {
        TypeReference<ApiResponse<List<FieldPresetDto>>> typeRef = new TypeReference<>() {
        };
        ApiResponse<List<FieldPresetDto>> response = ApiClient.get(BASE_PATH, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException("Не удалось загрузить пресеты: " + response.getMessage());
    }

    /**
     * Создать пресет из набора полей.
     */
    public static FieldPresetDto create(String name, String description,
                                        List<FieldDefinitionDto> fields,
                                        MarkingRuleDto markingRule) throws Exception {
        CreatePresetRequest request = new CreatePresetRequest(name, description, fields, markingRule);
        TypeReference<ApiResponse<FieldPresetDto>> typeRef = new TypeReference<>() {
        };
        ApiResponse<FieldPresetDto> response = ApiClient.post(BASE_PATH, request, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException(response.getMessage());
    }

    /**
     * Применить пресет к DRAFT-версии шаблона.
     * <p>
     * Сервер добавляет только недостающие поля; существующие не меняются.
     */
    public static ProductTemplateVersionDto applyToVersion(Long presetId, Long versionId,
                                                           boolean replace) throws Exception {
        TypeReference<ApiResponse<ProductTemplateVersionDto>> typeRef = new TypeReference<>() {
        };
        String path = BASE_PATH + "/" + presetId + "/apply-to-version/" + versionId
                + (replace ? "?replace=true" : "");
        ApiResponse<ProductTemplateVersionDto> response = ApiClient.post(path, null, typeRef);
        if (response.isSuccess() && response.getData() != null) {
            return response.getData();
        }
        throw new RuntimeException(response.getMessage());
    }

    /**
     * Тело запроса создания пресета.
     */
    public record CreatePresetRequest(String name, String description,
                                      List<FieldDefinitionDto> fields,
                                      MarkingRuleDto markingRule) {
    }
}

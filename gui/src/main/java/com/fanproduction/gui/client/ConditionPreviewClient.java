package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.request.preview.ConditionPreviewRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.preview.ConditionPreviewResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

/**
 * Клиент предпросмотра условий CONDITION.
 * <p>
 * Использует {@code POST /api/condition/preview}.
 */
public class ConditionPreviewClient {

    private static final String BASE_PATH = "/condition";

    private ConditionPreviewClient() {
    }

    /**
     * Вычислить условие CONDITION на тестовых значениях.
     *
     * @param condition условие (field / operator / value / values)
     * @param values    тестовые значения полей
     * @return {@code true}, если условие истинно (ветка THEN)
     * @throws RuntimeException при ошибке валидации или сети
     */
    public static boolean preview(ConditionDto condition, Map<String, Object> values) throws Exception {
        if (condition == null) {
            throw new IllegalArgumentException("Условие не может быть пустым");
        }

        Map<String, Object> safeValues = values != null ? values : Map.of();
        ConditionPreviewRequest request = new ConditionPreviewRequest(condition, safeValues);

        TypeReference<ApiResponse<ConditionPreviewResponse>> typeRef = new TypeReference<>() {};
        ApiResponse<ConditionPreviewResponse> response = ApiClient.post(
                BASE_PATH + "/preview", request, typeRef);

        if (response.isSuccess() && response.getData() != null) {
            return response.getData().matched();
        }

        throw new RuntimeException(
                response.getMessage() != null ? response.getMessage() : "Ошибка предпросмотра условия");
    }
}

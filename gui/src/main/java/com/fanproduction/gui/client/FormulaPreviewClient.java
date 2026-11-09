package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.request.preview.FormulaPreviewRequest;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.FormulaPreviewResponse;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

/**
 * Клиент предпросмотра формул DSL.
 * <p>
 * Использует {@code POST /api/formula/preview}.
 * <p>
 * Синхронный — вызывать из фонового потока (см. {@code TemplateControllerHelper.runAsync}).
 */
public class FormulaPreviewClient {

    private static final String BASE_PATH = "/formula";

    private FormulaPreviewClient() {
    }

    /**
     * Вычислить формулу DSL на тестовых значениях.
     *
     * @param formula строка формулы
     * @param values  тестовые значения полей
     * @return вычисленное значение; {@code null}, если формула вернула null
     * @throws RuntimeException при ошибке валидации формулы (400) или сети
     */
    public static String preview(String formula, Map<String, Object> values) throws Exception {
        if (formula == null || formula.isBlank()) {
            throw new IllegalArgumentException("Формула не может быть пустой");
        }

        Map<String, Object> safeValues = values != null ? values : Map.of();
        FormulaPreviewRequest request = new FormulaPreviewRequest(formula, safeValues);

        TypeReference<ApiResponse<FormulaPreviewResponse>> typeRef = new TypeReference<>() {};
        ApiResponse<FormulaPreviewResponse> response = ApiClient.post(
                BASE_PATH + "/preview", request, typeRef);

        if (response.isSuccess() && response.getData() != null) {
            return response.getData().result();
        }

        throw new RuntimeException(
                response.getMessage() != null ? response.getMessage() : "Ошибка предпросмотра формулы");
    }
}
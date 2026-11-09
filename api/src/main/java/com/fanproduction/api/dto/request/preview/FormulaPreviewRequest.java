package com.fanproduction.api.dto.request.preview;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

/**
 * Запрос на предпросмотр формулы DSL (US9.9.c).
 *
 * @param formula строка формулы (например, {@code FLOOR(DIVIDE(FIELD('wheelSize'), 10))})
 * @param values  тестовые значения полей (например, {@code {"wheelSize": 285}})
 */
public record FormulaPreviewRequest(
        @NotBlank(message = "Формула обязательна")
        String formula,

        Map<String, Object> values
) {
}

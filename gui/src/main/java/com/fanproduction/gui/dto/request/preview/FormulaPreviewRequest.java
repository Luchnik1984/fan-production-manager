package com.fanproduction.gui.dto.request.preview;

import java.util.Map;

/**
 * Запрос на предпросмотр формулы DSL.
 *
 * @param formula строка формулы (например, {@code FLOOR(DIVIDE(FIELD('wheelSize'), 10))})
 * @param values  тестовые значения полей (например, {@code {"wheelSize": 285}})
 */
public record FormulaPreviewRequest(String formula, Map<String, Object> values) {
}

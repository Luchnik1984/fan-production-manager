package com.fanproduction.api.dto.request.preview;

import com.fanproduction.template.model.Condition;

import java.util.Map;

/**
 * Запрос на предпросмотр условия CONDITION (US9.9.c).
 *
 * @param condition условие (field / operator / value / values)
 * @param values    тестовые значения полей
 */
public record ConditionPreviewRequest(
        Condition condition,
        Map<String, Object> values
) {
}

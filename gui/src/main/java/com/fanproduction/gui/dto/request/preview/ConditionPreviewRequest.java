package com.fanproduction.gui.dto.request.preview;

import com.fanproduction.gui.dto.config.ConditionDto;

import java.util.Map;

/**
 * Запрос на предпросмотр условия CONDITION.
 *
 * @param condition условие (field / operator / value / values)
 * @param values    тестовые значения полей
 */
public record ConditionPreviewRequest(ConditionDto condition, Map<String, Object> values) {
}

package com.fanproduction.template.model;

import com.fanproduction.template.enums.RuleElementType;

import java.util.List;

/**
 * Элемент правила маркировки.
 * <p>
 * Универсальная структура с nullable полями, зависящими от {@link #type}:
 * <ul>
 *   <li>{@code LITERAL}   → используется {@code value}</li>
 *   <li>{@code SEPARATOR} → используется {@code value}</li>
 *   <li>{@code FIELD}     → используется {@code fieldKey}</li>
 *   <li>{@code CONDITION} → используются {@code condition}, {@code then}, {@code else}</li>
 * </ul>
 * <p>
 * Такой подход (вместо sealed interface) выбран из-за простоты
 * сериализации/десериализации в JSONB через Jackson + Hibernate.
 */
public record MarkingRuleElement(
        RuleElementType type,
        String value,
        String fieldKey,
        Condition condition,
        List<MarkingRuleElement> then,
        List<MarkingRuleElement> otherwise
) {
}

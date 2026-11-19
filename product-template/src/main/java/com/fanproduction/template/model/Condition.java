package com.fanproduction.template.model;

import com.fanproduction.template.enums.ConditionOperator;

import java.util.List;

/**
 * Условие для visibleIf / requiredIf / CONDITION.
 * <p>
 * Пример:
 * <pre>
 * {
 *   "field": "isPartnerProduction",
 *   "operator": "EQUALS",
 *   "value": true
 * }
 * </pre>
 * <p>
 * Для оператора {@link ConditionOperator#IN} используется {@code values}.
 */
public record Condition(
        String field,
        ConditionOperator operator,
        Object value,
        List<Object> values
) {
}
package com.fanproduction.gui.dto.fan;

/**
 * Оператор условия visibleIf / requiredIf / CONDITION.
 * <p>
 * Дублирует серверный {@code com.fanproduction.template.enums.ConditionOperator}.
 */
public enum ConditionOperator {
    EQUALS,
    NOT_EQUALS,
    NOT_EMPTY,
    EMPTY,
    IN
}

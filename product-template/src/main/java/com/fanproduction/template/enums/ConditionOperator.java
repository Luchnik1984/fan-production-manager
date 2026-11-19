package com.fanproduction.template.enums;

/**
 * Оператор условия для visibleIf / requiredIf / CONDITION.
 */
public enum ConditionOperator {
    /** Равно значению. */
    EQUALS,
    /** Не равно значению. */
    NOT_EQUALS,
    /** Поле заполнено (не null и не пусто). */
    NOT_EMPTY,
    /** Поле пустое. */
    EMPTY,
    /** Значение входит в список. */
    IN
}

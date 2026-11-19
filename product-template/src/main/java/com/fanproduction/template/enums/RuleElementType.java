package com.fanproduction.template.enums;

/**
 * Тип элемента в правиле маркировки.
 */
public enum RuleElementType {
    /** Литерал (фиксированный текст). */
    LITERAL,
    /** Разделитель (символ между полями). */
    SEPARATOR,
    /** Значение поля или вычисляемого поля. */
    FIELD,
    /** Условный блок с THEN/ELSE. */
    CONDITION
}

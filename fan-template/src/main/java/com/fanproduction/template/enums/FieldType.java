package com.fanproduction.template.enums;

/**
 * Тип контрола поля в шаблоне карточки вентилятора.
 */
public enum FieldType {
    /** Текстовое поле. */
    TEXT,
    /** Целое число. */
    NUMBER,
    /** Дробное число. */
    DOUBLE,
    /** Выпадающий список (options). */
    COMBOBOX,
    /** Выбор связанной сущности (MOTOR, MOTOR_WHEEL, ...). */
    SELECTABLE,
    /** Логический флаг (галочка). */
    BOOLEAN,
    /** Разделитель-заголовок секции. */
    SEPARATOR,
    /** Скрытое поле (для служебных значений). */
    HIDDEN
}

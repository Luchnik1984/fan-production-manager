package com.fanproduction.gui.dto.fan;

/**
 * Тип поля в шаблоне карточки вентилятора.
 * <p>
 * Дублирует серверный {@code com.fanproduction.template.enums.FieldType}
 * на клиентской стороне. Имена констант совпадают — Jackson корректно
 * сериализует/десериализует в обе стороны.
 */
public enum FieldType {
    TEXT,
    NUMBER,
    DOUBLE,
    COMBOBOX,
    SELECTABLE,
    BOOLEAN,
    SEPARATOR,
    HIDDEN,
    COMPUTED
}

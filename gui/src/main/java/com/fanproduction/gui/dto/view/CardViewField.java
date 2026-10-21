package com.fanproduction.gui.dto.view;

/**
 * Одно поле в окне просмотра карточки.
 *
 * @param label отображаемое имя (например, «Мощность (КВт)»)
 * @param value отформатированное значение (например, «5,5»)
 */
public record CardViewField(String label, String value) {
}

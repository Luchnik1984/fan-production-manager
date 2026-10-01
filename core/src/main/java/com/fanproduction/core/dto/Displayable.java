package com.fanproduction.core.dto;

/**
 * Интерфейс для DTO, которые могут отображаться в UI (деревья, таблицы, ComboBox и т.д.)
 * Обеспечивает единообразный способ получения отображаемого имени.
 */
public interface Displayable {

    /**
     * Возвращает отображаемое имя для UI
     * @return имя для отображения в пользовательском интерфейсе
     */
    String getDisplayName();
}
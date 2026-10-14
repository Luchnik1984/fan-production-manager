package com.fanproduction.gui.dto;

/**
 * Опция фильтрации в ComboBox каталога.
 * <p>
 * Заменяет простую строку: содержит и отображаемое имя, и параметры для API.
 *
 * @param displayName  отображаемое имя, например «Вентилятор канальный (VRK-PatAIR)»
 * @param cardType     тип карточки для API: {@code "FAN"}, {@code "MOTOR"} и т.д.
 *                     {@code null} — «Все типы».
 * @param fanType      имя типа вентилятора для фильтрации (для FAN).
 *                     {@code null} — без фильтра по типу.
 */
public record CatalogFilterOption(
        String displayName,
        String cardType,
        String fanType
) {
    @Override
    public String toString() {
        return displayName;
    }
}

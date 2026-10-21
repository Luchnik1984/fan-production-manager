package com.fanproduction.gui.dto.view;

import java.util.List;

/**
 * Секция вкладки. Например, «Технические характеристики», «Компоненты».
 *
 * @param title       заголовок секции
 * @param fields      поля секции (без подсекций)
 * @param subsections подсекции внутри секции
 */
public record CardViewSection(
        String title,
        List<CardViewField> fields,
        List<CardViewSubsection> subsections
) {
    public static CardViewSection ofFields(String title, List<CardViewField> fields) {
        return new CardViewSection(title, fields, List.of());
    }

    public static CardViewSection ofSubsections(String title, List<CardViewSubsection> subsections) {
        return new CardViewSection(title, List.of(), subsections);
    }
}

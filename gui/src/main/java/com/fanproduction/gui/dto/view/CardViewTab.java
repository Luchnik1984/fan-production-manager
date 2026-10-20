package com.fanproduction.gui.dto.view;

import java.util.List;

/**
 * Вкладка в окне просмотра карточки.
 *
 * @param title    заголовок вкладки («Основные поля», «Компоненты», «Материалы»)
 * @param header   общие сведения (только для вкладки «Основные поля»)
 * @param sections секции вкладки
 */
public record CardViewTab(
        String title,
        List<CardViewField> header,
        List<CardViewSection> sections
) {
}
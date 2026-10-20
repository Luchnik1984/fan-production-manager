package com.fanproduction.gui.dto.view;

import java.util.List;

/**
 * Подсекция внутри секции. Например, «Электродвигатель» внутри
 * «Технических характеристик» или отдельный «Компонент» внутри
 * секции «Компоненты».
 *
 * @param title  заголовок подсекции
 * @param fields поля подсекции
 */
public record CardViewSubsection(String title, List<CardViewField> fields) {
}

package com.fanproduction.gui.dto.view;

import java.util.List;

/**
 * Итоговая разметка окна просмотра карточки.
 *
 * @param title заголовок окна («Вентилятор канальный VRK-PatAIR-...»)
 * @param tabs  вкладки
 */
public record CardViewLayout(String title, List<CardViewTab> tabs) {
}

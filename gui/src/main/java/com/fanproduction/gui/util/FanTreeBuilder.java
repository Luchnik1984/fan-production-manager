package com.fanproduction.gui.util;

import com.fanproduction.gui.dto.response.FanSeriesDto;
import com.fanproduction.gui.dto.response.FanSpeciesDto;
import com.fanproduction.gui.dto.response.FanTypeDto;
import javafx.scene.control.TreeItem;

import java.util.List;

/**
 * Утилита для построения дерева иерархии вентиляторов: Тип → Вид → Серия.
 * <p>
 * Используется в:
 * <ul>
 *   <li>{@code TemplateTreeController} — вкладка «Конструктор шаблонов»;</li>
 *   <li>{@code FanTemplateSelectionDialog} — выбор серии при создании FAN-карточки.</li>
 * </ul>
 * <p>
 * Устраняет дублирование между этими двумя местами.
 * <p>
 * Значения ячеек — DTO (`FanTypeDto`, `FanSpeciesDto`, `FanSeriesDto`).
 * Cell factory и стилизация остаются в каждом потребителе (разные требования).
 */
public final class FanTreeBuilder {

    private FanTreeBuilder() {
    }

    /**
     * Построить корневой элемент дерева иерархии.
     * <p>
     * Корневой элемент содержит дочерние элементы — по одному на каждый тип.
     * Все уровни раскрыты по умолчанию.
     *
     * @param types список типов (из {@code FanTypeClient.getTree()}), может быть null/пустым
     * @return корневой TreeItem; {@code children} пустой, если {@code types} пуст
     */
    public static TreeItem<Object> buildTree(List<FanTypeDto> types) {
        TreeItem<Object> root = new TreeItem<>();
        root.setExpanded(true);

        if (types != null) {
            for (FanTypeDto type : types) {
                root.getChildren().add(buildTypeItem(type));
            }
        }

        return root;
    }

    private static TreeItem<Object> buildTypeItem(FanTypeDto type) {
        TreeItem<Object> item = new TreeItem<>(type);
        item.setExpanded(true);

        if (type.getSpecies() != null) {
            for (FanSpeciesDto species : type.getSpecies()) {
                item.getChildren().add(buildSpeciesItem(species));
            }
        }

        return item;
    }

    private static TreeItem<Object> buildSpeciesItem(FanSpeciesDto species) {
        TreeItem<Object> item = new TreeItem<>(species);
        item.setExpanded(true);

        if (species.getSeries() != null) {
            for (FanSeriesDto series : species.getSeries()) {
                item.getChildren().add(new TreeItem<>(series));
            }
        }

        return item;
    }
}

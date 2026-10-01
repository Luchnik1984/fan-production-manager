package com.fanproduction.gui.base;

import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.scene.control.ComboBox;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CatalogHelper {

    /**
     * Построение выпадающего списка категорий с отступами
     */
    public static <T> void buildCategoryCombo(ComboBox<String> combo, Map<String, Long> idMap,
                                              List<T> allCategories,
                                              Function<T, Long> getId,
                                              Function<T, Long> getParentId,
                                              Function<T, String> getName) {
        List<T> rootCategories = allCategories.stream()
                .filter(c -> getParentId.apply(c) == null)
                .toList();

        for (T rootCat : rootCategories) {
            combo.getItems().add(getName.apply(rootCat));
            idMap.put(getName.apply(rootCat), getId.apply(rootCat));
            addChildCategoriesToCombo(combo, idMap, rootCat, 1, allCategories, getId, getParentId, getName);
        }
    }

    private static <T> void addChildCategoriesToCombo(ComboBox<String> combo, Map<String, Long> idMap,
                                                      T parent, int depth, List<T> allCategories,
                                                      Function<T, Long> getId,
                                                      Function<T, Long> getParentId,
                                                      Function<T, String> getName) {
        String indent = "  " + "  ".repeat(depth);
        List<T> children = allCategories.stream()
                .filter(c -> getParentId.apply(c) != null && getParentId.apply(c).equals(getId.apply(parent)))
                .toList();

        for (T child : children) {
            String display = indent + getName.apply(child);
            combo.getItems().add(display);
            idMap.put(display, getId.apply(child));
            addChildCategoriesToCombo(combo, idMap, child, depth + 1, allCategories, getId, getParentId, getName);
        }
    }

    /**
     * Получение всех ID дочерних категорий
     */
    public static <T> List<Long> getAllCategoryIds(Long categoryId, List<T> allCategories,
                                                   Function<T, Long> getId,
                                                   Function<T, Long> getParentId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<T> children = allCategories.stream()
                .filter(c -> getParentId.apply(c) != null && getParentId.apply(c).equals(categoryId))
                .toList();

        for (T child : children) {
            ids.addAll(getAllCategoryIds(getId.apply(child), allCategories, getId, getParentId));
        }
        return ids;
    }

    /**
     * Создание группированного ComboBox для единиц измерения
     */
    public static GroupedComboBox<UnitOfMeasureDto> createUnitCombo(List<UnitOfMeasureDto> allUnits) {
        GroupedComboBox<UnitOfMeasureDto> unitCombo = new GroupedComboBox<>();
        Map<String, List<UnitOfMeasureDto>> groupedUnits = allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
        unitCombo.setGroupedItems(groupedUnits);
        unitCombo.setPromptText("Выберите единицу измерения");
        return unitCombo;
    }
}

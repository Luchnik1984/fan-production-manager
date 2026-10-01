package com.fanproduction.gui.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Хелпер для фильтрации данных в каталогах.
 */
public class CatalogFilterHelper<C, T> {

    private final List<C> allCategories;
    private final Function<C, Long> getCategoryId;
    private final Function<C, Long> getCategoryParentId;
    private final Function<T, Long> getItemClassId;

    public CatalogFilterHelper(List<C> allCategories,
                               Function<C, Long> getCategoryId,
                               Function<C, Long> getCategoryParentId,
                               Function<T, Long> getItemClassId) {
        this.allCategories = allCategories;
        this.getCategoryId = getCategoryId;
        this.getCategoryParentId = getCategoryParentId;
        this.getItemClassId = getItemClassId;
    }

    /**
     * Получает все ID категории и всех её дочерних категорий
     */
    public List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);

        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) != null
                        && getCategoryParentId.apply(c).equals(categoryId))
                .toList();

        for (C child : children) {
            ids.addAll(getAllCategoryIds(getCategoryId.apply(child)));
        }

        return ids;
    }

    /**
     * Фильтрует элементы по ID категории
     * @param items элементы для фильтрации
     * @param categoryId ID категории
     * @param classIds список ID классов, принадлежащих категории
     * @return отфильтрованный список
     */
    public List<T> filterByCategoryId(List<T> items, Long categoryId, List<Long> classIds) {
        if (categoryId == null) {
            return new ArrayList<>(items);
        }

        if (classIds.isEmpty()) {
            return new ArrayList<>();
        }

        return items.stream()
                .filter(item -> classIds.contains(getItemClassId.apply(item)))
                .toList();
    }
}

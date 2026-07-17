package com.fanproduction.gui.util;

import javafx.scene.control.CheckBox;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Утилиты для работы с CheckBox в JavaFX.
 * Предоставляет методы для настройки взаимоисключающих групп CheckBox.
 */
public class CheckboxUtils {

    /**
     * Настраивает взаимоисключение для группы CheckBox.
     * При выборе одного, все остальные снимаются.
     *
     * @param checkboxes список CheckBox для группировки
     * @param onUpdate   колбэк при изменении (опционально)
     */
    public static void setupMutualExclusiveGroup(List<CheckBox> checkboxes, Runnable onUpdate) {
        if (checkboxes == null || checkboxes.size() < 2) {
            return;
        }

        for (CheckBox checkbox : checkboxes) {
            if (checkbox == null) {
                continue;
            }
            checkbox.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    // Снимаем все остальные
                    for (CheckBox other : checkboxes) {
                        if (other != null && other != checkbox) {
                            other.setSelected(false);
                        }
                    }
                    if (onUpdate != null) {
                        onUpdate.run();
                    }
                }
            });
        }
    }

    /**
     * Настраивает взаимоисключение для группы CheckBox (varargs вариант)
     */
    public static void setupMutualExclusiveGroup(Runnable onUpdate, CheckBox... checkboxes) {
        setupMutualExclusiveGroup(Arrays.asList(checkboxes), onUpdate);
    }

    /**
     * Настраивает взаимоисключение для двух CheckBox.
     */
    public static void setupMutualExclusive(CheckBox c1, CheckBox c2, Runnable onUpdate) {
        if (c1 == null || c2 == null) {
            return;
        }

        c1.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                c2.setSelected(false);
            }
            if (onUpdate != null) {
                onUpdate.run();
            }
        });

        c2.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                c1.setSelected(false);
            }
            if (onUpdate != null) {
                onUpdate.run();
            }
        });
    }

    /**
     * Настраивает взаимоисключение для двух CheckBox с Consumer (более гибкий вариант)
     */
    public static void setupMutualExclusive(CheckBox c1, CheckBox c2, Consumer<CheckBox> onSelect) {
        if (c1 == null || c2 == null) {
            return;
        }

        c1.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                c2.setSelected(false);
                if (onSelect != null) {
                    onSelect.accept(c1);
                }
            }
        });

        c2.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                c1.setSelected(false);
                if (onSelect != null) {
                    onSelect.accept(c2);
                }
            }
        });
    }

    /**
     * Проверяет, выбрана ли хотя бы одна галочка из группы
     */
    public static boolean isAnySelected(CheckBox... checkboxes) {
        for (CheckBox cb : checkboxes) {
            if (cb != null && cb.isSelected()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Снимает все галочки из группы
     */
    public static void clearAll(CheckBox... checkboxes) {
        for (CheckBox cb : checkboxes) {
            if (cb != null) {
                cb.setSelected(false);
            }
        }
    }

    /**
     * Возвращает выбранный CheckBox из группы (если ровно один выбран)
     */
    public static CheckBox getSelected(CheckBox... checkboxes) {
        CheckBox selected = null;
        for (CheckBox cb : checkboxes) {
            if (cb != null && cb.isSelected()) {
                if (selected != null) {
                    return null; // больше одного выбрано
                }
                selected = cb;
            }
        }
        return selected;
    }
}

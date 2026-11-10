package com.fanproduction.gui.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

/**
 * Утилиты для ComboBox.
 */
public final class ComboBoxUtils {

    private ComboBoxUtils() {
    }

    /**
     * Добавляет в ComboBox первый элемент {@code null} — «пустой выбор».
     * <p>
     * Позволяет пользователю отменить ранее сделанный выбор, вернувшись
     * к placeholder'у в раскрывающемся списке.
     * <p>
     * <b>Важно (US9.9.e.2-fix):</b>
     * <ul>
     *   <li>{@code buttonCell} <b>не переопределяется</b> — при {@code null}
     *       ComboBox сам показывает {@code promptText} (стандартное поведение JavaFX).</li>
     *   <li>Если {@code promptText} не задан — устанавливается в {@code placeholder}.</li>
     *   <li>{@code cellFactory} для раскрывающегося списка — «— не выбрано —» первым
     *       элементом (серым курсивом).</li>
     * </ul>
     *
     * @param combo       ComboBox (не {@code null})
     * @param placeholder текст для placeholder'а (например, «— не выбрано —»)
     */
    public static <T> void installClearOption(ComboBox<T> combo, String placeholder) {
        if (combo == null || placeholder == null) return;

        // 1. Добавить null первым элементом в новый ObservableList
        ObservableList<T> currentItems = combo.getItems();
        ObservableList<T> newItems = FXCollections.observableArrayList();
        newItems.add(null);
        if (currentItems != null) {
            newItems.addAll(currentItems);
        }
        combo.setItems(newItems);

        // 2. Установить promptText, если не задан
        if (combo.getPromptText() == null || combo.getPromptText().isBlank()) {
            combo.setPromptText(placeholder);
        }

        // 3. Только cellFactory для списка (НЕ buttonCell).
        //    При null в buttonCell ComboBox fallback на promptText.
        combo.setCellFactory(lv -> createCell(placeholder));
    }

    private static <T> ListCell<T> createCell(String placeholder) {
        return new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else if (item == null) {
                    setText(placeholder);
                    setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
                } else {
                    setText(item.toString());
                    setStyle("");
                }
            }
        };
    }
}

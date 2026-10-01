package com.fanproduction.gui.component;

import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;
import javafx.util.StringConverter;

/**
 * Редактируемая ячейка таблицы с всплывающей подсказкой
 * @param <S> тип данных строки таблицы
 * @param <T> тип данных в ячейке
 */
public class EditableTooltipCell<S, T> extends TableCell<S, T> {

    private final TextField textField;
    private final StringConverter<T> converter;

    public EditableTooltipCell(StringConverter<T> converter) {
        this.converter = converter;
        this.textField = new TextField();

        textField.setOnAction(e -> commitEdit(converter.fromString(textField.getText())));
        textField.focusedProperty().addListener((obs, old, newVal) -> {
            if (!newVal) {
                commitEdit(converter.fromString(textField.getText()));
            }
        });

        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setTooltip(null);
            setGraphic(null);
        } else {
            String text = converter.toString(item);
            setText(text);
            // Добавляем подсказку с полным текстом
            Tooltip tooltip = new Tooltip(text);
            tooltip.setShowDelay(Duration.millis(100));
            tooltip.setShowDuration(Duration.INDEFINITE);
            setTooltip(tooltip);
        }
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            T currentValue = getItem();
            String currentText = currentValue == null ? "" : converter.toString(currentValue);
            textField.setText(currentText);
            setText(null);
            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
            textField.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        T item = getItem();
        setText(item == null ? "" : converter.toString(item));
        setGraphic(null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }

    @Override
    public void commitEdit(T newValue) {
        if (!isEditing()) {
            return;
        }
        super.commitEdit(newValue);
        setText(converter.toString(newValue));
        setGraphic(null);
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}

package com.fanproduction.gui.configurator;


import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public interface CardFieldConfigurator {

    /**
     * Настраивает специальные поля для карточки
     * @param fieldControls карта контролов
     * @param fieldLabels карта лейблов
     * @param fieldHints карта подсказок
     * @param existingCardExists есть ли уже существующая карточка (редактирование)
     */
    void setupFields(Map<String, Control> fieldControls,
                     Map<String, Label> fieldLabels,
                     Map<String, Label> fieldHints,
                     boolean existingCardExists);

    /**
     * Безопасное получение ComboBox из карты контролов
     */
    @SuppressWarnings("unchecked")
    static ComboBox<String> getComboBox(Map<String, Control> fieldControls, String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control instanceof ComboBox) {
            return (ComboBox<String>) control;
        }
        return null;
    }

    /**
     * Безопасное получение TextField из карты контролов
     */
    static TextField getTextField(Map<String, Control> fieldControls, String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control instanceof TextField) {
            return (TextField) control;
        }
        return null;
    }

    /**
     * Безопасное получение CheckBox из карты контролов
     */
    static CheckBox getCheckBox(Map<String, Control> fieldControls, String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control instanceof CheckBox) {
            return (CheckBox) control;
        }
        return null;
    }

    /**
     * Автоматическое заполнение поля "Наименование" значением по умолчанию
     */
    default void autoFillName(Map<String, Control> fieldControls, String defaultName, boolean existingCardExists) {
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText(defaultName);
            }
        }
    }

}


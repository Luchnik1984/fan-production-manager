package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;

import java.util.Map;

public interface CardFieldConfigurator {

    void setupFields(Map<String, Node> fieldControls,
                     Map<String, Label> fieldLabels,
                     Map<String, Label> fieldHints,
                     boolean existingCardExists);

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ИСПОЛНЕНИЯМИ ==========

    /**
     * Настраивает логику исполнений (огнестойкость, взрывозащита)
     */
    default void setupExecutionMarking(Map<String, Node> fieldControls,
                                       Map<String, Label> fieldLabels,
                                       Map<String, Label> fieldHints,
                                       Runnable onUpdate) {
        ExecutionMarkingHelper helper = new ExecutionMarkingHelper(
                fieldControls, fieldLabels, fieldHints, onUpdate
        );
        helper.setup();
    }

    /**
     * Возвращает маркировку исполнения
     */
    default String getExecutionMarking(Map<String, Node> fieldControls) {
        ExecutionMarkingHelper helper = new ExecutionMarkingHelper(fieldControls);
        return helper.getExecutionMarking();
    }

    // ========== БАЗОВЫЕ МЕТОДЫ ДЛЯ ПОЛУЧЕНИЯ КОНТРОЛОВ ==========

    default TextField getTextField(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof TextField ? (TextField) control : null;
    }

    @SuppressWarnings("unchecked")
    default ComboBox<String> getComboBox(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof ComboBox ? (ComboBox<String>) control : null;
    }

    default CheckBox getCheckBox(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof CheckBox ? (CheckBox) control : null;
    }

    // ========== МЕТОДЫ ДЛЯ ПОЛУЧЕНИЯ ЗНАЧЕНИЙ ==========

    default String getFieldValue(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        if (control == null) return "";

        if (control instanceof TextField) {
            return ((TextField) control).getText().trim();
        }
        if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

    default boolean isSelected(Map<String, Node> fieldControls, String fieldName) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        return checkBox != null && checkBox.isSelected();
    }

    // ========== УПРАВЛЕНИЕ ВИДИМОСТЬЮ (С ПОДСКАЗКАМИ) ==========

    default void setVisible(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            String fieldName,
                            boolean visible) {
        Node control = fieldControls.get(fieldName);
        Label label = fieldLabels != null ? fieldLabels.get(fieldName) : null;
        Label hint = fieldHints != null ? fieldHints.get(fieldName) : null;

        if (control != null) {
            // Устанавливаем видимость у самого контрола
            control.setVisible(visible);
            control.setManaged(visible);

            // Ищем родительский контейнер (VBox) и устанавливаем видимость у него
            Parent parent = control.getParent();
            if (parent != null) {
                parent.setVisible(visible);
                parent.setManaged(visible);
            }
        }
        if (label != null) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
        if (hint != null) {
            hint.setVisible(visible);
            hint.setManaged(visible);
        }
    }

    // ========== РАСЧЁТ СКОРОСТИ ==========

    default void updateRatedSpeed(ComboBox<String> polesCombo, TextField ratedSpeedField) {
        String value = polesCombo.getValue();
        if (value != null && !value.isEmpty()) {
            try {
                int poles = Integer.parseInt(value);
                int ratedSpeed = 6000 / poles;
                ratedSpeedField.setText(String.valueOf(ratedSpeed));
            } catch (NumberFormatException e) {
                ratedSpeedField.setText("");
            }
        } else {
            ratedSpeedField.setText("");
        }
    }

    /**
     * Рассчёт номинальной скорости
     */
    default void setupRatedSpeedCalculation(Map<String, Node> fieldControls, Runnable callback) {
        ComboBox<String> polesCombo = getComboBox(fieldControls, "poles");
        TextField ratedSpeedField = getTextField(fieldControls, "ratedSpeedRpm");

        if (polesCombo != null && ratedSpeedField != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> {
                updateRatedSpeed(polesCombo, ratedSpeedField);
                if (callback != null) callback.run();
            });
            updateRatedSpeed(polesCombo, ratedSpeedField);
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    default void autoFillName(Map<String, Node> fieldControls, String defaultName, boolean existingCardExists) {
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText(defaultName);
            }
        }
    }

    default void addTextFieldListener(Map<String, Node> fieldControls, String fieldName, Runnable callback) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    default void addCheckBoxListener(Map<String, Node> fieldControls, String fieldName, Runnable callback) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        if (checkBox != null) {
            checkBox.selectedProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    default void addComboBoxListener(Map<String, Node> fieldControls, String fieldName, Runnable callback) {
        ComboBox<String> comboBox = getComboBox(fieldControls, fieldName);
        if (comboBox != null) {
            comboBox.valueProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Проверяет, изменились ли указанные поля по сравнению с начальными значениями
     */
    default boolean hasAnyFieldChanged(Map<String, Node> fieldControls,
                                       Map<String, String> initialValues,
                                       String... fieldNames) {
        for (String fieldName : fieldNames) {
            String currentValue = getFieldValue(fieldControls, fieldName);
            String initialValue = initialValues.getOrDefault(fieldName, "");
            if (currentValue == null && initialValue.isEmpty()) continue;
            if (currentValue == null || !currentValue.equals(initialValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Валидация карточки (по умолчанию всегда валидна)
     */
    default boolean validate(Map<String, Node> fieldControls,
                             Map<String, Object> fields,
                             Map<String, Label> fieldLabels) {
        return true;
    }

    // ========== МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛНОЙ МАРКИРОВКОЙ ==========

    /**
     * Обновляет полную маркировку (с защитой от перезаписи вручную отредактированных полей)
     * Обычно вызывается при изменении полей, влияющих на маркировку
     */
    default void refreshFullMarking(Map<String, Node> fieldControls) {
        // По умолчанию ничего не делаем
        // Каждый конфигуратор переопределяет этот метод
    }

    /**
     * Принудительно вычисляет и устанавливает полную маркировку.
     * Игнорирует защиту от перезаписи.
     * Используется для кнопки "Восстановить маркировку"
     */
    default void forceSetFullMarking(Map<String, Node> fieldControls) {
    }

    /**
     * Проверяет, что полная маркировка не пустая
     */
    default boolean validateFullMarking(Map<String, Object> fields) {
        String fullMarking = (String) fields.get("fullMarking");
        if (fullMarking == null || fullMarking.isEmpty()) {
            showValidationError("Полная маркировка не может быть пустой. Проверьте заполнение полей, влияющих на маркировку.");
            return false;
        }
        return true;
    }

    /**
     * Показывает сообщение об ошибке валидации
     */
    default void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
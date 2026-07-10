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


    // ========== СЛУШАТЕЛИ ==========

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

    // ========== ВЗАИМОИСКЛЮЧАЮЩИЕ ГАЛОЧКИ ==========

    default void setupExclusiveSelection(Map<String, Node> fieldControls, Runnable callback) {
        CheckBox generalPurposeCheck = getCheckBox(fieldControls, "generalPurpose");
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");

        // Настраиваем взаимное исключение для всех пар
        setupMutualExclusiveCheckboxes(generalPurposeCheck, fireproofCheck, callback);
        setupMutualExclusiveCheckboxes(generalPurposeCheck, explosionCheck, callback);
        setupMutualExclusiveCheckboxes(fireproofCheck, explosionCheck, callback);
    }


    /**
     * Настраивает взаимное исключение для двух галочек.
     * Если одна из них становится выбранной, другая снимается.
     *
     * @param checkbox1 первая галочка
     * @param checkbox2 вторая галочка
     * @param callback  действие после изменения
     */
    default void setupMutualExclusiveCheckboxes(CheckBox checkbox1, CheckBox checkbox2, Runnable callback) {
        if (checkbox1 == null || checkbox2 == null) {
            return;
        }

        // Слушатель для первой галочки
        checkbox1.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                checkbox2.setSelected(false);
            }
            if (callback != null) {
                callback.run();
            }
        });

        // Слушатель для второй галочки
        checkbox2.selectedProperty().addListener((obs, old, val) -> {
            if (val) {
                checkbox1.setSelected(false);
            }
            if (callback != null) {
                callback.run();
            }
        });
    }

    // ========== УСЛОВНАЯ ВИДИМОСТЬ (ОГНЕСТОЙКОСТЬ/ВЗРЫВОЗАЩИТА) ==========

    default void setupConditionalVisibility(Map<String, Node> fieldControls,
                                            Map<String, Label> fieldLabels,
                                            Map<String, Label> fieldHints,
                                            Runnable updateCallback) {
        // Огнестойкость
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        if (fireproofCheck != null) { String[] fireproofFields = {"fireproofMarking", "fireproofTime", "maxTemperature"};
            boolean isFireproof = fireproofCheck.isSelected();
            for (String fieldName : fireproofFields) {
                setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isFireproof);
            }

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                for (String fieldName : fireproofFields) {
                    setVisible(fieldControls, fieldLabels, fieldHints, fieldName, val);
                }
                if (updateCallback != null) updateCallback.run();
            });
        }

        // Взрывозащита
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");
        if (explosionCheck != null) {
            setVisible(fieldControls, fieldLabels, fieldHints, "explosionMarking", explosionCheck.isSelected());

            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                setVisible(fieldControls, fieldLabels, fieldHints, "explosionMarking", val);
                if (updateCallback != null) updateCallback.run();
            });
        }
    }

    // ========== РАСЧЁТ НОМИНАЛЬНОЙ СКОРОСТИ ==========

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


    /**
     * Возвращает маркировку исполнения (C, F/xxx, или Ex-маркировку)
     * на основе состояния галочек и соответствующих полей.
     */
    default String getExecutionMarking(Map<String, Node> fieldControls) {
        boolean isGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        if (isGeneralPurpose) return "C";
        if (isFireproof) {
            String fireproofMarking = getFieldValue(fieldControls, "fireproofMarking");
            return !fireproofMarking.isEmpty() ? fireproofMarking : "F/400";
        }
        if (isExplosionProof) {
            String explosionMarking = getFieldValue(fieldControls, "explosionMarking");
            return !explosionMarking.isEmpty() ? explosionMarking : "1Ex d IIC T4 Gb";
        }
        return "";
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

    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ДЛЯ ФОРМИРОВАНИЯ СТРОК

    default void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(value);
        }
    }

    default void appendWithSeparator(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append("-");
            }
            sb.append(value);
        }
    }

    /**
     * Форматирует размер: убирает .0 если число целое
     */
    default String formatSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isEmpty()) return "";
        try {
            double size = Double.parseDouble(sizeStr.replace(',', '.'));
            if (size == Math.floor(size)) {
                return String.valueOf((int) size);
            }
            return String.valueOf(size);
        } catch (NumberFormatException e) {
            return sizeStr;
        }
    }

    default String formatSize(Double size) {
        if (size == null) return "";
        if (size == Math.floor(size)) {
            return String.valueOf(size.intValue());
        }
        return String.valueOf(size);
    }


    default boolean validate(Map<String, Node> fieldControls, Map<String, Object> fields, Map<String, Label> fieldLabels) {
        return true; // По умолчанию — всегда валидно
    }

    /**
     * Проверяет, что полная маркировка не пустая
     * @return true если валидация пройдена, false если есть ошибка
     */
    default boolean validateFullMarking(Map<String, Object> fields) {
        String fullMarking = (String) fields.get("fullMarking");
        if (fullMarking == null || fullMarking.isEmpty()) {
            showValidationError("""
                    Полная маркировка не может быть пустой. Проверьте заполнение полей, влияющих на маркировку.""");
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

    /**
     * АВТОЗАПОЛНЕНИЕ НАИМЕНОВАНИЯ
     */
    default void autoFillName(Map<String, Node> fieldControls, String defaultName, boolean existingCardExists) {
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText(defaultName);
            }
        }
    }

}
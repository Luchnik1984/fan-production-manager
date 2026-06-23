package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

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

    // ========== МЕТОДЫ ДЛЯ УСТАНОВКИ ЗНАЧЕНИЙ ==========

    default void setFieldValue(Map<String, Node> fieldControls, String fieldName, String value) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null && value != null) {
            textField.setText(value);
        }
    }

    default void setFieldValue(Map<String, Node> fieldControls, String fieldName, Number value) {
        if (value != null) {
            setFieldValue(fieldControls, fieldName, value.toString());
        }
    }

    // ========== УПРАВЛЕНИЕ ВИДИМОСТЬЮ (С ПОДСКАЗКАМИ) ==========

    default void setVisible(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            String fieldName,
                            boolean visible) {
        Node control = fieldControls.get(fieldName);
        Label label = fieldLabels.get(fieldName);
        Label hint = fieldHints.get(fieldName);

        if (control != null) {
            control.setVisible(visible);
            control.setManaged(visible);
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

    // ========== ВЗАИМОИСКЛЮЧАЮЩИЕ ГАЛОЧКИ ==========

    default void setupExclusiveSelection(Map<String, Node> fieldControls, Runnable callback) {
        CheckBox generalPurposeCheck = getCheckBox(fieldControls, "generalPurpose");
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");

        if (generalPurposeCheck != null) {
            generalPurposeCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                if (callback != null) callback.run();
            });
        }

        if (fireproofCheck != null) {
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                if (callback != null) callback.run();
            });
        }

        if (explosionCheck != null) {
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                }
                if (callback != null) callback.run();
            });
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
}
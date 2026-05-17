package com.fanproduction.gui.configurator;


import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class MotorWheelCardConfigurator implements CardFieldConfigurator {

    private TextField ratedSpeedField;
    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Node> fieldControls;

    private TextField getTextField(Map<String, Node> controls, String name) {
        Node c = controls.get(name);
        return c instanceof TextField ? (TextField) c : null;
    }

    private ComboBox<String> getComboBox(Map<String, Node> controls, String name) {
        return CardFieldConfigurator.getComboBox(controls, name);
    }

    @Override
    public void setupFields(Map<String, Node> fieldControls, Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints, boolean existingCardExists) {
        this.fieldControls = fieldControls;

        ratedSpeedField = getTextField(fieldControls, "ratedSpeedRpm");
        fullMarkingField = getTextField(fieldControls, "fullMarking");

        setupVoltageAutoFill();
        setupRatedSpeedCalculation();
        setupFullMarkingGeneration();

    }

    private void setupVoltageAutoFill() {
        ComboBox<String> voltageCodeCombo = getComboBox(fieldControls, "voltageCode");
        TextField voltageField = getTextField(fieldControls, "voltage");

        if (voltageCodeCombo != null && voltageField != null) {
            voltageCodeCombo.valueProperty().addListener((obs, old, val) -> {
                if ("E".equals(val)) {
                    voltageField.setText("220");
                } else if ("D".equals(val)) {
                    voltageField.setText("380");
                } else {
                    voltageField.setText("");
                }
            });

            String initialCode = voltageCodeCombo.getValue();
            if ("E".equals(initialCode)) {
                voltageField.setText("220");
            } else if ("D".equals(initialCode)) {
                voltageField.setText("380");
            }
        }
    }

    private void setupRatedSpeedCalculation() {
        ComboBox<String> polesCombo = getComboBox(fieldControls, "poles");
        if (polesCombo != null && ratedSpeedField != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> {
                updateRatedSpeed(polesCombo);
                updateFullMarking();
            });
            updateRatedSpeed(polesCombo);
        }
    }

    private void updateRatedSpeed(ComboBox<String> polesCombo) {
        if (ratedSpeedField == null || polesCombo == null) return;

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

    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        addTextFieldListener("name", this::updateFullMarking);
        addTextFieldListener("poles", this::updateFullMarking);
        addTextFieldListener("voltageCode", this::updateFullMarking);
        addTextFieldListener("motorCode", this::updateFullMarking);

        updateFullMarking();
    }

    private void addTextFieldListener(String fieldName, Runnable callback) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void updateFullMarking() {
        if (fullMarkingField == null) return;

        String name = getFieldValue("name");
        String poles = getFieldValue("poles");
        String voltageCode = getFieldValue("voltageCode");
        String motorCode = getFieldValue("motorCode");

        StringBuilder fullMarking = new StringBuilder();

        if (name != null && !name.isEmpty()) {
            fullMarking.append(name);
        }
        if (poles != null && !poles.isEmpty()) {
            fullMarking.append("-").append(poles);
        }
        if (voltageCode != null && !voltageCode.isEmpty()) {
            fullMarking.append(voltageCode);
        }
        if (motorCode != null && !motorCode.isEmpty()) {
            fullMarking.append("-").append(motorCode);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    private String getFieldValue(String fieldName) {
        Node control = fieldControls.get(fieldName);
        if (control == null) return "";
        if (control instanceof TextField) return ((TextField) control).getText().trim();
        if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }
}

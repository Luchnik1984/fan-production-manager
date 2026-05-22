package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class MotorWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        setupVoltageAutoFill(fieldControls);

        // Используем общий метод для расчёта скорости
        setupRatedSpeedCalculation(fieldControls, () -> updateFullMarking(fieldControls));

        setupFullMarkingGeneration(fieldControls);

        autoFillName(fieldControls, "Мотор-колесо", existingCardExists);
    }

    private void setupVoltageAutoFill(Map<String, Node> fieldControls) {
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

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "name", () -> updateFullMarking(fieldControls));
        addComboBoxListener(fieldControls, "poles", () -> updateFullMarking(fieldControls));
        addComboBoxListener(fieldControls, "voltageCode", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "motorCode", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String name = getFieldValue(fieldControls, "name");
        String poles = getFieldValue(fieldControls, "poles");
        String voltageCode = getFieldValue(fieldControls, "voltageCode");
        String motorCode = getFieldValue(fieldControls, "motorCode");

        StringBuilder fullMarking = new StringBuilder();

        if (!name.isEmpty()) {
            fullMarking.append(name);
        }
        if (!poles.isEmpty()) {
            fullMarking.append("-").append(poles);
        }
        if (!voltageCode.isEmpty()) {
            fullMarking.append(voltageCode);
        }
        if (!motorCode.isEmpty()) {
            fullMarking.append("-").append(motorCode);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
}

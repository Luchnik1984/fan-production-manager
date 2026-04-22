package com.fanproduction.gui.configurator;


import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RoofRadialFanCardConfigurator implements CardFieldConfigurator {

    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;

    private TextField getTextField(Map<String, Control> controls, String name) {
        Control c = controls.get(name);
        return c instanceof TextField ? (TextField) c : null;
    }

    private ComboBox<String> getComboBox(Map<String, Control> controls, String name) {
        return CardFieldConfigurator.getComboBox(controls, name);
    }

    @Override
    public void setupFields(Map<String, Control> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {
        this.fieldControls = fieldControls;

        fullMarkingField = getTextField(fieldControls, "fullMarking");

        setupRadialWheelListener();
        setupMotorListener();
        setupFullMarkingGeneration();

        autoFillName(fieldControls, "Вентилятор крышный радиальный", existingCardExists);
    }

    private void setupRadialWheelListener() {
        ComboBox<String> radialWheelCombo = getComboBox(fieldControls, "radialWheelId");
        if (radialWheelCombo != null) {
            radialWheelCombo.valueProperty().addListener((obs, old, val) -> updateFullMarking());
        }
    }

    private void setupMotorListener() {
        ComboBox<String> motorCombo = getComboBox(fieldControls, "motorId");
        TextField polesField = getTextField(fieldControls, "poles");
        TextField voltageField = getTextField(fieldControls, "voltage");
        TextField voltageCodeField = getTextField(fieldControls, "voltageCode");
        TextField ratedSpeedField = getTextField(fieldControls, "ratedSpeedRpm");
        TextField actualSpeedField = getTextField(fieldControls, "actualSpeedRpm");

        if (motorCombo != null) {
            motorCombo.valueProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    // TODO: загрузить данные из выбранного электродвигателя
                    updateFullMarking();
                }
            });
        }
    }

    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        addTextFieldListener("seriesName", this::updateFullMarking);
        addTextFieldListener("executionType", this::updateFullMarking);
        addTextFieldListener("roofSize", this::updateFullMarking);
        addTextFieldListener("poles", this::updateFullMarking);
        addTextFieldListener("climateType", this::updateFullMarking);
        addTextFieldListener("voltage", this::updateFullMarking);
        addTextFieldListener("voltageCode", this::updateFullMarking);

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

        String seriesName = getFieldValue("seriesName");
        String executionType = getFieldValue("executionType");
        String roofSize = getFieldValue("roofSize");
        String poles = getFieldValue("poles");
        String climateType = getFieldValue("climateType");
        String voltage = getFieldValue("voltage");
        String voltageCode = getFieldValue("voltageCode");

        StringBuilder fullMarking = new StringBuilder();

        if (seriesName != null && !seriesName.isEmpty()) {
            fullMarking.append(seriesName);
        }
        if (executionType != null && !executionType.isEmpty()) {
            fullMarking.append("-").append(executionType);
        }
        if (roofSize != null && !roofSize.isEmpty()) {
            fullMarking.append("-").append(roofSize);
        }
        if (poles != null && !poles.isEmpty()) {
            fullMarking.append("-").append(poles);
        }
        if (climateType != null && !climateType.isEmpty()) {
            fullMarking.append("-").append(climateType);
        }
        if (voltage != null && !voltage.isEmpty()) {
            fullMarking.append("-").append(voltage);
        }
        if (voltageCode != null && !voltageCode.isEmpty()) {
            fullMarking.append(voltageCode);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    private String getFieldValue(String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control == null) return "";
        if (control instanceof TextField) return ((TextField) control).getText().trim();
        if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }
}

package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RoofLowProfileFanCardConfigurator implements CardFieldConfigurator {

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
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {
        this.fieldControls = fieldControls;

        fullMarkingField = getTextField(fieldControls, "fullMarking");

        setupMotorWheelListener();
        setupFullMarkingGeneration();

        autoFillName(fieldControls, "Вентилятор крышный низкопрофильный", existingCardExists);
    }

    private void setupMotorWheelListener() {
        ComboBox<String> motorWheelCombo = getComboBox(fieldControls, "motorWheelId");
        TextField polesField = getTextField(fieldControls, "poles");
        TextField voltageField = getTextField(fieldControls, "voltage");

        if (motorWheelCombo != null && polesField != null && voltageField != null) {
            motorWheelCombo.valueProperty().addListener((obs, old, val) -> {
                if (val != null) {
                    // TODO: загрузить данные из выбранного мотор-колеса
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
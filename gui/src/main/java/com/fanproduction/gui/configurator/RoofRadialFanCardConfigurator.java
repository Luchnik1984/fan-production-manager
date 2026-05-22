package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RoofRadialFanCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        setupFullMarkingGeneration(fieldControls);
        autoFillName(fieldControls, "Вентилятор крышный радиальный", existingCardExists);
    }

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "seriesName", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "executionType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "roofSize", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "poles", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "climateType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "voltage", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "voltageCode", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String seriesName = getFieldValue(fieldControls, "seriesName");
        String executionType = getFieldValue(fieldControls, "executionType");
        String roofSize = getFieldValue(fieldControls, "roofSize");
        String poles = getFieldValue(fieldControls, "poles");
        String climateType = getFieldValue(fieldControls, "climateType");
        String voltage = getFieldValue(fieldControls, "voltage");
        String voltageCode = getFieldValue(fieldControls, "voltageCode");

        StringBuilder fullMarking = new StringBuilder();

        if (!seriesName.isEmpty()) {
            fullMarking.append(seriesName);
        }
        if (!executionType.isEmpty()) {
            fullMarking.append("-").append(executionType);
        }
        if (!roofSize.isEmpty()) {
            fullMarking.append("-").append(roofSize);
        }
        if (!poles.isEmpty()) {
            fullMarking.append("-").append(poles);
        }
        if (!climateType.isEmpty()) {
            fullMarking.append("-").append(climateType);
        }
        if (!voltage.isEmpty()) {
            fullMarking.append("-").append(voltage);
        }
        if (!voltageCode.isEmpty()) {
            fullMarking.append(voltageCode);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
}

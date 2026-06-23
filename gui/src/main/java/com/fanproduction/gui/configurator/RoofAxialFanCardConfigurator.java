package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RoofAxialFanCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        setupFullMarkingGeneration(fieldControls);
        autoFillName(fieldControls, "Вентилятор крышный осевой", existingCardExists);
    }

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "seriesName", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "executionType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "roofSize", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "climateType", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String seriesName = getFieldValue(fieldControls, "seriesName");
        String executionType = getFieldValue(fieldControls, "executionType");
        String roofSize = getFieldValue(fieldControls, "roofSize");
        String climateType = getFieldValue(fieldControls, "climateType");

        StringBuilder fullMarking = new StringBuilder();

        if (!seriesName.isEmpty()) fullMarking.append(seriesName);
        if (!executionType.isEmpty()) fullMarking.append("-").append(executionType);
        if (!roofSize.isEmpty()) fullMarking.append("-").append(roofSize);
        if (!climateType.isEmpty()) fullMarking.append("-").append(climateType);

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
}

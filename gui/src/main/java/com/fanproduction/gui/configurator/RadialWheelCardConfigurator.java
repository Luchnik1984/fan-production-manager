package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RadialWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        setupExclusiveSelection(fieldControls);
        setupConditionalVisibility(fieldControls, fieldLabels);
        setupFullMarkingGeneration(fieldControls);

        autoFillName(fieldControls, "Колесо радиальное", existingCardExists);
    }

    private void setupExclusiveSelection(Map<String, Node> fieldControls) {
        CheckBox generalPurposeCheck = getCheckBox(fieldControls, "generalPurpose");
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");

        if (generalPurposeCheck != null) {
            generalPurposeCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                updateFullMarking(fieldControls);
            });
        }

        if (fireproofCheck != null) {
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                updateFullMarking(fieldControls);
            });
        }

        if (explosionCheck != null) {
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                }
                updateFullMarking(fieldControls);
            });
        }
    }

    private void setupConditionalVisibility(Map<String, Node> fieldControls,
                                            Map<String, Label> fieldLabels) {
        // Огнестойкость -> поле маркировки огнестойкости и предельной температуры
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        if (fireproofCheck != null) {
            setVisible(fieldControls, fieldLabels, "fireproofMarking", fireproofCheck.isSelected());
            setVisible(fieldControls, fieldLabels, "maxTemperature", fireproofCheck.isSelected());

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                setVisible(fieldControls, fieldLabels, "fireproofMarking", val);
                setVisible(fieldControls, fieldLabels, "maxTemperature", val);
                updateFullMarking(fieldControls);
            });
        }

        // Взрывозащита -> поле маркировки взрывозащиты
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");
        if (explosionCheck != null) {
            setVisible(fieldControls, fieldLabels, "explosionMarking", explosionCheck.isSelected());

            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                setVisible(fieldControls, fieldLabels, "explosionMarking", val);
                updateFullMarking(fieldControls);
            });
        }
    }

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "bladeMod", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "hubType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));

        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String marking = getFieldValue(fieldControls, "marking");
        String bladeMod = getFieldValue(fieldControls, "bladeMod");
        String hubType = getFieldValue(fieldControls, "hubType");

        boolean isGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue(fieldControls, "fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue(fieldControls, "explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder();

        if (!marking.isEmpty()) {
            fullMarking.append(marking);
        }

        if (isGeneralPurpose) {
            fullMarking.append("-C");
        } else if (isFireproof && !fireproofMarking.isEmpty()) {
            fullMarking.append("-").append(fireproofMarking);
        } else if (isExplosionProof && !explosionMarking.isEmpty()) {
            fullMarking.append("-").append(explosionMarking);
        }

        if (!bladeMod.isEmpty()) {
            fullMarking.append("-").append(bladeMod);
        }

        if (!hubType.isEmpty()) {
            fullMarking.append("-").append(hubType);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
}

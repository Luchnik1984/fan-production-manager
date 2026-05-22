package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class DuctFanCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        setupTypeSelection(fieldControls, fieldLabels);
        setupFullMarkingGeneration(fieldControls);
        setupExclusiveSelection(fieldControls);

        autoFillName(fieldControls, "Вентилятор канальный", existingCardExists);
    }

    private void setupTypeSelection(Map<String, Node> fieldControls, Map<String, Label> fieldLabels) {
        ComboBox<String> typeCombo = getComboBox(fieldControls, "ductFanType");
        if (typeCombo == null) return;

        String savedType = getFieldValue(fieldControls, "ductFanType");

        typeCombo.getItems().clear();
        typeCombo.getItems().addAll("Мотор-колесо", "Радиальное колесо");

        if ("MOTOR_WHEEL".equals(savedType)) {
            typeCombo.setValue("Мотор-колесо");
            updateFieldsVisibility(fieldControls, fieldLabels, true, false);
        } else if ("RADIAL_WHEEL".equals(savedType)) {
            typeCombo.setValue("Радиальное колесо");
            updateFieldsVisibility(fieldControls, fieldLabels, false, true);
        } else {
            typeCombo.setValue("Мотор-колесо");
            updateFieldsVisibility(fieldControls, fieldLabels, true, false);
        }

        typeCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                boolean isMotorWheel = "Мотор-колесо".equals(newVal);
                boolean isRadialWheel = "Радиальное колесо".equals(newVal);
                updateFieldsVisibility(fieldControls, fieldLabels, isMotorWheel, isRadialWheel);
                updateFullMarking(fieldControls);
            }
        });
    }

    private void updateFieldsVisibility(Map<String, Node> fieldControls,
                                        Map<String, Label> fieldLabels,
                                        boolean isMotorWheel,
                                        boolean isRadialWheel) {
        setVisible(fieldControls, fieldLabels, "motorWheelId", isMotorWheel);
        setVisible(fieldControls, fieldLabels, "radialWheelId", isRadialWheel);
        setVisible(fieldControls, fieldLabels, "motorId", isRadialWheel);
        setVisible(fieldControls, fieldLabels, "wheelSize", isRadialWheel);
    }

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "seriesName", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "executionType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "ductSize", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "poles", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "voltage", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "wheelSize", () -> updateFullMarking(fieldControls));
        addComboBoxListener(fieldControls, "ductFanType", () -> updateFullMarking(fieldControls));

        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String seriesName = getFieldValue(fieldControls, "seriesName");
        String executionType = getFieldValue(fieldControls, "executionType");
        String ductSize = getFieldValue(fieldControls, "ductSize");
        String poles = getFieldValue(fieldControls, "poles");
        String voltage = getFieldValue(fieldControls, "voltage");
        String wheelSize = getFieldValue(fieldControls, "wheelSize");
        String ductFanType = getFieldValue(fieldControls, "ductFanType");

        if ("Мотор-колесо".equals(ductFanType)) {
            ductFanType = "MOTOR_WHEEL";
        } else if ("Радиальное колесо".equals(ductFanType)) {
            ductFanType = "RADIAL_WHEEL";
        }

        boolean isGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        String purposeMarking = "";
        if (isFireproof) {
            purposeMarking = getFieldValue(fieldControls, "fireproofMarking");
        } else if (isExplosionProof) {
            purposeMarking = getFieldValue(fieldControls, "explosionMarking");
        }

        StringBuilder fullMarking = new StringBuilder();

        if (!seriesName.isEmpty()) {
            fullMarking.append(seriesName);
        }
        if (!executionType.isEmpty()) {
            fullMarking.append("-").append(executionType);
        }

        if ("MOTOR_WHEEL".equals(ductFanType)) {
            if (!ductSize.isEmpty()) {
                fullMarking.append("-").append(ductSize);
            }
            if (!poles.isEmpty()) {
                fullMarking.append("-").append(poles);
            }
            if (!voltage.isEmpty()) {
                fullMarking.append("-").append(voltage);
            }
        } else if ("RADIAL_WHEEL".equals(ductFanType)) {
            if (!ductSize.isEmpty()) {
                fullMarking.append("-").append(ductSize);
            }
            if (!wheelSize.isEmpty()) {
                try {
                    double ws = Double.parseDouble(wheelSize);
                    fullMarking.append("/").append((int) Math.round(ws * 10));
                } catch (NumberFormatException e) {
                    fullMarking.append("/").append(wheelSize);
                }
            }
            if (!poles.isEmpty()) {
                fullMarking.append(".").append(poles);
            }
            String voltageCode = getVoltageCode(voltage);
            if (!voltageCode.isEmpty()) {
                fullMarking.append(voltageCode);
            }
        }

        if (!isGeneralPurpose && !purposeMarking.isEmpty()) {
            fullMarking.append("-").append(purposeMarking);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    private String getVoltageCode(String voltage) {
        if (voltage == null) return "";
        if (voltage.equals("220")) return "E";
        if (voltage.equals("380")) return "D";
        return "";
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
}
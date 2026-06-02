package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RadialWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";
    private String lastAutoWheelCode = "";
    private String lastAutoWheelFormula = "";
    private String lastAutoFullMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // Управление видимостью полей для собственного производства
        setupOwnProductionVisibility(fieldControls, fieldLabels, fieldHints);

        // Условная видимость (огнестойкость/взрывозащита) — из интерфейса
        setupConditionalVisibility(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));

        // Взаимоисключающие галочки — из интерфейса
        setupExclusiveSelection(fieldControls, () -> updateFullMarking(fieldControls));

        // Автоматическое формирование полей
        setupAutoGeneration(fieldControls);

        // Автоматическое заполнение наименования
        autoFillName(fieldControls, "Колесо радиальное", existingCardExists);
    }

    // ========== УПРАВЛЕНИЕ ВИДИМОСТЬЮ ДЛЯ СОБСТВЕННОГО ПРОИЗВОДСТВА ==========

    private void setupOwnProductionVisibility(Map<String, Node> fieldControls,
                                              Map<String, Label> fieldLabels,
                                              Map<String, Label> fieldHints) {
        CheckBox ownProductionCheck = getCheckBox(fieldControls, "isOwnProduction");
        if (ownProductionCheck == null) return;

        String[] dependentFields = {
                "bladeType", "hubComponentId", "hubName",
                "bladeMod", "frontDiskMod", "wheelWidth",
                "bladeCount", "bladeLengthCoeff", "wheelCode", "wheelFormula"
        };

        boolean isOwn = ownProductionCheck.isSelected();
        for (String fieldName : dependentFields) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isOwn);
        }

        ownProductionCheck.selectedProperty().addListener((obs, old, val) -> {
            for (String fieldName : dependentFields) {
                setVisible(fieldControls, fieldLabels, fieldHints, fieldName, val);
            }
            if (!val) {
                clearDependentFields(fieldControls, dependentFields);
            }
            updateFullMarking(fieldControls);
        });
    }

    private void clearDependentFields(Map<String, Node> fieldControls, String[] fieldNames) {
        for (String fieldName : fieldNames) {
            Node control = fieldControls.get(fieldName);
            if (control instanceof TextField) {
                ((TextField) control).clear();
            } else if (control instanceof ComboBox) {
                ((ComboBox<?>) control).setValue(null);
            }
        }
    }

    // ========== АВТОМАТИЧЕСКОЕ ФОРМИРОВАНИЕ ПОЛЕЙ ==========

    private void setupAutoGeneration(Map<String, Node> fieldControls) {
        setupMarkingGeneration(fieldControls);
        setupWheelCodeGeneration(fieldControls);
        setupWheelFormulaGeneration(fieldControls);
        setupFireproofMarkingGeneration(fieldControls);
        setupFullMarkingGeneration(fieldControls);
    }

    // ========== МАРКИРОВКА (series + size) ==========

    private void setupMarkingGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "series", () -> updateMarking(fieldControls));
        addTextFieldListener(fieldControls, "size", () -> updateMarking(fieldControls));
        updateMarking(fieldControls);
    }

    private void updateMarking(Map<String, Node> fieldControls) {
        TextField markingField = getTextField(fieldControls, "marking");
        if (markingField == null) return;

        String series = getFieldValue(fieldControls, "series");
        String size = getFieldValue(fieldControls, "size");

        StringBuilder newMarking = new StringBuilder();
        if (!series.isEmpty()) newMarking.append(series);
        if (!size.isEmpty()) {
            if (!newMarking.isEmpty()) newMarking.append("-");
            newMarking.append(size);
        }

        String newValue = newMarking.toString();
        String currentValue = markingField.getText();

        if (currentValue == null || currentValue.isEmpty() || currentValue.equals(lastAutoMarking)) {
            markingField.setText(newValue);
            lastAutoMarking = newValue;
        }
    }

    // ========== КОД КОЛЕСА (bladeMod → wheelCode) ==========

    private void setupWheelCodeGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "bladeMod", () -> updateWheelCode(fieldControls));
        updateWheelCode(fieldControls);
    }

    private void updateWheelCode(Map<String, Node> fieldControls) {
        TextField wheelCodeField = getTextField(fieldControls, "wheelCode");
        if (wheelCodeField == null) return;

        String bladeMod = getFieldValue(fieldControls, "bladeMod");
        String currentValue = wheelCodeField.getText();

        if (currentValue == null || currentValue.isEmpty() || currentValue.equals(lastAutoWheelCode)) {
            wheelCodeField.setText(bladeMod);
            lastAutoWheelCode = bladeMod;
        }
    }

    // ========== ФОРМУЛА КОЛЕСА ==========

    private void setupWheelFormulaGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "bladeMod", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "frontDiskMod", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "wheelWidth", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeLengthCoeff", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeCount", () -> updateWheelFormula(fieldControls));
        addComboBoxListener(fieldControls, "bladeType", () -> updateWheelFormula(fieldControls));
    }

    private String getBladeTypeMarking(String bladeTypeValue) {
        if (bladeTypeValue == null) return "";
        return switch (bladeTypeValue) {
            case "V" -> "V";
            case "N" -> "N";
            case "RO" -> "RO";
            default -> "";
        };
    }

    private String formatWheelWidth(Double width) {
        if (width == null) return "";
        int intPart = (int) Math.floor(width);
        int fracPart = (int) Math.round((width - intPart) * 100);
        return String.format("%d%02d", intPart, fracPart);
    }

    private void updateWheelFormula(Map<String, Node> fieldControls) {
        TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
        if (wheelFormulaField == null) return;

        String bladeTypeValue = getFieldValue(fieldControls, "bladeType");
        String bladeTypeMarking = getBladeTypeMarking(bladeTypeValue);
        String wheelCode = getFieldValue(fieldControls, "wheelCode");
        String frontDiskMod = getFieldValue(fieldControls, "frontDiskMod");
        String wheelWidthStr = getFieldValue(fieldControls, "wheelWidth");
        String bladeLengthCoeff = getFieldValue(fieldControls, "bladeLengthCoeff");
        String bladeCount = getFieldValue(fieldControls, "bladeCount");

        if (bladeTypeMarking.isEmpty() && wheelCode.isEmpty() && frontDiskMod.isEmpty() &&
                wheelWidthStr.isEmpty() && bladeCount.isEmpty() && bladeLengthCoeff.isEmpty()) {
            if (wheelFormulaField.getText().isEmpty()) return;
            wheelFormulaField.setText("");
            lastAutoWheelFormula = "";
            return;
        }

        Double wheelWidth = null;
        try {
            if (!wheelWidthStr.isEmpty()) {
                wheelWidth = Double.parseDouble(wheelWidthStr.replace(',', '.'));
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        String formattedWheelWidth = formatWheelWidth(wheelWidth);

        StringBuilder formula = new StringBuilder();

        if (!bladeTypeMarking.isEmpty()) formula.append(bladeTypeMarking);
        if (!wheelCode.isEmpty()) formula.append(".").append(wheelCode);
        if (!frontDiskMod.isEmpty()) formula.append("/").append(frontDiskMod);
        if (!formattedWheelWidth.isEmpty()) formula.append(".").append(formattedWheelWidth);
        if (!bladeCount.isEmpty()) formula.append("/").append(bladeCount);
        if (!bladeLengthCoeff.isEmpty()) formula.append("/").append(bladeLengthCoeff);

        String newFormula = formula.toString();
        String currentFormula = wheelFormulaField.getText();

        if (currentFormula == null || currentFormula.isEmpty() || currentFormula.equals(lastAutoWheelFormula)) {
            wheelFormulaField.setText(newFormula);
            lastAutoWheelFormula = newFormula;
        }
    }

    // ========== ОГНЕСТОЙКОСТЬ (авто-формирование fireproofMarking) ==========

    private void setupFireproofMarkingGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "fireproofTime", () -> updateFireproofMarking(fieldControls));
        addTextFieldListener(fieldControls, "maxTemperature", () -> updateFireproofMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFireproofMarking(fieldControls));
        updateFireproofMarking(fieldControls);
    }

    private void updateFireproofMarking(Map<String, Node> fieldControls) {
        TextField fireproofMarkingField = getTextField(fieldControls, "fireproofMarking");
        if (fireproofMarkingField == null) return;

        boolean isFireproof = isSelected(fieldControls, "fireproof");

        if (!isFireproof) {
            fireproofMarkingField.setText("");
            return;
        }

        String fireproofTime = getFieldValue(fieldControls, "fireproofTime");
        String maxTemperature = getFieldValue(fieldControls, "maxTemperature");

        StringBuilder marking = new StringBuilder("F");

        if (!fireproofTime.isEmpty()) {
            marking.append("-").append(fireproofTime);
        }

        String temp = maxTemperature.isEmpty() ? "400" : maxTemperature;
        marking.append("/").append(temp);

        String newMarking = marking.toString();
        fireproofMarkingField.setText(newMarking);
    }

    // ========== ПОЛНАЯ МАРКИРОВКА ==========

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "series", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "size", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "wheelFormula", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "hubName", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));
        updateFullMarking(fieldControls);
    }

    private String getExecutionMarking(Map<String, Node> fieldControls) {
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

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String marking = getFieldValue(fieldControls, "marking");
        String executionMarking = getExecutionMarking(fieldControls);

        StringBuilder fullMarking = new StringBuilder();
        fullMarking.append(marking);

        if (!executionMarking.isEmpty()) {
            fullMarking.append("-").append(executionMarking);
        }

        CheckBox ownProductionCheck = getCheckBox(fieldControls, "isOwnProduction");
        boolean isOwn = ownProductionCheck != null && ownProductionCheck.isSelected();

        if (isOwn) {
            String wheelFormula = getFieldValue(fieldControls, "wheelFormula");
            String hubName = getFieldValue(fieldControls, "hubName");

            if (!wheelFormula.isEmpty()) {
                fullMarking.append("-").append(wheelFormula);
            }
            if (!hubName.isEmpty()) {
                fullMarking.append("-").append(hubName);
            }
        }

        String newFullMarking = fullMarking.toString();
        String currentFullMarking = fullMarkingField.getText();

        if (currentFullMarking == null || currentFullMarking.isEmpty() || currentFullMarking.equals(lastAutoFullMarking)) {
            fullMarkingField.setText(newFullMarking);
            lastAutoFullMarking = newFullMarking;
        }
    }
}

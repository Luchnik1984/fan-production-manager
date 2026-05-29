package com.fanproduction.gui.configurator;

import javafx.scene.Node;
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

        // Условная видимость (огнестойкость/взрывозащита) - из интерфейса
        setupConditionalVisibility(fieldControls, fieldLabels, () -> updateFullMarking(fieldControls));

        // Взаимоисключающие галочки - из интерфейса
        setupExclusiveSelection(fieldControls, () -> updateFullMarking(fieldControls));

        // Автоматическое формирование полей
        setupMarkingGeneration(fieldControls);
        setupWheelCodeGeneration(fieldControls);
        setupWheelFormulaGeneration(fieldControls);
        setupFullMarkingGeneration(fieldControls);

        // Автоматическое заполнение наименования
        autoFillName(fieldControls, "Колесо радиальное", existingCardExists);
    }

    // ========== АВТОМАТИЧЕСКОЕ ФОРМИРОВАНИЕ ПОЛЕЙ ==========

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
        if (!series.isEmpty()) {
            newMarking.append(series);
        }
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

    // ========== КОД КОЛЕСА ==========

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
        updateWheelFormula(fieldControls);
    }

    private String getBladeTypeMarking(String bladeTypeRussian) {
        if (bladeTypeRussian == null) return "";
        return switch (bladeTypeRussian) {
            case "впередзагнутые" -> "V";
            case "назадзагнутые" -> "N";
            case "радиальнооканчивающиеся" -> "RO";
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

        String bladeTypeRussian = getFieldValue(fieldControls, "bladeType");
        String bladeTypeMarking = getBladeTypeMarking(bladeTypeRussian);
        String wheelCode = getFieldValue(fieldControls, "wheelCode");
        String frontDiskMod = getFieldValue(fieldControls, "frontDiskMod");
        String wheelWidthStr = getFieldValue(fieldControls, "wheelWidth");
        String bladeLengthCoeff = getFieldValue(fieldControls, "bladeLengthCoeff");
        String bladeCount = getFieldValue(fieldControls, "bladeCount");

        Double wheelWidth = null;
        try {
            if (!wheelWidthStr.isEmpty()) {
                wheelWidth = Double.parseDouble(wheelWidthStr.replace(',', '.'));
            }
        } catch (NumberFormatException e) {
            // игнорируем
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

    // ========== ПОЛНАЯ МАРКИРОВКА ==========

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "series", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "size", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "wheelFormula", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));
        addComboBoxListener(fieldControls, "bladeType", () -> updateFullMarking(fieldControls));
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
        if (isFireproof) return "F";
        if (isExplosionProof) return "Ex";
        return "";
    }

    private String getSpecialMarking(Map<String, Node> fieldControls) {
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        if (isFireproof) {
            String marking = getFieldValue(fieldControls, "fireproofMarking");
            return !marking.isEmpty() ? marking : "F/400";
        }
        if (isExplosionProof) {
            String marking = getFieldValue(fieldControls, "explosionMarking");
            return !marking.isEmpty() ? marking : "1Ex d IIC T4 Gb";
        }
        return "";
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String marking = getFieldValue(fieldControls, "marking");
        String execution = getExecutionMarking(fieldControls);
        String specialMarking = getSpecialMarking(fieldControls);
        String wheelFormula = getFieldValue(fieldControls, "wheelFormula");
        String hubName = getFieldValue(fieldControls, "hubName");

        StringBuilder fullMarking = new StringBuilder();

        if (!marking.isEmpty()) fullMarking.append(marking);
        if (!execution.isEmpty()) fullMarking.append("-").append(execution);
        if (!specialMarking.isEmpty()) fullMarking.append("-").append(specialMarking);
        if (!wheelFormula.isEmpty()) fullMarking.append("-").append(wheelFormula);
        if (!hubName.isEmpty()) fullMarking.append("-").append(hubName);

        String newFullMarking = fullMarking.toString();
        String currentFullMarking = fullMarkingField.getText();

        if (currentFullMarking == null || currentFullMarking.isEmpty() || currentFullMarking.equals(lastAutoFullMarking)) {
            fullMarkingField.setText(newFullMarking);
            lastAutoFullMarking = newFullMarking;
        }
    }
}

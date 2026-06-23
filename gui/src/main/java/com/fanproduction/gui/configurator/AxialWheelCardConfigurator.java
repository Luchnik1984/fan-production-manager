package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class AxialWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // Условная видимость (огнестойкость/взрывозащита) — с fieldHints
        setupConditionalVisibility(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));

        // Взаимоисключающие галочки
        setupExclusiveSelection(fieldControls, () -> updateFullMarking(fieldControls));

        // Настройка расчётов (диаметр колеса, формула колеса)
        setupCalculations(fieldControls);

        // Настройка автоматического формирования полной маркировки
        setupFullMarkingGeneration(fieldControls);

        // Автоматическое заполнение наименования
        autoFillName(fieldControls, "Колесо осевое", existingCardExists);
    }

    private void setupCalculations(Map<String, Node> fieldControls) {
        // Расчёт диаметра колеса
        TextField sizeField = getTextField(fieldControls, "size");
        TextField trimField = getTextField(fieldControls, "trimCoefficient");
        TextField wheelDiameterField = getTextField(fieldControls, "wheelDiameter");

        if (sizeField != null && trimField != null && wheelDiameterField != null) {
            Runnable calculate = () -> {
                try {
                    String sizeText = sizeField.getText().replace(',', '.');
                    String trimText = trimField.getText().replace(',', '.');

                    if (!sizeText.isEmpty()) {
                        double size = Double.parseDouble(sizeText);
                        double trim = trimText.isEmpty() ? 0 : Double.parseDouble(trimText);
                        double diameter = size * (100 - trim);
                        wheelDiameterField.setText(String.valueOf(Math.round(diameter)));
                    } else {
                        wheelDiameterField.setText("");
                    }
                } catch (NumberFormatException e) {
                    wheelDiameterField.setText("");
                }
            };

            sizeField.textProperty().addListener((obs, old, val) -> calculate.run());
            trimField.textProperty().addListener((obs, old, val) -> calculate.run());
            calculate.run();
        }

        // Расчёт формулы колеса
        TextField bladeCountField = getTextField(fieldControls, "bladeCount");
        TextField bladeSlotsField = getTextField(fieldControls, "bladeSlots");
        TextField bladeTypeField = getTextField(fieldControls, "bladeType");
        TextField bladeAngleField = getTextField(fieldControls, "bladeAngle");
        ComboBox<String> bladeMaterialCombo = getComboBox(fieldControls, "bladeMaterial");
        TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
        TextField wheelDiameterForFormula = getTextField(fieldControls, "wheelDiameter");

        if (wheelFormulaField != null) {
            Runnable updateFormula = () -> {
                String diameter = wheelDiameterForFormula != null ? wheelDiameterForFormula.getText() : "";
                String bladeCount = bladeCountField != null ? bladeCountField.getText() : "";
                String bladeSlots = bladeSlotsField != null ? bladeSlotsField.getText() : "";
                String bladeType = bladeTypeField != null ? bladeTypeField.getText() : "";
                String bladeAngle = bladeAngleField != null ? bladeAngleField.getText() : "";
                String bladeMaterial = bladeMaterialCombo != null ? bladeMaterialCombo.getValue() : "";

                StringBuilder formula = new StringBuilder();
                if (!diameter.isEmpty()) formula.append(diameter);
                if (!bladeCount.isEmpty()) formula.append("/").append(bladeCount);
                if (!bladeSlots.isEmpty()) formula.append("-").append(bladeSlots);
                if (!bladeType.isEmpty()) formula.append("/").append(bladeType);
                if (!bladeAngle.isEmpty()) formula.append("/").append(bladeAngle);
                if (bladeMaterial != null && !bladeMaterial.isEmpty()) formula.append("/").append(bladeMaterial);

                wheelFormulaField.setText(formula.toString());
            };

            if (wheelDiameterForFormula != null) {
                wheelDiameterForFormula.textProperty().addListener((obs, old, val) -> updateFormula.run());
            }
            if (bladeCountField != null) {
                bladeCountField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            }
            if (bladeSlotsField != null) {
                bladeSlotsField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            }
            if (bladeTypeField != null) {
                bladeTypeField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            }
            if (bladeAngleField != null) {
                bladeAngleField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            }
            if (bladeMaterialCombo != null) {
                bladeMaterialCombo.valueProperty().addListener((obs, old, val) -> updateFormula.run());
            }

            updateFormula.run();
        }
    }

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "size", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "wheelFormula", () -> updateFullMarking(fieldControls));
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
        String size = getFieldValue(fieldControls, "size");
        String wheelFormula = getFieldValue(fieldControls, "wheelFormula");

        boolean isGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue(fieldControls, "fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue(fieldControls, "explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder();

        if (!marking.isEmpty()) fullMarking.append(marking).append(" ");
        if (!size.isEmpty()) fullMarking.append(size);
        if (isGeneralPurpose) {
            fullMarking.append("-C");
        } else if (isFireproof && !fireproofMarking.isEmpty()) {
            fullMarking.append("-").append(fireproofMarking);
        } else if (isExplosionProof && !explosionMarking.isEmpty()) {
            fullMarking.append("-").append(explosionMarking);
        }
        if (!wheelFormula.isEmpty()) fullMarking.append("-").append(wheelFormula);

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
}
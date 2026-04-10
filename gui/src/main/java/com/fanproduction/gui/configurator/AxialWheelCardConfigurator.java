package com.fanproduction.gui.configurator;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.util.Map;

public class AxialWheelCardConfigurator implements CardFieldConfigurator {

    private TextField wheelDiameterField;
    private TextField wheelFormulaField;
    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;

    @Override
    public void setupFields(Map<String, Control> fieldControls, boolean existingCardExists) {
        this.fieldControls = fieldControls;

        wheelDiameterField = getTextField(fieldControls, "wheelDiameter");
        wheelFormulaField = getTextField(fieldControls, "wheelFormula");
        fullMarkingField = getTextField(fieldControls, "fullMarking");

        setupCalculations();
        setupConditionalVisibility();
        setupFullMarkingGeneration();

        // Автоматическое заполнение наименования
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Колесо осевое");
            }
        }
    }

    private void setupCalculations() {
        // Расчёт диаметра колеса: size * (100 - trimCoefficient) / 100
        TextField sizeField = getTextField(fieldControls, "size");
        TextField trimField = getTextField(fieldControls, "trimCoefficient");

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

        // Формирование формулы колеса: диаметр/кол-во лопаток-посадочных мест/форма лопатки/материал
        TextField bladeCountField = getTextField(fieldControls, "bladeCount");
        TextField bladeSlotsField = getTextField(fieldControls, "bladeSlots");
        TextField bladeShapeField = getTextField(fieldControls, "bladeShape");
        TextField bladeMaterialField = getTextField(fieldControls, "bladeMaterial");

        if (wheelFormulaField != null) {
            Runnable updateFormula = () -> {
                String diameter = wheelDiameterField != null ? wheelDiameterField.getText() : "";
                String bladeCount = bladeCountField != null ? bladeCountField.getText() : "";
                String bladeSlots = bladeSlotsField != null ? bladeSlotsField.getText() : "";
                String bladeShape = bladeShapeField != null ? bladeShapeField.getText() : "";
                String material = bladeMaterialField != null ? bladeMaterialField.getText() : "";

                StringBuilder formula = new StringBuilder();
                if (!diameter.isEmpty()) formula.append(diameter);
                if (!bladeCount.isEmpty()) formula.append("/").append(bladeCount);
                if (!bladeSlots.isEmpty()) formula.append("-").append(bladeSlots);
                if (!bladeShape.isEmpty()) formula.append("/").append(bladeShape);
                if (!material.isEmpty()) formula.append("/").append(material);

                wheelFormulaField.setText(formula.toString());
            };

            if (wheelDiameterField != null) wheelDiameterField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeCountField != null) bladeCountField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeSlotsField != null) bladeSlotsField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeShapeField != null) bladeShapeField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeMaterialField != null) bladeMaterialField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            updateFormula.run();
        }
    }

    private void setupConditionalVisibility() {
        // Огнестойкость -> поле предельной температуры
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        Control tempField = fieldControls.get("maxTemperature");

        if (fireproofCheck != null && tempField != null) {
            boolean initialVisible = fireproofCheck.isSelected();
            tempField.setVisible(initialVisible);
            tempField.setManaged(initialVisible);

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                tempField.setVisible(val);
                tempField.setManaged(val);
            });
        }

        // Взрывозащита -> поле маркировки взрывозащиты
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");
        Control markingField = fieldControls.get("explosionMarking");

        if (explosionCheck != null && markingField != null) {
            boolean initialVisible = explosionCheck.isSelected();
            markingField.setVisible(initialVisible);
            markingField.setManaged(initialVisible);

            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                markingField.setVisible(val);
                markingField.setManaged(val);
            });
        }
    }

    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        addTextFieldListener("marking", this::updateFullMarking);
        addTextFieldListener("size", this::updateFullMarking);
        addTextFieldListener("execution", this::updateFullMarking);
        addTextFieldListener("explosionMarking", this::updateFullMarking);
        addTextFieldListener("wheelFormula", this::updateFullMarking);
        addTextFieldListener("bladeMaterial", this::updateFullMarking);

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

        String marking = getFieldValue("marking");
        String size = getFieldValue("size");
        String execution = getFieldValue("execution");
        String explosionMarking = getFieldValue("explosionMarking");
        String wheelFormula = getFieldValue("wheelFormula");
        String bladeMaterial = getFieldValue("bladeMaterial");

        StringBuilder fullMarking = new StringBuilder("Колесо Осевое ");
        if (marking != null && !marking.isEmpty()) fullMarking.append(marking).append(" ");
        if (size != null && !size.isEmpty()) fullMarking.append(size);
        if (execution != null && !execution.isEmpty()) fullMarking.append("-").append(execution);
        if (explosionMarking != null && !explosionMarking.isEmpty()) fullMarking.append(explosionMarking);
        if (wheelFormula != null && !wheelFormula.isEmpty()) fullMarking.append("-").append(wheelFormula);
        if (bladeMaterial != null && !bladeMaterial.isEmpty()) fullMarking.append("/").append(bladeMaterial);

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

    private TextField getTextField(Map<String, Control> controls, String name) {
        Control c = controls.get(name);
        return c instanceof TextField ? (TextField) c : null;
    }

    private CheckBox getCheckBox(Map<String, Control> controls, String name) {
        Control c = controls.get(name);
        return c instanceof CheckBox ? (CheckBox) c : null;
    }
}

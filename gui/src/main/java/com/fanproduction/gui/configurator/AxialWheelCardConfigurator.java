package com.fanproduction.gui.configurator;

import javafx.scene.control.*;

import java.util.Map;

public class AxialWheelCardConfigurator implements CardFieldConfigurator {

    private TextField wheelDiameterField;
    private TextField wheelFormulaField;
    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;

    private ComboBox<String> getComboBox(Map<String, Control> controls, String name) {
        return CardFieldConfigurator.getComboBox(controls, name);
    }

    private TextField getTextField(Map<String, Control> controls, String name) {
        return CardFieldConfigurator.getTextField(controls, name);
    }

    private CheckBox getCheckBox(Map<String, Control> controls, String name) {
        return CardFieldConfigurator.getCheckBox(controls, name);
    }

    public void setupFields(Map<String, Control> fieldControls, Map<String, Label> fieldLabels, Map<String, Label> fieldHints, boolean existingCardExists) {
        this.fieldControls = fieldControls;

//        // Отладка: выводим все ключи fieldControls
//        System.out.println("=== AxialWheelCardConfigurator: fieldControls keys ===");
//        for (String key : fieldControls.keySet()) {
//            System.out.println("  " + key);
//        }

        wheelDiameterField = getTextField(fieldControls, "wheelDiameter");
        wheelFormulaField = getTextField(fieldControls, "wheelFormula");
        fullMarkingField = getTextField(fieldControls, "fullMarking");

//        // Проверка конкретного поля
//        Control bladeMaterialControl = fieldControls.get("bladeMaterial");
//        System.out.println("bladeMaterial control: " + bladeMaterialControl);
//        System.out.println("bladeMaterial is ComboBox? " + (bladeMaterialControl instanceof ComboBox));

        setupCalculations();
        setupExclusiveSelection();
        setupConditionalVisibility();
        setupFullMarkingGeneration();

        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Колесо осевое");
            }
        }
    }

    private void setupCalculations() {
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

        TextField bladeCountField = getTextField(fieldControls, "bladeCount");
        TextField bladeSlotsField = getTextField(fieldControls, "bladeSlots");
        TextField bladeTypeField = getTextField(fieldControls, "bladeType");
        TextField bladeAngleField = getTextField(fieldControls, "bladeAngle");
        ComboBox<String> bladeMaterialCombo = getComboBox(fieldControls, "bladeMaterial");

        if (wheelFormulaField != null) {
            Runnable updateFormula = () -> {
                String diameter = wheelDiameterField != null ? wheelDiameterField.getText() : "";
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

            if (wheelDiameterField != null) wheelDiameterField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeCountField != null) bladeCountField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeSlotsField != null) bladeSlotsField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeTypeField != null) bladeTypeField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeAngleField != null) bladeAngleField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeMaterialCombo != null) bladeMaterialCombo.valueProperty().addListener((obs, old, val) -> updateFormula.run());

            updateFormula.run();
        }
    }

    private void setupExclusiveSelection() {
        CheckBox generalPurposeCheck = getCheckBox(fieldControls, "generalPurpose");
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");

        if (generalPurposeCheck != null) {
            generalPurposeCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                updateFullMarking();
            });
        }

        if (fireproofCheck != null) {
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                updateFullMarking();
            });
        }

        if (explosionCheck != null) {
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                }
                updateFullMarking();
            });
        }
    }

    private void setupConditionalVisibility() {
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        Control fireproofMarkingField = fieldControls.get("fireproofMarking");
        Control tempField = fieldControls.get("maxTemperature");

        if (fireproofCheck != null) {
            boolean isVisible = fireproofCheck.isSelected();
            if (fireproofMarkingField != null) {
                fireproofMarkingField.setVisible(isVisible);
                fireproofMarkingField.setManaged(isVisible);
            }
            if (tempField != null) {
                tempField.setVisible(isVisible);
                tempField.setManaged(isVisible);
            }
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (fireproofMarkingField != null) {
                    fireproofMarkingField.setVisible(val);
                    fireproofMarkingField.setManaged(val);
                }
                if (tempField != null) {
                    tempField.setVisible(val);
                    tempField.setManaged(val);
                }
                updateFullMarking();
            });
        }

        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");
        Control explosionMarkingField = fieldControls.get("explosionMarking");

        if (explosionCheck != null && explosionMarkingField != null) {
            boolean isVisible = explosionCheck.isSelected();
            explosionMarkingField.setVisible(isVisible);
            explosionMarkingField.setManaged(isVisible);
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                explosionMarkingField.setVisible(val);
                explosionMarkingField.setManaged(val);
                updateFullMarking();
            });
        }
    }

    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        addTextFieldListener("marking", this::updateFullMarking);
        addTextFieldListener("size", this::updateFullMarking);
        addTextFieldListener("wheelFormula", this::updateFullMarking);
        addTextFieldListener("fireproofMarking", this::updateFullMarking);
        addTextFieldListener("explosionMarking", this::updateFullMarking);

        addCheckBoxListener("generalPurpose", this::updateFullMarking);
        addCheckBoxListener("fireproof", this::updateFullMarking);
        addCheckBoxListener("explosionProof", this::updateFullMarking);

        updateFullMarking();
    }

    private void addTextFieldListener(String fieldName, Runnable callback) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void addCheckBoxListener(String fieldName, Runnable callback) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        if (checkBox != null) {
            checkBox.selectedProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void updateFullMarking() {
        if (fullMarkingField == null) return;

        String marking = getFieldValue("marking");
        String size = getFieldValue("size");
        String wheelFormula = getFieldValue("wheelFormula");  // уже содержит материал

        boolean isGeneralPurpose = isSelected("generalPurpose");
        boolean isFireproof = isSelected("fireproof");
        boolean isExplosionProof = isSelected("explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue("fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue("explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder();

        // Маркировка
        if (marking != null && !marking.isEmpty()) {
            fullMarking.append(marking).append(" ");
        }

        // Типоразмер
        if (size != null && !size.isEmpty()) {
            fullMarking.append(size);
        }

        // Исполнение
        if (isGeneralPurpose) {
            fullMarking.append("-C");
        } else if (isFireproof && fireproofMarking != null && !fireproofMarking.isEmpty()) {
            fullMarking.append("-").append(fireproofMarking);
        } else if (isExplosionProof && explosionMarking != null && !explosionMarking.isEmpty()) {
            fullMarking.append("-").append(explosionMarking);
        }

        // Формула колеса (уже содержит материал)
        if (wheelFormula != null && !wheelFormula.isEmpty()) {
            fullMarking.append("-").append(wheelFormula);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    private boolean isSelected(String fieldName) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        return checkBox != null && checkBox.isSelected();
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

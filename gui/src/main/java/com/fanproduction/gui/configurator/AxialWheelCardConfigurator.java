package com.fanproduction.gui.configurator;

import javafx.scene.control.CheckBox;
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
        setupExclusiveSelection();
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
        // Расчёт диаметра колеса: size * (100 - trimCoefficient)
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

        // Формирование формулы колеса: диаметр/количество лопаток-посадочных мест/тип лопаток/угол установки
        TextField bladeCountField = getTextField(fieldControls, "bladeCount");
        TextField bladeSlotsField = getTextField(fieldControls, "bladeSlots");
        TextField bladeTypeField = getTextField(fieldControls, "bladeType");
        TextField bladeAngleField = getTextField(fieldControls, "bladeAngle");

        if (wheelFormulaField != null) {
            Runnable updateFormula = () -> {
                String diameter = wheelDiameterField != null ? wheelDiameterField.getText() : "";
                String bladeCount = bladeCountField != null ? bladeCountField.getText() : "";
                String bladeSlots = bladeSlotsField != null ? bladeSlotsField.getText() : "";
                String bladeType = bladeTypeField != null ? bladeTypeField.getText() : "";
                String bladeAngle = bladeAngleField != null ? bladeAngleField.getText() : "";

                StringBuilder formula = new StringBuilder();
                if (!diameter.isEmpty()) formula.append(diameter);
                if (!bladeCount.isEmpty()) formula.append("/").append(bladeCount);
                if (!bladeSlots.isEmpty()) formula.append("-").append(bladeSlots);
                if (!bladeType.isEmpty()) formula.append("/").append(bladeType);
                if (!bladeAngle.isEmpty()) formula.append("/").append(bladeAngle);

                wheelFormulaField.setText(formula.toString());
            };

            if (wheelDiameterField != null) wheelDiameterField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeCountField != null) bladeCountField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeSlotsField != null) bladeSlotsField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeTypeField != null) bladeTypeField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            if (bladeAngleField != null) bladeAngleField.textProperty().addListener((obs, old, val) -> updateFormula.run());
            updateFormula.run();
        }
    }

    /**
     * Взаимоисключающие галочки: общее назначение не может быть с огнестойкостью или взрывозащитой
     */
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
        // Огнестойкость -> поле маркировки огнестойкости и предельной температуры
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

        // Взрывозащита -> поле маркировки взрывозащиты
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

        // Слушатели на все поля, влияющие на маркировку
        addTextFieldListener("marking", this::updateFullMarking);
        addTextFieldListener("size", this::updateFullMarking);
        addTextFieldListener("fireproofMarking", this::updateFullMarking);
        addTextFieldListener("explosionMarking", this::updateFullMarking);
        addTextFieldListener("wheelFormula", this::updateFullMarking);
        addTextFieldListener("bladeMaterial", this::updateFullMarking);

        // Слушатели на галочки
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
        String wheelFormula = getFieldValue("wheelFormula");
        String bladeMaterial = getFieldValue("bladeMaterial");

        boolean isGeneralPurpose = isSelected("generalPurpose");
        boolean isFireproof = isSelected("fireproof");
        boolean isExplosionProof = isSelected("explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue("fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue("explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder("Колесо Осевое ");

        // Маркировка
        if (marking != null && !marking.isEmpty()) {
            fullMarking.append(marking).append(" ");
        }

        // Типоразмер
        if (size != null && !size.isEmpty()) {
            fullMarking.append(size);
        }

        // Исполнение (C - общее, F - огнестойкость, Ex - взрывозащита)
        if (isGeneralPurpose) {
            fullMarking.append("-C");
        } else if (isFireproof && fireproofMarking != null && !fireproofMarking.isEmpty()) {
            fullMarking.append("-").append(fireproofMarking);
        } else if (isExplosionProof && explosionMarking != null && !explosionMarking.isEmpty()) {
            fullMarking.append("-").append(explosionMarking);
        }

        // Формула колеса
        if (wheelFormula != null && !wheelFormula.isEmpty()) {
            fullMarking.append("-").append(wheelFormula);
        }

        // Материал лопаток
        if (bladeMaterial != null && !bladeMaterial.isEmpty()) {
            fullMarking.append("/").append(bladeMaterial);
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

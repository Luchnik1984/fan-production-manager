package com.fanproduction.gui.configurator;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class RadialWheelCardConfigurator implements CardFieldConfigurator {

    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;

    @Override
    public void setupFields(Map<String, Control> fieldControls, Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints, boolean existingCardExists) {
        this.fieldControls = fieldControls;

        fullMarkingField = getTextField(fieldControls, "fullMarking");

        setupExclusiveSelection();
        setupConditionalVisibility();
        setupFullMarkingGeneration();

        // Автоматическое заполнение наименования
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Колесо радиальное");
            }
        }
    }

    /**
     * Взаимоисключающие галочки
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

        addTextFieldListener("marking", this::updateFullMarking);
        addTextFieldListener("bladeMod", this::updateFullMarking);
        addTextFieldListener("hubType", this::updateFullMarking);
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
        String bladeMod = getFieldValue("bladeMod");
        String hubType = getFieldValue("hubType");

        boolean isGeneralPurpose = isSelected("generalPurpose");
        boolean isFireproof = isSelected("fireproof");
        boolean isExplosionProof = isSelected("explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue("fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue("explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder();

        // Маркировка
        if (!marking.isEmpty()) {
            fullMarking.append(marking);
        }

        // Исполнение
        if (isGeneralPurpose) {
            fullMarking.append("-C");
        } else if (isFireproof) {
            fullMarking.append("-").append(fireproofMarking);
        } else if (isExplosionProof && !explosionMarking.isEmpty()) {
            fullMarking.append("-").append(explosionMarking);
        }

        // Модификация лопатки
        if (!bladeMod.isEmpty()) {
            fullMarking.append("-").append(bladeMod);
        }

        // Ступица
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

package com.fanproduction.gui.configurator;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.util.Map;

public class RadialWheelCardConfigurator implements CardFieldConfigurator {

    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;

    @Override
    public void setupFields(Map<String, Control> fieldControls, boolean existingCardExists) {
        this.fieldControls = fieldControls;

        fullMarkingField = getTextField(fieldControls, "fullMarking");

        setupConditionalVisibility();
        setupFullMarkingGeneration();

        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Радиальное колесо");
            }
        }
    }

    private void setupConditionalVisibility() {
        // Огнестойкость -> поле предельной температуры
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        Control tempField = fieldControls.get("maxTemperature");
        Control tempLabel = fieldControls.get("maxTemperature_label");
        Control tempHint = fieldControls.get("maxTemperature_hint");

        if (fireproofCheck != null && tempField != null) {
            boolean initialVisible = fireproofCheck.isSelected();
            setVisibility(tempField, tempLabel, tempHint, initialVisible);

            fireproofCheck.selectedProperty().addListener((obs, old, val) ->
                    setVisibility(tempField, tempLabel, tempHint, val));
        }

        // Взрывозащита -> поле маркировки взрывозащиты
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");
        Control markingField = fieldControls.get("explosionMarking");
        Control markingLabel = fieldControls.get("explosionMarking_label");
        Control markingHint = fieldControls.get("explosionMarking_hint");

        if (explosionCheck != null && markingField != null) {
            boolean initialVisible = explosionCheck.isSelected();
            setVisibility(markingField, markingLabel, markingHint, initialVisible);

            explosionCheck.selectedProperty().addListener((obs, old, val) ->
                    setVisibility(markingField, markingLabel, markingHint, val));
        }
    }

    private void setVisibility(Control field, Control label, Control hint, boolean visible) {
        if (field != null) {
            field.setVisible(visible);
            field.setManaged(visible);
        }
        if (label != null) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
        if (hint != null) {
            hint.setVisible(visible);
            hint.setManaged(visible);
        }
    }

    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        addTextFieldListener("marking", this::updateFullMarking);
        addTextFieldListener("bladeMod", this::updateFullMarking);
        addTextFieldListener("hubType", this::updateFullMarking);

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
        String bladeMod = getFieldValue("bladeMod");
        String hubType = getFieldValue("hubType");

        StringBuilder fullMarking = new StringBuilder();
        if (marking != null && !marking.isEmpty()) fullMarking.append(marking);
        if (bladeMod != null && !bladeMod.isEmpty()) fullMarking.append("-").append(bladeMod);
        if (hubType != null && !hubType.isEmpty()) fullMarking.append("-").append(hubType);

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

    private CheckBox getCheckBox(Map<String, Control> controls, String name) {
        Control c = controls.get(name);
        return c instanceof CheckBox ? (CheckBox) c : null;
    }

    private TextField getTextField(Map<String, Control> controls, String name) {
        Control c = controls.get(name);
        return c instanceof TextField ? (TextField) c : null;
    }
}

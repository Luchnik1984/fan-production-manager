package com.fanproduction.gui.configurator;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.util.Map;

/**
 * Настройка специальных полей для карточки электродвигателя.
 */
public class MotorCardConfigurator implements CardFieldConfigurator {

    private TextField ratedSpeedField;
    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;

    @Override
    public void setupFields(Map<String, Control> fieldControls, boolean existingCardExists) {
        this.fieldControls = fieldControls;

        // Сохраняем ссылки на специальные поля
        ratedSpeedField = CardFieldConfigurator.getTextField(fieldControls, "ratedSpeedRpm");
        fullMarkingField = CardFieldConfigurator.getTextField(fieldControls, "fullMarking");

        // Настройка условного отображения полей (огнестойкий, взрывозащищённый)
        setupConditionalVisibility();

        // Настройка автоматического расчёта номинальной скорости
        setupRatedSpeedCalculation();

        // Настройка автоматического формирования полной маркировки
        setupFullMarkingGeneration();
    }

    /**
     * Настройка условного отображения полей
     */
    private void setupConditionalVisibility() {
        // Огнестойкость -> поле предельной температуры
        var fireproofCheck = CardFieldConfigurator.getCheckBox(fieldControls, "fireproof");
        var tempField = fieldControls.get("maxTemperature");
        var tempLabel = fieldControls.get("maxTemperature_label");
        var tempHint = fieldControls.get("maxTemperature_hint");

        if (fireproofCheck != null && tempField != null) {
            boolean initialVisible = fireproofCheck.isSelected();
            setVisibility(tempField, tempLabel, tempHint, initialVisible);

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> setVisibility(tempField, tempLabel, tempHint, val));
        }

        // Взрывозащита -> поле маркировки взрывозащиты
        var explosionCheck = CardFieldConfigurator.getCheckBox(fieldControls, "explosionProof");
        var markingField = fieldControls.get("explosionMarking");
        var markingLabel = fieldControls.get("explosionMarking_label");
        var markingHint = fieldControls.get("explosionMarking_hint");

        if (explosionCheck != null && markingField != null) {
            boolean initialVisible = explosionCheck.isSelected();
            setVisibility(markingField, markingLabel, markingHint, initialVisible);

            explosionCheck.selectedProperty().addListener((obs, old, val) -> setVisibility(markingField, markingLabel, markingHint, val));
        }
    }

    /**
     * Устанавливает видимость поля, его лейбла и подсказки
     */
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

    /**
     * Настройка автоматического расчёта номинальной скорости
     */
    private void setupRatedSpeedCalculation() {
        ComboBox<String> polesCombo = CardFieldConfigurator.getComboBox(fieldControls, "poles");
        if (polesCombo != null && ratedSpeedField != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> {
                updateRatedSpeed(polesCombo);
                updateFullMarking(); // при изменении полюсов обновляем маркировку
            });
            updateRatedSpeed(polesCombo);
        }
    }

    /**
     * Обновляет номинальную скорость на основе количества полюсов
     */
    private void updateRatedSpeed(ComboBox<String> polesCombo) {
        if (ratedSpeedField == null || polesCombo == null) return;

        String value = polesCombo.getValue();
        if (value != null && !value.isEmpty()) {
            try {
                int poles = Integer.parseInt(value);
                int ratedSpeed = 6000 / poles;
                ratedSpeedField.setText(String.valueOf(ratedSpeed));
            } catch (NumberFormatException e) {
                ratedSpeedField.setText("");
            }
        } else {
            ratedSpeedField.setText("");
        }
    }

    /**
     * Настройка автоматического формирования полной маркировки
     */
    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        // Добавляем слушатели на поля, влияющие на маркировку
        addTextFieldListener("series", this::updateFullMarking);
        addTextFieldListener("motorType", this::updateFullMarking);

        ComboBox<String> polesCombo = CardFieldConfigurator.getComboBox(fieldControls, "poles");
        if (polesCombo != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> updateFullMarking());
        }

        ComboBox<String> climateCombo = CardFieldConfigurator.getComboBox(fieldControls, "climateType");
        if (climateCombo != null) {
            climateCombo.valueProperty().addListener((obs, old, val) -> updateFullMarking());
        }

        updateFullMarking();
    }

    /**
     * Добавляет слушатель на текстовое поле
     */
    private void addTextFieldListener(String fieldName, Runnable callback) {
        TextField textField = CardFieldConfigurator.getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Обновляет полную маркировку на основе заполненных полей
     */
    private void updateFullMarking() {
        if (fullMarkingField == null) return;

        String series = getFieldValue("series");
        String motorType = getFieldValue("motorType");
        String poles = getFieldValue("poles");
        String climateType = getFieldValue("climateType");

        StringBuilder marking = new StringBuilder();
        if (series != null && !series.isEmpty()) {
            marking.append(series).append(" ");
        }
        if (motorType != null && !motorType.isEmpty()) {
            marking.append(motorType);
        }
        if (poles != null && !poles.isEmpty()) {
            marking.append(poles);
        }
        if (climateType != null && !climateType.isEmpty()) {
            marking.append(" ").append(climateType);
        }

        String newMarking = marking.toString().trim();
        String currentMarking = fullMarkingField.getText();

        // Обновляем только если пользователь не редактировал поле вручную
        if (currentMarking == null || currentMarking.isEmpty() ||
                currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    /**
     * Получает значение поля по имени
     */
    private String getFieldValue(String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control == null) return "";

        if (control instanceof TextField) {
            return ((TextField) control).getText().trim();
        } else if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }
}

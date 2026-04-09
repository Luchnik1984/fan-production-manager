package com.fanproduction.gui.configurator;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.util.Map;

/**
 * Настройка специальных полей для карточки мотор-колеса.
 */
public class MotorWheelCardConfigurator implements CardFieldConfigurator {

    private TextField ratedSpeedField;

    @Override
    public void setupFields(Map<String, Control> fieldControls, boolean existingCardExists) {
        // Сохраняем ссылки на специальные поля
        ratedSpeedField = CardFieldConfigurator.getTextField(fieldControls, "ratedSpeedRpm");

        // Автоматическое заполнение напряжения из кода напряжения
        setupVoltageAutoFill(fieldControls);

        // Автоматический расчёт номинальной скорости
        setupRatedSpeedCalculation(fieldControls);
    }

    /**
     * Автоматическое заполнение напряжения из кода напряжения
     */
    private void setupVoltageAutoFill(Map<String, Control> fieldControls) {
        ComboBox<String> voltageCodeCombo = CardFieldConfigurator.getComboBox(fieldControls, "voltageCode");
        TextField voltageField = CardFieldConfigurator.getTextField(fieldControls, "voltage");

        if (voltageCodeCombo != null && voltageField != null) {
            voltageCodeCombo.valueProperty().addListener((obs, old, val) -> {
                if ("E".equals(val)) {
                    voltageField.setText("220");
                } else if ("D".equals(val)) {
                    voltageField.setText("380");
                } else {
                    voltageField.setText("");
                }
            });

            // Устанавливаем начальное значение
            String initialCode = voltageCodeCombo.getValue();
            if ("E".equals(initialCode)) {
                voltageField.setText("220");
            } else if ("D".equals(initialCode)) {
                voltageField.setText("380");
            }
        }
    }

    /**
     * Настройка автоматического расчёта номинальной скорости
     */
    private void setupRatedSpeedCalculation(Map<String, Control> fieldControls) {
        ComboBox<String> polesCombo = CardFieldConfigurator.getComboBox(fieldControls, "poles");
        if (polesCombo != null && ratedSpeedField != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> updateRatedSpeed(polesCombo));
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
}

package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class MotorWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";
    private String initialManufacturerMarking = "";

    /**
     * Проверяет, заполнены ли обязательные поля для мотор-колеса
     */
    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {

        // Проверяем, что маркировка производителя не пустая
        String manufacturerMarking = (String) fields.get("manufacturerMarking");
        if (manufacturerMarking == null || manufacturerMarking.isEmpty()) {
            showValidationError("""
                    Поле 'Маркировка производителя' обязательно для заполнения.
                    Пример: RE-280F-AC0E или DYF4D-280-QW1a""");
            return false;
        }
        // Проверяем fullMarking через общий метод
        return validateFullMarking(fields);
    }

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // 1. Автозаполнение напряжения из кода напряжения
        setupVoltageAutoFill(fieldControls);

        // 2. Расчёт номинальной скорости из полюсов
        setupRatedSpeedCalculation(fieldControls, () -> {});

        // 3. Формирование полной маркировки из маркировки производителя
        setupFullMarkingGeneration(fieldControls, existingCardExists);

        // 4. Автоматическое заполнение наименования для новых карточек
        autoFillName(fieldControls, "Мотор-колесо", existingCardExists);

        // 5. ПРИНУДИТЕЛЬНАЯ СИНХРОНИЗАЦИЯ ПРИ ЗАГРУЗКЕ ДАННЫХ
        if (existingCardExists) {
            // Запоминаем начальное значение manufacturerMarking
            initialManufacturerMarking = getFieldValue(fieldControls, "manufacturerMarking");

            TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
            if (fullMarkingField != null && !fullMarkingField.getText().isEmpty()) {
                lastAutoMarking = fullMarkingField.getText();
            }
        }
    }

    /**
     * Автозаполнение напряжения из кода напряжения
     * E → 220В, D → 380В
     */
    private void setupVoltageAutoFill(Map<String, Node> fieldControls) {
        ComboBox<String> voltageCodeCombo = getComboBox(fieldControls, "voltageCode");
        TextField voltageField = getTextField(fieldControls, "voltage");

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

            String initialCode = voltageCodeCombo.getValue();
            if ("E".equals(initialCode)) {
                voltageField.setText("220");
            } else if ("D".equals(initialCode)) {
                voltageField.setText("380");
            }
        }
    }

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls, boolean existingCardExists) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        // Слушаем изменение маркировки производителя
        addTextFieldListener(fieldControls, "manufacturerMarking", () -> updateFullMarking(fieldControls));

        // Вызываем updateFullMarking() только для новой карточки
        if (!existingCardExists) {
            updateFullMarking(fieldControls);
        }
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String manufacturerMarking = getFieldValue(fieldControls, "manufacturerMarking");
        String currentMarking = fullMarkingField.getText();

        // Если manufacturerMarking не изменился — не обновляем
        if (manufacturerMarking.equals(initialManufacturerMarking) &&
                currentMarking != null && !currentMarking.isEmpty()) {
            return;
        }

        // Обновляем только если поле пустое ИЛИ содержит последнее автоматическое значение
        if (currentMarking == null || currentMarking.isEmpty() ||
                currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(manufacturerMarking);
            lastAutoMarking = manufacturerMarking;
            // Обновляем начальное значение после изменения
            initialManufacturerMarking = manufacturerMarking;
        }
    }
}

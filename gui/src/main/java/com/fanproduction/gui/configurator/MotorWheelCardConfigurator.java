package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class MotorWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";

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
        setupFullMarkingGeneration(fieldControls);

        // 4. Автоматическое заполнение наименования для новых карточек
        autoFillName(fieldControls, "Мотор-колесо", existingCardExists);
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

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        addTextFieldListener(fieldControls, "manufacturerMarking", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String manufacturerMarking = getFieldValue(fieldControls, "manufacturerMarking");
        String currentMarking = fullMarkingField.getText();

        // Формируем новую маркировку (она равна маркировке производителя)
        String newMarking = manufacturerMarking;

        // Обновляем только если поле ещё не редактировалось вручную
        if (currentMarking == null || currentMarking.isEmpty() ||
                currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
    }

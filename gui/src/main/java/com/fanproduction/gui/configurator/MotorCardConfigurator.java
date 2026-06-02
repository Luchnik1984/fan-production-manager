package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.Map;

/**
 * Настройка специальных полей для карточки электродвигателя.
 */
public class MotorCardConfigurator implements CardFieldConfigurator {

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

        // Настройка автоматического расчёта номинальной скорости
        setupRatedSpeedCalculation(fieldControls, () -> updateFullMarking(fieldControls));

        // Настройка автоматического формирования полной маркировки
        setupFullMarkingGeneration(fieldControls);

        // Автоматическое заполнение наименования
        autoFillName(fieldControls, "Электродвигатель", existingCardExists);
    }

    /**
     * Настройка автоматического формирования полной маркировки
     */
    private void setupFullMarkingGeneration(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        // Добавляем слушатели на поля, влияющие на маркировку
        addTextFieldListener(fieldControls, "series", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "motorType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "climateType", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "mountingType", () -> updateFullMarking(fieldControls));

        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));

        addComboBoxListener(fieldControls, "poles", () -> updateFullMarking(fieldControls));

        updateFullMarking(fieldControls);
    }

    /**
     * Обновляет полную маркировку на основе заполненных полей
     */
    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        String series = getFieldValue(fieldControls, "series");
        String motorType = getFieldValue(fieldControls, "motorType");
        String poles = getFieldValue(fieldControls, "poles");
        String climateType = getFieldValue(fieldControls, "climateType");
        String mountingType = getFieldValue(fieldControls, "mountingType");

        boolean isGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue(fieldControls, "fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue(fieldControls, "explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder();

        if (!series.isEmpty()) {
            fullMarking.append(series).append(" ");
        }

        if (isFireproof && !fireproofMarking.isEmpty()) {
            fullMarking.append(fireproofMarking).append(" ");
        } else if (isExplosionProof && !explosionMarking.isEmpty()) {
            fullMarking.append(explosionMarking).append(" ");
        }

        if (!motorType.isEmpty()) {
            fullMarking.append(motorType);
        }

        if (!poles.isEmpty()) {
            fullMarking.append(poles);
        }

        if (!mountingType.isEmpty()) {
            fullMarking.append(" ").append(mountingType);
        }

        if (!climateType.isEmpty()) {
            fullMarking.append(" ").append(climateType);
        }

        String newMarking = fullMarking.toString().trim();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }
}
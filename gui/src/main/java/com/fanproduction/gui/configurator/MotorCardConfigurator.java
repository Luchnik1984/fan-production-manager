package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Настройка специальных полей для карточки электродвигателя.
 */
public class MotorCardConfigurator implements CardFieldConfigurator {

    private String lastAutoMarking = "";
    private Map<String, String> initialValues = new HashMap<>();
    private boolean initialGeneralPurpose = true;
    private boolean initialFireproof = false;
    private boolean initialExplosionProof = false;

    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {

        // Проверяем, заполнены ли обязательные поля для формирования fullMarking
        String series = (String) fields.get("series");
        String motorType = (String) fields.get("motorType");
        String poles = (String) fields.get("poles");
        String mountingType = (String) fields.get("mountingType");

        if (series == null || series.isEmpty() ||
                motorType == null || motorType.isEmpty() ||
                poles == null || poles.isEmpty() ||
                mountingType == null || mountingType.isEmpty()) {
            showValidationError(
                    """
                            Для формирования полной маркировки необходимо заполнить:
                            - Серия
                            - Тип двигателя
                            - Количество полюсов
                            - Исполнение по монтажу""");
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

        // Условная видимость (огнестойкость/взрывозащита) — с fieldHints
        setupConditionalVisibility(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));

        // Взаимоисключающие галочки
        setupExclusiveSelection(fieldControls, () -> updateFullMarking(fieldControls));

        // Настройка автоматического расчёта номинальной скорости
        setupRatedSpeedCalculation(fieldControls, () -> updateFullMarking(fieldControls));

        // Настройка автоматического формирования полной маркировки
        setupFullMarkingGeneration(fieldControls, existingCardExists);

        // Автоматическое заполнение наименования
        autoFillName(fieldControls, "Электродвигатель", existingCardExists);

        // Синхронизация при загрузке данных
        if (existingCardExists) {
            storeInitialValues(fieldControls);

            TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
            if (fullMarkingField != null && !fullMarkingField.getText().isEmpty()) {
                lastAutoMarking = fullMarkingField.getText();
            }
        }
    }

    /**
     * Настройка автоматического формирования полной маркировки
     */
    private void setupFullMarkingGeneration(Map<String, Node> fieldControls,boolean existingCardExists) {
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

        // Вызываем updateFullMarking() только для новой карточки
        if (!existingCardExists) {
            updateFullMarking(fieldControls);
        }
    }

    /**
     * Обновляет полную маркировку на основе заполненных полей
     */
    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        // Проверяем, изменились ли поля
        String currentSeries = getFieldValue(fieldControls, "series");
        String currentMotorType = getFieldValue(fieldControls, "motorType");
        String currentPoles = getFieldValue(fieldControls, "poles");
        String currentMountingType = getFieldValue(fieldControls, "mountingType");
        String currentClimateType = getFieldValue(fieldControls, "climateType");
        String currentFireproofMarking = getFieldValue(fieldControls, "fireproofMarking");
        String currentExplosionMarking = getFieldValue(fieldControls, "explosionMarking");

        boolean isGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        boolean isFireproof = isSelected(fieldControls, "fireproof");
        boolean isExplosionProof = isSelected(fieldControls, "explosionProof");

        // Проверяем, изменилось ли что-то
        String[] fieldsToCheck = {"series", "motorType", "poles", "mountingType",
                "climateType", "fireproofMarking", "explosionMarking"};
        if (!hasAnyFieldChanged(fieldControls, initialValues, fieldsToCheck)) {
            // Проверяем галочки
            if (initialGeneralPurpose == isGeneralPurpose &&
                    initialFireproof == isFireproof &&
                    initialExplosionProof == isExplosionProof) {
                return; // Ничего не изменилось
            }
        }

        // Формируем новую маркировку
        String newMarking = buildFullMarking(currentSeries, currentMotorType, currentPoles,
                currentMountingType, currentClimateType,
                isGeneralPurpose, isFireproof, isFireproof ? currentFireproofMarking : "",
                isExplosionProof ? currentExplosionMarking : "");

        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() ||
                currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
            storeInitialValues(fieldControls);
        }
    }

    private String buildFullMarking(String series, String motorType, String poles,
                                    String mountingType, String climateType,
                                    boolean isGeneralPurpose, boolean isFireproof,
                                    String fireproofMarking, String explosionMarking) {
        StringBuilder fullMarking = new StringBuilder();

        if (!series.isEmpty()) {
            fullMarking.append(series).append(" ");
        }

        if (isFireproof && !fireproofMarking.isEmpty()) {
            fullMarking.append(fireproofMarking).append(" ");
        } else if (!isGeneralPurpose && !explosionMarking.isEmpty()) {
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

        return fullMarking.toString().trim();
    }

    /**
     * Запоминает начальные значения всех полей
     */
    private void storeInitialValues(Map<String, Node> fieldControls) {
        String[] fieldNames = {"series", "motorType", "climateType", "mountingType",
                "fireproofMarking", "explosionMarking"};
        initialValues = new HashMap<>();
        for (String fieldName : fieldNames) {
            initialValues.put(fieldName, getFieldValue(fieldControls, fieldName));
        }

        // Запоминаем начальные значения полюсов
        String poles = getFieldValue(fieldControls, "poles");
        initialValues.put("poles", poles);

        initialGeneralPurpose = isSelected(fieldControls, "generalPurpose");
        initialFireproof = isSelected(fieldControls, "fireproof");
        initialExplosionProof = isSelected(fieldControls, "explosionProof");
    }
}
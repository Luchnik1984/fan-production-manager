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
    private String initialExecutionMarking = "";

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

        // ========== 1. НАСТРОЙКА ИСПОЛНЕНИЙ (ОГНЕСТОЙКОСТЬ/ВЗРЫВОЗАЩИТА) ==========
        setupExecutionMarking(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));

        // ========== 2. НАСТРОЙКА РАСЧЁТА НОМИНАЛЬНОЙ СКОРОСТИ ==========
        setupRatedSpeedCalculation(fieldControls, () -> updateFullMarking(fieldControls));

        // ========== 3. НАСТРОЙКА ФОРМИРОВАНИЯ ПОЛНОЙ МАРКИРОВКИ ==========
        setupFullMarkingGeneration(fieldControls, existingCardExists);

        // ========== 4. АВТОЗАПОЛНЕНИЕ НАИМЕНОВАНИЯ ==========
        autoFillName(fieldControls, "Электродвигатель", existingCardExists);

        // ========== 5. СИНХРОНИЗАЦИЯ ПРИ ЗАГРУЗКЕ ДАННЫХ ==========
        if (existingCardExists) {
            storeInitialValues(fieldControls);

            TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
            if (fullMarkingField != null && !fullMarkingField.getText().isEmpty()) {
                lastAutoMarking = fullMarkingField.getText();
            }
        }
    }

    // ==========================================================
    // МЕТОДЫ ДЛЯ РАБОТЫ С FULL_MARKING
    // ==========================================================

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
        addTextFieldListener(fieldControls, "mountingType", () -> updateFullMarking(fieldControls));
        addComboBoxListener(fieldControls, "poles", () -> updateFullMarking(fieldControls));

        // Слушатели на поля исполнений
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));

        // Слушатели на галочки исполнений
        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));


        // Вызываем updateFullMarking() только для новой карточки
        if (!existingCardExists) {
            updateFullMarking(fieldControls);
        }
    }

    @Override
    public void refreshFullMarking(Map<String, Node> fieldControls) {
        updateFullMarking(fieldControls);
    }

    /**
     * Обновляет полную маркировку с защитой от перезаписи
     */
    @Override
    public void forceSetFullMarking(Map<String, Node> fieldControls) {
        // 1. Сбрасываем защиту
        lastAutoMarking = "";
        // 2. Обновляем маркировку
        updateFullMarking(fieldControls);
    }

    /**
     * Обновляет полную маркировку на основе заполненных полей
     */
    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        // ПОЛУЧАЕМ ТЕКУЩИЕ ЗНАЧЕНИЯ
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

        // ПРОВЕРЯЕМ, ИЗМЕНИЛОСЬ ЛИ ЧТО-ТО
        String[] fieldsToCheck = getFieldsToCheckForFullMarking();
        boolean fieldsChanged = hasAnyFieldChanged(fieldControls, initialValues, fieldsToCheck);

        // Дополнительно проверяем галочки исполнений
        if (!fieldsChanged) {
            if (initialGeneralPurpose == isGeneralPurpose &&
                    initialFireproof == isFireproof &&
                    initialExplosionProof == isExplosionProof) {
                return; // Ничего не изменилось
            }
        }

        // ФОРМИРУЕМ НОВУЮ МАРКИРОВКУ
        String newMarking = buildFullMarking(
                currentSeries,
                currentMotorType,
                currentPoles,
                currentMountingType,
                currentClimateType,
                isGeneralPurpose,
                isFireproof,
                isFireproof ? currentFireproofMarking : "",
                isExplosionProof ? currentExplosionMarking : "");

        // ОБНОВЛЯЕМ ПОЛЕ (ТОЛЬКО ЕСЛИ ОНО НЕ БЫЛО ИЗМЕНЕНО ВРУЧНУЮ)
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
            fullMarking.append(series);
        }

        // Добавляем маркировку исполнения (если есть)
        if (isFireproof && !fireproofMarking.isEmpty()) {
            if (!fullMarking.isEmpty()) fullMarking.append(" ");
            fullMarking.append(fireproofMarking);
        } else if (!isGeneralPurpose && !explosionMarking.isEmpty()) {
            if (!fullMarking.isEmpty()) fullMarking.append(" ");
            fullMarking.append(explosionMarking);
        }

        if (!motorType.isEmpty()) {
            if (!fullMarking.isEmpty()) fullMarking.append(" ");
            fullMarking.append(motorType);
        }

        if (!poles.isEmpty()) {
            fullMarking.append(poles);
        }

        if (!mountingType.isEmpty()) {
            if (!fullMarking.isEmpty()) fullMarking.append(" ");
            fullMarking.append(mountingType);
        }

        if (!climateType.isEmpty()) {
            if (!fullMarking.isEmpty()) fullMarking.append(" ");
            fullMarking.append(climateType);
        }

        return fullMarking.toString().trim();
    }

    /**
     * Возвращает список полей, которые нужно проверить для определения,
     * изменился ли fullMarking
     */
    private String[] getFieldsToCheckForFullMarking() {
        return new String[]{"series", "motorType", "poles", "mountingType",
                "climateType", "fireproofMarking", "explosionMarking", "fullMarking"};
    }


    /**
     * Запоминает начальные значения всех полей
     */
    public void storeInitialValues(Map<String, Node> fieldControls) {
        String[] fieldNames = {"series", "motorType", "climateType", "mountingType",
                "fireproofMarking", "explosionMarking", "fullMarking"};
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
        initialExecutionMarking = getExecutionMarking(fieldControls);
    }
}
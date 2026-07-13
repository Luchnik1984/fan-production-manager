package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.Map;

public class RadialWheelCardConfigurator extends BaseWheelCardConfigurator {

  protected String lastAutoWheelCode = "";

    @Override
    protected String[] getPartnerFields() {
        return new String[]{"marking"};
    }

    @Override
    protected String[] getOwnFields() {
        return new String[]{
                "series", "bladeType", "hubComponentId", "hubName",
                "bladeMod", "frontDiskMod", "wheelWidth", "bladeCount",
                "bladeLengthCoeff", "wheelCode", "wheelFormula"
        };
    }

    @Override
    protected String[] getFieldsToCheckForFullMarking(boolean isPartner) {
        if (isPartner) {
            return new String[]{"marking"};
        }
        return new String[]{"series", "size", "wheelFormula", "hubName"};
    }

    @Override
    protected String[] getInitializationFields() {
        return new String[]{"series", "size", "wheelFormula", "hubName", "marking"};
    }

    @Override
    protected String[] getWheelFormulaFields() {
        return new String[]{"bladeMod", "frontDiskMod", "wheelWidth", "bladeCount", "bladeLengthCoeff", "bladeType"};
    }

    @Override
    protected String[] getFullMarkingFields() {
        return new String[]{"marking", "series", "size", "wheelFormula", "hubName", "fireproofMarking", "explosionMarking", "generalPurpose", "fireproof", "explosionProof", "isPartnerWheel", "isOwnProduction"};
    }

    @Override
    protected String buildOwnFullMarking(Map<String, Node> fieldControls) {
        String series = getFieldValue(fieldControls, "series");
        String size = getFieldValue(fieldControls, "size");
        String executionMarking = getExecutionMarking(fieldControls);
        String wheelFormula = getFieldValue(fieldControls, "wheelFormula");
        String hubName = getFieldValue(fieldControls, "hubName");

        StringBuilder sb = new StringBuilder();
        appendIfNotEmpty(sb, series);
        appendWithSeparator(sb, formatSize(size));
        appendWithSeparator(sb, executionMarking);
        appendWithSeparator(sb, wheelFormula);
        appendWithSeparator(sb, hubName);
        return sb.toString();
//        // Вызываем вспомогательный метод с 5 параметрами
//        return buildOwnFullMarking(series, size, executionMarking, wheelFormula, hubName);
    }

    @Override
    protected void updateWheelFormula(Map<String, Node> fieldControls) {
        TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
        if (wheelFormulaField == null) return;

        String bladeTypeValue = getFieldValue(fieldControls, "bladeType");
        String bladeTypeMarking = getBladeTypeMarking(bladeTypeValue);
        String wheelCode = getFieldValue(fieldControls, "wheelCode");
        String frontDiskMod = getFieldValue(fieldControls, "frontDiskMod");
        String wheelWidthStr = getFieldValue(fieldControls, "wheelWidth");
        String bladeLengthCoeff = getFieldValue(fieldControls, "bladeLengthCoeff");
        String bladeCount = getFieldValue(fieldControls, "bladeCount");

        if (bladeTypeMarking.isEmpty() && wheelCode.isEmpty() && frontDiskMod.isEmpty() &&
                wheelWidthStr.isEmpty() && bladeCount.isEmpty() && bladeLengthCoeff.isEmpty()) {
            if (wheelFormulaField.getText().isEmpty()) return;
            wheelFormulaField.setText("");
            lastAutoWheelFormula = "";
            return;
        }

        Double wheelWidth = null;
        try {
            if (!wheelWidthStr.isEmpty()) {
                wheelWidth = Double.parseDouble(wheelWidthStr.replace(',', '.'));
            }
        } catch (NumberFormatException e) {
            // ignore
        }
        String formattedWheelWidth = formatWheelWidth(wheelWidth);

        StringBuilder formula = new StringBuilder();

        if (!bladeTypeMarking.isEmpty()) formula.append(bladeTypeMarking);
        if (!wheelCode.isEmpty()) {
            if (!formula.isEmpty()) formula.append(".");
            formula.append(wheelCode);
        }
        if (!frontDiskMod.isEmpty()) {
            if (!formula.isEmpty()) formula.append("/");
            formula.append(frontDiskMod);
        }
        if (!formattedWheelWidth.isEmpty()) {
            if (!formula.isEmpty()) formula.append(".");
            formula.append(formattedWheelWidth);
        }
        if (!bladeCount.isEmpty()) {
            if (!formula.isEmpty()) formula.append("/");
            formula.append(bladeCount);
        }
        if (!bladeLengthCoeff.isEmpty()) {
            if (!formula.isEmpty()) formula.append("/");
            formula.append(bladeLengthCoeff);
        }

        String newFormula = formula.toString();
        String currentFormula = wheelFormulaField.getText();

        if (currentFormula == null || currentFormula.isEmpty() ||
                currentFormula.equals(lastAutoWheelFormula)) {
            wheelFormulaField.setText(newFormula);
            lastAutoWheelFormula = newFormula;
        }
    }

    @Override
    protected void setupSpecificListeners(Map<String, Node> fieldControls, boolean existingCardExists) {
        // Специфичные слушатели для радиального колеса
        addTextFieldListener(fieldControls, "bladeMod", () -> updateWheelCode(fieldControls));
        // Синхронизация при загрузке
        if (existingCardExists) {
            TextField wheelCodeField = getTextField(fieldControls, "wheelCode");
            if (wheelCodeField != null && !wheelCodeField.getText().isEmpty()) {
                lastAutoWheelCode = wheelCodeField.getText();
            }
            TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
            if (wheelFormulaField != null && !wheelFormulaField.getText().isEmpty()) {
                lastAutoWheelFormula = wheelFormulaField.getText();
            }
        }
    }

    /**
     * КОД КОЛЕСА (из bladeMod)
     */
    private void setupWheelCodeGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "bladeMod", () -> updateWheelCode(fieldControls));
        updateWheelCode(fieldControls);
    }

    /**
     * Метод для обновления кода колеса.
     * Специфичный для радиального колеса.
     */
    private void updateWheelCode(Map<String, Node> fieldControls) {
        TextField wheelCodeField = getTextField(fieldControls, "wheelCode");
        if (wheelCodeField == null) return;

        String bladeMod = getFieldValue(fieldControls, "bladeMod");
        String currentValue = wheelCodeField.getText();

        if (currentValue == null || currentValue.isEmpty() ||
                currentValue.equals(lastAutoWheelCode) ||
                !currentValue.equals(bladeMod)) {
            wheelCodeField.setText(bladeMod);
            lastAutoWheelCode = bladeMod;
        }
    }

    private String getBladeTypeMarking(String bladeTypeValue) {
        if (bladeTypeValue == null) return "";
        return switch (bladeTypeValue) {
            case "V" -> "V";
            case "N" -> "N";
            case "RO" -> "RO";
            default -> "";
        };
    }

    private String formatWheelWidth(Double width) {
        if (width == null) return "";
        int intPart = (int) Math.floor(width);
        int fracPart = (int) Math.round((width - intPart) * 100);
        return String.format("%d%02d", intPart, fracPart);
    }

    // ==========================================================
    // УНИКАЛЬНЫЕ МЕТОДЫ ДЛЯ РАДИАЛЬНОГО КОЛЕСА
    // ==========================================================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // 1. Сначала настраиваем слушатели на галочки
        setupWheelTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // 2. Потом применяем начальную видимость
        setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);

        // 3. Остальная логика
        setupConditionalVisibility(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));
        setupExclusiveSelection(fieldControls, () -> updateFullMarking(fieldControls));

        setupWheelCodeGeneration(fieldControls);

        setupFireproofGeneration(fieldControls, fieldLabels, fieldHints);

        super.setupWheelFormulaGeneration(fieldControls);
        super.setupFullMarkingGeneration(fieldControls, existingCardExists);

        setupSpecificListeners(fieldControls, existingCardExists);

        autoFillName(fieldControls, "Колесо радиальное", existingCardExists);

        // 4. Синхронизация при загрузке данных (только для существующей карточки)
        if (!existingCardExists) {
            return;
        }

        // Запоминаем начальные значения всех полей
        storeInitialValues(fieldControls);

        // Запоминаем текущие значения автоматических полей из БД
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField != null) {
            String currentMarking = fullMarkingField.getText();
            if (currentMarking != null && !currentMarking.isEmpty()) {
                lastAutoFullMarking = currentMarking;
            }
        }

        TextField wheelCodeField = getTextField(fieldControls, "wheelCode");
        if (wheelCodeField != null) {
            String currentWheelCode = wheelCodeField.getText();
            if (currentWheelCode != null && !currentWheelCode.isEmpty()) {
                lastAutoWheelCode = currentWheelCode;
            }
        }

        TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
        if (wheelFormulaField != null) {
            String currentWheelFormula = wheelFormulaField.getText();
            if (currentWheelFormula != null && !currentWheelFormula.isEmpty()) {
                lastAutoWheelFormula = currentWheelFormula;
            }
        }
    }

    // =============== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ===============


    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {

        boolean isPartner = Boolean.TRUE.equals(fields.get("isPartnerWheel"));
        boolean isOwn = Boolean.TRUE.equals(fields.get("isOwnProduction"));

        if (!isPartner && !isOwn) {
            showValidationError("""
                    Необходимо выбрать тип колеса:
                    - Фирменное рабочее колесо
                    - Партнёрское рабочее колесо""");
            return false;
        }
        // Проверяем fullMarking через общий метод
        return validateFullMarking(fields);
    }

    private String buildFullMarking(boolean isPartner, boolean isOwn,
                                    String currentSeries, String currentSize,
                                    String currentExecutionMarking, String currentWheelFormula,
                                    String currentHubName, String currentMarkingValue) {

        if (isPartner) {
            return currentMarkingValue;
        }

        if (isOwn) {
            return buildOwnFullMarking(currentSeries, currentSize, currentExecutionMarking,
                    currentWheelFormula, currentHubName);
        }

        return "";
    }

    /**
     * Вспомогательный метод для построения fullMarking для фирменного колеса
     */
    private String buildOwnFullMarking(String series, String sizeStr, String executionMarking,
                                       String wheelFormula, String hubName) {
        StringBuilder sb = new StringBuilder();

        // Парсим размер
        Double size = null;
        try {
            if (sizeStr != null && !sizeStr.isEmpty()) {
                size = Double.parseDouble(sizeStr.replace(',', '.'));
            }
        } catch (NumberFormatException ignored) {
        }

        appendIfNotEmpty(sb, series);
        appendWithSeparator(sb, formatSize(size));
        appendWithSeparator(sb, executionMarking);
        appendWithSeparator(sb, wheelFormula);
        appendWithSeparator(sb, hubName);

        return sb.toString();
    }

   protected void updateFullMarking(Map<String, Node> fieldControls) {

        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");

        // Если ни одна галочка не выбрана — не трогаем fullMarking
        if (!isPartner && !isOwn) {
            return;
        }

        // Получаем текущие значения
        String currentSeries = getFieldValue(fieldControls, "series");
        String currentSize = getFieldValue(fieldControls, "size");
        String currentWheelFormula = getFieldValue(fieldControls, "wheelFormula");
        String currentHubName = getFieldValue(fieldControls, "hubName");
        String currentMarkingValue = getFieldValue(fieldControls, "marking");
        String currentExecutionMarking = getExecutionMarking(fieldControls);

        // Проверяем, изменились ли поля, определяющие fullMarking
        String[] fieldsToCheck = getFieldsToCheckForFullMarking(isPartner);
        // Проверяем стандартные поля
        boolean fieldsChanged = hasAnyFieldChanged(fieldControls, initialValues, fieldsToCheck);

        // Дополнительно проверяем executionMarking (если оно входит в fieldsToCheck)
        if (!fieldsChanged && isOwn) {
            String initialExecution = initialExecutionMarking != null ? initialExecutionMarking : "";
            if (!currentExecutionMarking.equals(initialExecution)) {
                fieldsChanged = true;
            }
        }

        if (!fieldsChanged) {
            return;
        }

        // Формируем новую полную маркировку
        String newFullMarking = buildFullMarking(isPartner, isOwn,
                currentSeries, currentSize, currentExecutionMarking,
                currentWheelFormula, currentHubName, currentMarkingValue);

        String existingFullMarking = fullMarkingField.getText();

        // Обновляем только если поле пустое ИЛИ содержит последнее автоматическое значение
        if (existingFullMarking == null || existingFullMarking.isEmpty() ||
                existingFullMarking.equals(lastAutoFullMarking)) {
            fullMarkingField.setText(newFullMarking);
            lastAutoFullMarking = newFullMarking;

            // Обновляем начальные значения после пересчёта
            storeInitialValues(fieldControls);
        }
    }

}

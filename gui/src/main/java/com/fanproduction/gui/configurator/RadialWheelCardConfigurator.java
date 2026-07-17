package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

public class RadialWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoWheelCode = "";
    private String lastAutoWheelFormula = "";
    private String lastAutoFullMarking = "";

    // Храним начальные значения полей при загрузке
    private Map<String, String> initialValues = new HashMap<>();
    private boolean initialIsPartner = false;
    private boolean initialIsOwn = false;
    private String initialExecutionMarking = "";

    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {

        boolean isPartner = Boolean.TRUE.equals(fields.get("isPartnerWheel"));
        boolean isOwn = Boolean.TRUE.equals(fields.get("isOwnProduction"));

        // ========== 1. ПРОВЕРКА: ВЫБРАН ТИП КОЛЕСА ==========
        if (!isPartner && !isOwn) {
            showValidationError("""
                Необходимо выбрать тип колеса:
                - Фирменное рабочее колесо
                - Партнёрское рабочее колесо""");
            return false;
        }

        // ========== 2. ПРОВЕРКА: ПАРТНЁРСКОЕ КОЛЕСО ==========
        if (isPartner) {
            String marking = (String) fields.get("marking");
            if (marking == null || marking.isEmpty()) {
                showValidationError("Для партнёрского колеса необходимо заполнить поле 'Маркировка производителя'.");
                return false;
            }
        }

        // ========== 3. ПРОВЕРКА: ФИРМЕННОЕ КОЛЕСО ==========
        if (isOwn) {
            // Проверяем основные обязательные поля
            String series = (String) fields.get("series");
            String bladeType = (String) fields.get("bladeType");
            Long hubComponentId = (Long) fields.get("hubComponentId");

            if (series == null || series.isEmpty()) {
                showValidationError("Для фирменного колеса необходимо заполнить поле 'Серия колеса'.");
                return false;
            }
            if (bladeType == null || bladeType.isEmpty()) {
                showValidationError("Для фирменного колеса необходимо выбрать 'Тип лопаток'.");
                return false;
            }
            if (hubComponentId == null) {
                showValidationError("Для фирменного колеса необходимо выбрать 'Ступицу'.");
                return false;
            }

            // ========== ПРОВЕРКА ПОЛЕЙ ДЛЯ ФОРМУЛЫ КОЛЕСА ==========
            if (!WheelFormulaValidator.validateRadialWheelFormula(fields, true)) {
                return false;
            }
        }

        // ========== 4. ПРОВЕРКА FULL_MARKING ==========
        return validateFullMarking(fields);
    }

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // 1. Настройка исполнений (ОГНЕСТОЙКОСТЬ/ВЗРЫВОЗАЩИТА)
        setupExecutionMarking(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));

        // 2. Взаимоисключение типа колеса (партнёрское/фирменное)
        setupWheelTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // 3. Управление видимостью полей
        setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);

        // 4. Генерация кода колеса
        setupWheelCodeGeneration(fieldControls);

        // 5. Генерация формулы колеса
        setupWheelFormulaGeneration(fieldControls);

        // 6. Генерация полной маркировки
        setupFullMarkingGeneration(fieldControls, existingCardExists);

        // 7. Автозаполнение наименования
        autoFillName(fieldControls, "Колесо радиальное", existingCardExists);

        // 8. Синхронизация при загрузке данных (только для существующей карточки)
        if (existingCardExists) {
            storeInitialValues(fieldControls);

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
    }

    /**
    * УПРАВЛЕНИЕ ВИДИМОСТЬЮ
    */
    private void setupVisibilityLogic(Map<String, Node> fieldControls,
                                      Map<String, Label> fieldLabels,
                                      Map<String, Label> fieldHints) {

        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");

        // Поля для партнёрского колеса
        setVisible(fieldControls, fieldLabels, fieldHints, "marking", isPartner);

        // Поля для фирменного колеса
        String[] ownFields = {
                "series", "bladeType", "hubComponentId", "hubName",
                "bladeMod", "frontDiskMod", "wheelWidth", "bladeCount",
                "bladeLengthCoeff", "wheelCode", "wheelFormula"
        };
        for (String fieldName : ownFields) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isOwn);
        }
    }

    /**
     * ВЗАИМОИСКЛЮЧАЮЩИЕ ГАЛОЧКИ ДЛЯ ТИПА КОЛЕСА
     */
    private void setupWheelTypeExclusiveSelection(Map<String, Node> fieldControls,
                                                  Map<String, Label> fieldLabels,
                                                  Map<String, Label> fieldHints) {
        CheckBox partnerCheck = getCheckBox(fieldControls, "isPartnerWheel");
        CheckBox ownCheck = getCheckBox(fieldControls, "isOwnProduction");

        if (partnerCheck != null) {
            partnerCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && ownCheck != null) {
                    ownCheck.setSelected(false);
                }
                // Если партнёрское снято — очищаем поля
                if (!val) {
                    clearPartnerFields(fieldControls);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls);
            });
        }

        if (ownCheck != null) {
            ownCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && partnerCheck != null) {
                    partnerCheck.setSelected(false);
                }
                // Если фирменное снято — очищаем поля
                if (!val) {
                    clearOwnFields(fieldControls);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls);
            });
        }
    }

    private void clearPartnerFields(Map<String, Node> fieldControls) {
        String[] fields = {"marking"};
        for (String fieldName : fields) {
            Node control = fieldControls.get(fieldName);
            if (control instanceof TextField) {
                ((TextField) control).clear();
            }
        }
    }

    private void clearOwnFields(Map<String, Node> fieldControls) {
        String[] fields = {
                "series", "bladeType", "hubComponentId", "hubName",
                "bladeMod", "frontDiskMod", "wheelWidth", "bladeCount",
                "bladeLengthCoeff", "wheelCode", "wheelFormula"
        };
        for (String fieldName : fields) {
            Node control = fieldControls.get(fieldName);
            if (control instanceof TextField) {
                ((TextField) control).clear();
            } else if (control instanceof ComboBox) {
                ((ComboBox<?>) control).setValue(null);
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

    /**
    * ФОРМУЛА КОЛЕСА
    */
    private void setupWheelFormulaGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "bladeMod", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "frontDiskMod", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "wheelWidth", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeLengthCoeff", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeCount", () -> updateWheelFormula(fieldControls));
        addComboBoxListener(fieldControls, "bladeType", () -> updateWheelFormula(fieldControls));
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

    private void updateWheelFormula(Map<String, Node> fieldControls) {
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

    // ==========================================================
    // МЕТОДЫ ДЛЯ РАБОТЫ С FULL_MARKING
    // ==========================================================

    /**
    * ПОЛНАЯ МАРКИРОВКА
    */

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls, boolean existingCardExists) {
        // Партнёрское колесо
        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls));

        // Фирменное колесо
        addTextFieldListener(fieldControls, "series", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "size", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "wheelFormula", () -> updateFullMarking(fieldControls));

        // Исполнение
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));

        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "isPartnerWheel", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "isOwnProduction", () -> updateFullMarking(fieldControls));

        if (!existingCardExists) {
            updateFullMarking(fieldControls);
        }
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

    private void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(value);
        }
    }

    private void appendWithSeparator(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            if (!sb.isEmpty()) {
                sb.append("-");
            }
            sb.append(value);
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
        lastAutoFullMarking = "";
        // 2. Обновляем маркировку
        updateFullMarking(fieldControls);
    }

    private void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");

        if (!isPartner && !isOwn) {
            return;
        }

        String currentSeries = getFieldValue(fieldControls, "series");
        String currentSize = getFieldValue(fieldControls, "size");
        String currentWheelFormula = getFieldValue(fieldControls, "wheelFormula");
        String currentHubName = getFieldValue(fieldControls, "hubName");
        String currentMarkingValue = getFieldValue(fieldControls, "marking");

        // ========== ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ИЗ ИНТЕРФЕЙСА ==========
        String currentExecutionMarking = getExecutionMarking(fieldControls);

        // Проверяем, изменились ли поля
        String[] fieldsToCheck = getFieldsToCheckForFullMarking(isPartner);
        boolean fieldsChanged = hasAnyFieldChanged(fieldControls, initialValues, fieldsToCheck);

        // Дополнительно проверяем executionMarking
        if (!fieldsChanged && isOwn) {
            String initialExecution = initialExecutionMarking != null ? initialExecutionMarking : "";
            if (!currentExecutionMarking.equals(initialExecution)) {
                fieldsChanged = true;
            }
        }

        if (!fieldsChanged) {
            return;
        }

        String newFullMarking = buildFullMarking(isPartner, isOwn,
                currentSeries, currentSize, currentExecutionMarking,
                currentWheelFormula, currentHubName, currentMarkingValue);

        String existingFullMarking = fullMarkingField.getText();

        if (existingFullMarking == null || existingFullMarking.isEmpty() ||
                existingFullMarking.equals(lastAutoFullMarking)) {
            fullMarkingField.setText(newFullMarking);
            lastAutoFullMarking = newFullMarking;
            storeInitialValues(fieldControls);
        }
    }

    /**
     * Возвращает список полей, которые нужно проверить для определения,
     * изменился ли fullMarking
     */
    private String[] getFieldsToCheckForFullMarking(boolean isPartner) {
        if (isPartner) {
            // Для партнёрского колеса: только marking
            return new String[]{"marking", "fullMarking"};
        } else {
            // Для фирменного колеса: series, size, wheelFormula, executionMarking
            return new String[]{"series", "size", "wheelFormula","hubName", "executionMarking", "fullMarking"};
        }
    }

    /**
     * Запоминает начальные значения всех полей
     */
   public void storeInitialValues(Map<String, Node> fieldControls) {
        String[] fieldNames = {"series", "size", "wheelFormula", "hubName", "marking",  "fullMarking"};
        initialValues = new HashMap<>();
        for (String fieldName : fieldNames) {
            initialValues.put(fieldName, getFieldValue(fieldControls, fieldName));
        }
        initialIsPartner = isSelected(fieldControls, "isPartnerWheel");
        initialIsOwn = isSelected(fieldControls, "isOwnProduction");
        initialExecutionMarking = getExecutionMarking(fieldControls);
    }

    /**
     * Форматирует размер: убирает .0 если число целое
     */
    private String formatSize(Double size) {
        if (size == null) return "";
        if (size == Math.floor(size)) {
            return String.valueOf(size.intValue());
        }
        return String.valueOf(size);
    }

}

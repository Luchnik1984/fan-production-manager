package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигуратор для карточки осевого колеса.
 * Особенности:
 * - Взаимоисключающие галочки: "Партнёрское" / "Фирменное"
 * - Для фирменного: "Сборное из компонентов" / "Сварное из материалов"
 * - Расчёт диаметра колеса: size * (100 - trimCoefficient)
 * - Формирование формулы колеса из выбранных компонентов/полей
 * - Формирование полной маркировки по двум формулам
 * - Защита автоматических полей от перезаписи
 */
public class AxialWheelCardConfigurator implements CardFieldConfigurator {

    private String lastAutoFullMarking = "";
    private String lastAutoWheelFormula = "";
    private String lastAutoWheelDiameter = "";

    // Начальные значения для сравнения (ТОЛЬКО поля, влияющие на fullMarking)
    private Map<String, String> initialValues = new HashMap<>();
    private boolean initialIsPartner = false;
    private boolean initialIsOwn = false;
    private boolean initialIsAssembled = false;
    private boolean initialIsWelded = false;
    private String initialExecutionMarking = "";

    // ==========================================================
    // ОСНОВНОЙ МЕТОД НАСТРОЙКИ
    // ==========================================================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // ========== 1. НАСТРОЙКА ИСПОЛНЕНИЙ (ОГНЕСТОЙКОСТЬ/ВЗРЫВОЗАЩИТА) ==========
        setupExecutionMarking(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));

        // ========== 2. ВЗАИМОИСКЛЮЧЕНИЕ ТИПА КОЛЕСА ==========
        setupWheelTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // ========== 3. ВЗАИМОИСКЛЮЧЕНИЕ ТИПА ИЗГОТОВЛЕНИЯ ==========
        setupAssemblyTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // ========== 4. УПРАВЛЕНИЕ ВИДИМОСТЬЮ ПОЛЕЙ ==========
        setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);

        // ========== 5. РАСЧЁТ ДИАМЕТРА КОЛЕСА ==========
        setupWheelDiameterCalculation(fieldControls);

        // ========== 6. ФОРМИРОВАНИЕ ФОРМУЛЫ КОЛЕСА ==========
        setupWheelFormulaGeneration(fieldControls);

        // ========== 7. ФОРМИРОВАНИЕ ПОЛНОЙ МАРКИРОВКИ ==========
        setupFullMarkingGeneration(fieldControls, existingCardExists);

        // ========== 8. АВТОЗАПОЛНЕНИЕ НАИМЕНОВАНИЯ ==========
        autoFillName(fieldControls, "Колесо осевое", existingCardExists);

        // ========== 9. СИНХРОНИЗАЦИЯ ПРИ ЗАГРУЗКЕ ДАННЫХ ==========
        if (existingCardExists) {
            storeInitialValues(fieldControls);

            TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
            if (fullMarkingField != null && !fullMarkingField.getText().isEmpty()) {
                lastAutoFullMarking = fullMarkingField.getText();
            }

            TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
            if (wheelFormulaField != null && !wheelFormulaField.getText().isEmpty()) {
                lastAutoWheelFormula = wheelFormulaField.getText();
            }

            TextField wheelDiameterField = getTextField(fieldControls, "wheelDiameter");
            if (wheelDiameterField != null && !wheelDiameterField.getText().isEmpty()) {
                lastAutoWheelDiameter = wheelDiameterField.getText();
            }
        }
    }

    // ==========================================================
    // 1. ВЗАИМОИСКЛЮЧЕНИЕ "ПАРТНЁРСКОЕ / ФИРМЕННОЕ"
    // ==========================================================

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
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls);
                updateWheelFormula(fieldControls);
            });
        }

        if (ownCheck != null) {
            ownCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && partnerCheck != null) {
                    partnerCheck.setSelected(false);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls);
                updateWheelFormula(fieldControls);
            });
        }
    }

    // ==========================================================
    // 2. ВЗАИМОИСКЛЮЧЕНИЕ "СБОРНОЕ / СВАРНОЕ"
    // ==========================================================

    private void setupAssemblyTypeExclusiveSelection(Map<String, Node> fieldControls,
                                                     Map<String, Label> fieldLabels,
                                                     Map<String, Label> fieldHints) {
        CheckBox assembledCheck = getCheckBox(fieldControls, "isAssembledFromComponents");
        CheckBox weldedCheck = getCheckBox(fieldControls, "isWeldedFromMaterials");

        if (assembledCheck != null) {
            assembledCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && weldedCheck != null) {
                    weldedCheck.setSelected(false);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateWheelFormula(fieldControls);
                updateFullMarking(fieldControls);
            });
        }

        if (weldedCheck != null) {
            weldedCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && assembledCheck != null) {
                    assembledCheck.setSelected(false);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateWheelFormula(fieldControls);
                updateFullMarking(fieldControls);
            });
        }
    }

    // ==========================================================
    // 3. УПРАВЛЕНИЕ ВИДИМОСТЬЮ ПОЛЕЙ
    // ==========================================================

    private void setupVisibilityLogic(Map<String, Node> fieldControls,
                                      Map<String, Label> fieldLabels,
                                      Map<String, Label> fieldHints) {
        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");
        boolean isAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        boolean isWelded = isSelected(fieldControls, "isWeldedFromMaterials");

        // Поля для партнёрского колеса
        setVisible(fieldControls, fieldLabels, fieldHints, "marking", isPartner);

        // Поля для фирменного колеса (общие)
        setVisible(fieldControls, fieldLabels, fieldHints, "series", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "isAssembledFromComponents", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "isWeldedFromMaterials", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "maxBladeCount", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeCount", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeAngle", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "hubComponentId", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "hubName", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelFormula", isOwn);

        // Поля для сборного колеса
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelHubComponentId", isOwn && isAssembled);
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelHubName", isOwn && isAssembled);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeComponentId", isOwn && isAssembled);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeName", isOwn && isAssembled);

        // Поля для сварного колеса
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelHubType", isOwn && isWelded);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeType", isOwn && isWelded);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeMaterial", isOwn && isWelded);
    }

    // ==========================================================
    // 4. РАСЧЁТ ДИАМЕТРА КОЛЕСА
    // ==========================================================

    private void setupWheelDiameterCalculation(Map<String, Node> fieldControls) {
        TextField sizeField = getTextField(fieldControls, "size");
        TextField trimField = getTextField(fieldControls, "trimCoefficient");
        TextField diameterField = getTextField(fieldControls, "wheelDiameter");

        if (sizeField != null && trimField != null && diameterField != null) {
            Runnable calculate = () -> {
                try {
                    String sizeText = sizeField.getText().replace(',', '.');
                    String trimText = trimField.getText().replace(',', '.');

                    if (!sizeText.isEmpty()) {
                        double size = Double.parseDouble(sizeText);
                        double trim = trimText.isEmpty() ? 0 : Double.parseDouble(trimText);
                        double diameter = size * (100 - trim);
                        long rounded = Math.round(diameter);
                        String newValue = String.valueOf(rounded);
                        String currentValue = diameterField.getText();

                        // Обновляем только если поле не было изменено вручную
                        if (currentValue == null || currentValue.isEmpty() ||
                                currentValue.equals(lastAutoWheelDiameter) ||
                                !currentValue.equals(newValue)) {
                            diameterField.setText(newValue);
                            lastAutoWheelDiameter = newValue;
                        }
                    } else {
                        diameterField.setText("");
                    }
                } catch (NumberFormatException e) {
                    diameterField.setText("");
                }
            };

            sizeField.textProperty().addListener((obs, old, val) -> calculate.run());
            trimField.textProperty().addListener((obs, old, val) -> calculate.run());
            calculate.run();
        }
    }

    // ==========================================================
    // 5. ФОРМИРОВАНИЕ ФОРМУЛЫ КОЛЕСА
    // ==========================================================

    private void setupWheelFormulaGeneration(Map<String, Node> fieldControls) {
        addTextFieldListener(fieldControls, "wheelDiameter", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeCount", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "maxBladeCount", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeAngle", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeMaterial", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeType", () -> updateWheelFormula(fieldControls));
        addCheckBoxListener(fieldControls, "isAssembledFromComponents", () -> updateWheelFormula(fieldControls));
        addCheckBoxListener(fieldControls, "isWeldedFromMaterials", () -> updateWheelFormula(fieldControls));

        updateWheelFormula(fieldControls);
    }

    private void updateWheelFormula(Map<String, Node> fieldControls) {
        TextField formulaField = getTextField(fieldControls, "wheelFormula");
        if (formulaField == null) return;

        boolean isAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        boolean isWelded = isSelected(fieldControls, "isWeldedFromMaterials");

        // Если ни одна галочка не выбрана — не трогаем формулу
        if (!isAssembled && !isWelded) {
            return;
        }

        String diameter = getFieldValue(fieldControls, "wheelDiameter");
        String bladeCount = getFieldValue(fieldControls, "bladeCount");
        String maxBladeCount = getFieldValue(fieldControls, "maxBladeCount");
        String bladeAngle = getFieldValue(fieldControls, "bladeAngle");
        String bladeMaterial = getFieldValue(fieldControls, "bladeMaterial");

        String bladeIdentifier;
        if (isAssembled) {
            // Для сборного — используем designation компонента
            bladeIdentifier = getFieldValue(fieldControls, "bladeName");
        } else {
            // Для сварного — используем ручной ввод bladeType
            bladeIdentifier = getFieldValue(fieldControls, "bladeType");
        }

        if (diameter.isEmpty() || bladeCount.isEmpty() || maxBladeCount.isEmpty() ||
                bladeIdentifier.isEmpty() || bladeAngle.isEmpty() || bladeMaterial.isEmpty()) {
            return;
        }

        String newFormula = diameter + "/" + bladeCount + "-" + maxBladeCount +
                "/" + bladeIdentifier + "/" + bladeAngle + "/" + bladeMaterial;

        String currentFormula = formulaField.getText();

        // Обновляем только если поле пустое ИЛИ содержит последнее автоматическое значение
        if (currentFormula == null || currentFormula.isEmpty() ||
                currentFormula.equals(lastAutoWheelFormula)) {
            formulaField.setText(newFormula);
            lastAutoWheelFormula = newFormula;
        }
    }

    // ==========================================================
    // 6. МЕТОДЫ ДЛЯ РАБОТЫ С ПОЛНОЙ МАРКИРОВКОЙ
    // ==========================================================

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls, boolean existingCardExists) {
        // Партнёрское колесо
        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls));

        // Фирменное колесо
        addTextFieldListener(fieldControls, "series", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "size", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "wheelFormula", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "hubName", () -> updateFullMarking(fieldControls));

        // Исполнение
        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "isPartnerWheel", () -> updateFullMarking(fieldControls));
        addCheckBoxListener(fieldControls, "isOwnProduction", () -> updateFullMarking(fieldControls));

        // Маркировки исполнений (изменяются через helper)
        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls));

        // Вызываем updateFullMarking() только для новой карточки
        if (!existingCardExists) {
            updateFullMarking(fieldControls);
        }
    }

    @Override
    public void refreshFullMarking(Map<String, Node> fieldControls) {
        updateFullMarking(fieldControls);
    }

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

        // ========== ПРОВЕРКА ИЗМЕНЕНИЙ ==========
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
            return;  // Ничего не изменилось
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

        String formattedSize = formatSize(size);

        if (!series.isEmpty()) sb.append(series);
        if (!formattedSize.isEmpty()) {
            if (!sb.isEmpty()) sb.append("(");
            sb.append(formattedSize);
            if (!series.isEmpty()) sb.append(")");
        }
        if (!executionMarking.isEmpty()) {
            if (!sb.isEmpty()) sb.append("-");
            sb.append(executionMarking);
        }
        if (!wheelFormula.isEmpty()) {
            if (!sb.isEmpty()) sb.append("-");
            sb.append(wheelFormula);
        }
        if (!hubName.isEmpty()) {
            if (!sb.isEmpty()) sb.append("-");
            sb.append(hubName);
        }

        return sb.toString();
    }

    /**
     * Возвращает список полей для проверки изменения fullMarking
     */
    private String[] getFieldsToCheckForFullMarking(boolean isPartner) {
        if (isPartner) {
            return new String[]{"marking", "fullMarking"};
        } else {
            return new String[]{"series", "size", "wheelFormula", "hubName", "executionMarking", "fullMarking"};
        }
    }

    // ==========================================================
    // 7. РАБОТА С ФОРМАТОМ РАЗМЕРА
    // ==========================================================

    private String formatSize(Double size) {
        if (size == null) return "";
        if (size == Math.floor(size)) {
            return String.valueOf(size.intValue());
        }
        return String.valueOf(size);
    }

    private String formatSize(String sizeStr) {
        if (sizeStr == null || sizeStr.isEmpty()) return "";
        try {
            double size = Double.parseDouble(sizeStr.replace(',', '.'));
            if (size == Math.floor(size)) {
                return String.valueOf((int) size);
            }
            return String.valueOf(size);
        } catch (NumberFormatException e) {
            return sizeStr;
        }
    }

    // ==========================================================
    // 8. ВАЛИДАЦИЯ
    // ==========================================================

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
                - Партнёрское рабочее колесо
                - Фирменное рабочее колесо""");
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
            boolean isAssembled = Boolean.TRUE.equals(fields.get("isAssembledFromComponents"));
            boolean isWelded = Boolean.TRUE.equals(fields.get("isWeldedFromMaterials"));

            if (!isAssembled && !isWelded) {
                showValidationError("""
                    Для фирменного колеса необходимо выбрать тип изготовления:
                    - Сборное из компонентов
                    - Сварное из материалов""");
                return false;
            }

            // ========== ПРОВЕРКА ПОЛЕЙ ДЛЯ ФОРМУЛЫ ОСЕВОГО КОЛЕСА ==========
            if (!WheelFormulaValidator.validateAxialWheelFormula(fields, true)) {
                return false;
            }
        }

        // ========== 4. ПРОВЕРКА FULL_MARKING ==========
        return validateFullMarking(fields);
    }

    // ==========================================================
    // 9. ХРАНЕНИЕ НАЧАЛЬНЫХ ЗНАЧЕНИЙ
    // ==========================================================

    public void storeInitialValues(Map<String, Node> fieldControls) {
        // ========== ТОЛЬКО ПОЛЯ, ВЛИЯЮЩИЕ НА FULL_MARKING ==========
        String[] fieldNames = {"series", "size", "wheelFormula", "hubName", "marking", "fullMarking"};

        initialValues = new HashMap<>();
        for (String fieldName : fieldNames) {
            initialValues.put(fieldName, getFieldValue(fieldControls, fieldName));
        }

        initialIsPartner = isSelected(fieldControls, "isPartnerWheel");
        initialIsOwn = isSelected(fieldControls, "isOwnProduction");
        initialIsAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        initialIsWelded = isSelected(fieldControls, "isWeldedFromMaterials");
        initialExecutionMarking = getExecutionMarking(fieldControls);
    }

}
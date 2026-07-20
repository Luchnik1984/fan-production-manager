package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Конфигуратор для карточки осевого колеса.
 * Поддерживает партнёрские и фирменные колёса (сборные/сварные).
 */
public class AxialWheelCardConfigurator implements CardFieldConfigurator {

    // ===================== КОНСТАНТЫ ПОЛЕЙ =====================

    private static final List<String> PARTNER_FIELDS = List.of("marking");

    private static final List<String> OWN_COMMON_FIELDS = Arrays.asList(
            "series",
            "isAssembledFromComponents",
            "isWeldedFromMaterials",
            "maxBladeCount",
            "bladeCount",
            "bladeAngle",
            "hubComponentId",
            "hubName",
            "wheelFormula"
    );

    private static final List<String> ASSEMBLED_FIELDS = Arrays.asList(
            "wheelHubComponentId",
            "wheelHubName",
            "bladeComponentId",
            "bladeName"
    );

    private static final List<String> WELDED_FIELDS = Arrays.asList(
            "wheelHubType",
            "bladeType",
            "bladeMaterial"
    );

    // ===================== ПОЛЯ СОСТОЯНИЯ =====================

    private String lastAutoFullMarking = "";
    private String lastAutoWheelFormula = "";
    private String lastAutoWheelDiameter = "";

    private Map<String, String> initialValues = new HashMap<>();
    private boolean initialIsPartner = false;
    private boolean initialIsOwn = false;
    private boolean initialIsAssembled = false;
    private boolean initialIsWelded = false;
    private String initialExecutionMarking = "";

    // ===================== ВАЛИДАЦИЯ =====================

    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {
        boolean isPartner = Boolean.TRUE.equals(fields.get("isPartnerWheel"));
        boolean isOwn = Boolean.TRUE.equals(fields.get("isOwnProduction"));

        if (!isPartner && !isOwn) {
            showValidationError("""
                Необходимо выбрать тип колеса:
                - Партнёрское рабочее колесо
                - Фирменное рабочее колесо""");
            return false;
        }

        if (isPartner) {
            String marking = (String) fields.get("marking");
            if (marking == null || marking.isEmpty()) {
                showValidationError("Для партнёрского колеса необходимо заполнить поле 'Маркировка производителя'.");
                return false;
            }
        }

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

            // Проверка количества лопаток
            Integer bladeCount = (Integer) fields.get("bladeCount");
            Integer maxBladeCount = (Integer) fields.get("maxBladeCount");

            if (bladeCount == null) {
                showValidationError("Необходимо указать количество установленных лопаток.");
                return false;
            }
            if (bladeCount < 2) {
                showValidationError("Количество лопаток не может быть меньше двух.");
                return false;
            }
            if (maxBladeCount != null && bladeCount > maxBladeCount) {
                showValidationError("Количество лопаток не может превышать максимальное (" + maxBladeCount + ").");
                return false;
            }
        }

        return validateFullMarking(fields);
    }

    // ===================== НАСТРОЙКА ПОЛЕЙ =====================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // 1. Исполнения
        setupExecutionMarking(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls, false));

        // 2. Взаимоисключение типа колеса
        setupWheelTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // 3. Взаимоисключение типа изготовления
        setupAssemblyTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // 4. Видимость полей
        setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);

        // 5. Расчёт диаметра
        setupWheelDiameterCalculation(fieldControls);

        // 6. Формула колеса
        setupWheelFormulaGeneration(fieldControls);

        // 7. Полная маркировка
        setupFullMarkingGeneration(fieldControls, existingCardExists);

        // 8. Автозаполнение имени
        autoFillName(fieldControls, "Колесо осевое", existingCardExists);

        // 9. Синхронизация при загрузке
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

    // -------------------- Вспомогательные методы настройки --------------------

    private void setupWheelTypeExclusiveSelection(Map<String, Node> fieldControls,
                                                  Map<String, Label> fieldLabels,
                                                  Map<String, Label> fieldHints) {
        CheckBox partnerCheck = getCheckBox(fieldControls, "isPartnerWheel");
        CheckBox ownCheck = getCheckBox(fieldControls, "isOwnProduction");

        if (partnerCheck != null) {
            partnerCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && ownCheck != null) ownCheck.setSelected(false);
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls, false);
                updateWheelFormula(fieldControls);
            });
        }

        if (ownCheck != null) {
            ownCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && partnerCheck != null) partnerCheck.setSelected(false);
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls, false);
                updateWheelFormula(fieldControls);
            });
        }
    }

    private void setupAssemblyTypeExclusiveSelection(Map<String, Node> fieldControls,
                                                     Map<String, Label> fieldLabels,
                                                     Map<String, Label> fieldHints) {
        CheckBox assembledCheck = getCheckBox(fieldControls, "isAssembledFromComponents");
        CheckBox weldedCheck = getCheckBox(fieldControls, "isWeldedFromMaterials");

        if (assembledCheck != null) {
            assembledCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && weldedCheck != null) weldedCheck.setSelected(false);
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateWheelFormula(fieldControls);
                updateFullMarking(fieldControls, false);
            });
        }

        if (weldedCheck != null) {
            weldedCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && assembledCheck != null) assembledCheck.setSelected(false);
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateWheelFormula(fieldControls);
                updateFullMarking(fieldControls, false);
            });
        }
    }

    private void setupVisibilityLogic(Map<String, Node> fieldControls,
                                      Map<String, Label> fieldLabels,
                                      Map<String, Label> fieldHints) {
        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");
        boolean isAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        boolean isWelded = isSelected(fieldControls, "isWeldedFromMaterials");

        // Партнёрское
        for (String fieldName : PARTNER_FIELDS) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isPartner);
        }

        // Фирменное общее
        for (String fieldName : OWN_COMMON_FIELDS) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isOwn);
        }

        // Сборное
        for (String fieldName : ASSEMBLED_FIELDS) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isOwn && isAssembled);
        }

        // Сварное
        for (String fieldName : WELDED_FIELDS) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isOwn && isWelded);
        }
    }

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

                        if (currentValue == null || currentValue.isEmpty() ||
                                currentValue.equals(lastAutoWheelDiameter) || !currentValue.equals(newValue)) {
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

        if (!isAssembled && !isWelded) return;

        String diameter = getFieldValue(fieldControls, "wheelDiameter");
        String bladeCount = getFieldValue(fieldControls, "bladeCount");
        String maxBladeCount = getFieldValue(fieldControls, "maxBladeCount");
        String bladeAngle = getFieldValue(fieldControls, "bladeAngle");
        String bladeMaterial = getFieldValue(fieldControls, "bladeMaterial");

        String bladeIdentifier = isAssembled
                ? getFieldValue(fieldControls, "bladeName")
                : getFieldValue(fieldControls, "bladeType");

        if (diameter.isEmpty() || bladeCount.isEmpty() || maxBladeCount.isEmpty() ||
                bladeIdentifier.isEmpty() || bladeAngle.isEmpty() || bladeMaterial.isEmpty()) {
            return;
        }

        String newFormula = diameter + "/" + bladeCount + "-" + maxBladeCount +
                "/" + bladeIdentifier + "/" + bladeAngle + "/" + bladeMaterial;

        String currentFormula = formulaField.getText();

        if (currentFormula == null || currentFormula.isEmpty() ||
                currentFormula.equals(lastAutoWheelFormula)) {
            formulaField.setText(newFormula);
            lastAutoWheelFormula = newFormula;
        }
    }

    // -------------------- Полная маркировка --------------------

    private void setupFullMarkingGeneration(Map<String, Node> fieldControls, boolean existingCardExists) {
        addTextFieldListener(fieldControls, "marking", () -> updateFullMarking(fieldControls, false));
        addTextFieldListener(fieldControls, "series", () -> updateFullMarking(fieldControls, false));
        addTextFieldListener(fieldControls, "size", () -> updateFullMarking(fieldControls, false));
        addTextFieldListener(fieldControls, "wheelFormula", () -> updateFullMarking(fieldControls, false));
        addTextFieldListener(fieldControls, "hubName", () -> updateFullMarking(fieldControls, false));

        addCheckBoxListener(fieldControls, "generalPurpose", () -> updateFullMarking(fieldControls, false));
        addCheckBoxListener(fieldControls, "fireproof", () -> updateFullMarking(fieldControls, false));
        addCheckBoxListener(fieldControls, "explosionProof", () -> updateFullMarking(fieldControls, false));
        addCheckBoxListener(fieldControls, "isPartnerWheel", () -> updateFullMarking(fieldControls, false));
        addCheckBoxListener(fieldControls, "isOwnProduction", () -> updateFullMarking(fieldControls, false));

        addTextFieldListener(fieldControls, "fireproofMarking", () -> updateFullMarking(fieldControls, false));
        addTextFieldListener(fieldControls, "explosionMarking", () -> updateFullMarking(fieldControls, false));

        if (!existingCardExists) {
            updateFullMarking(fieldControls, false);
        }
    }

    private void updateFullMarking(Map<String, Node> fieldControls, boolean force) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");

        if (!isPartner && !isOwn) return;

        String currentSeries = getFieldValue(fieldControls, "series");
        String currentSize = getFieldValue(fieldControls, "size");
        String currentWheelFormula = getFieldValue(fieldControls, "wheelFormula");
        String currentHubName = getFieldValue(fieldControls, "hubName");
        String currentMarkingValue = getFieldValue(fieldControls, "marking");
        String currentExecutionMarking = getExecutionMarking(fieldControls);

        // Если фирменное и hubName пустой – пропускаем (защита суффикса)
        if (isOwn && (currentHubName == null || currentHubName.isEmpty())) {
            return;
        }

        if (!force) {
            String[] fieldsToCheck = getFieldsToCheckForFullMarking(isPartner);
            boolean fieldsChanged = hasAnyFieldChanged(fieldControls, initialValues, fieldsToCheck);

            if (!fieldsChanged && isOwn) {
                String initialExecution = initialExecutionMarking != null ? initialExecutionMarking : "";
                if (!currentExecutionMarking.equals(initialExecution)) {
                    fieldsChanged = true;
                }
            }

            if (!fieldsChanged) {
                return;
            }
        }

        String newFullMarking = buildFullMarking(isPartner, isOwn,
                currentSeries, currentSize, currentExecutionMarking,
                currentWheelFormula, currentHubName, currentMarkingValue);

        String existingFullMarking = fullMarkingField.getText();

        if (force || existingFullMarking == null || existingFullMarking.isEmpty() ||
                existingFullMarking.trim().equals(lastAutoFullMarking.trim())) {
            fullMarkingField.setText(newFullMarking);
            lastAutoFullMarking = newFullMarking;
            storeInitialValues(fieldControls);
        }
    }

    @Override
    public void refreshFullMarking(Map<String, Node> fieldControls) {
        updateFullMarking(fieldControls, false);
    }

    @Override
    public void forceSetFullMarking(Map<String, Node> fieldControls) {
        updateFullMarking(fieldControls, true);
    }

    // -------------------- Формирование строки маркировки --------------------

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

        Double size = null;
        try {
            if (sizeStr != null && !sizeStr.isEmpty()) {
                size = Double.parseDouble(sizeStr.replace(',', '.'));
            }
        } catch (NumberFormatException ignored) {}

        String formattedSize = formatSize(size);

        appendIfNotEmpty(sb, series);
        appendWithSeparator(sb, formattedSize);
        appendWithSeparator(sb, executionMarking);
        appendWithSeparator(sb, wheelFormula);
        if (hubName != null && !hubName.isEmpty()) {
            appendWithSeparator(sb, hubName);
        }

        return sb.toString();
    }

    private void appendIfNotEmpty(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            sb.append(value);
        }
    }

    private void appendWithSeparator(StringBuilder sb, String value) {
        if (value != null && !value.isEmpty()) {
            if (!sb.isEmpty()) sb.append("-");
            sb.append(value);
        }
    }

    private String formatSize(Double size) {
        if (size == null) return "";
        return size == Math.floor(size) ? String.valueOf(size.intValue()) : String.valueOf(size);
    }

    // -------------------- Вспомогательные методы --------------------

    private String[] getFieldsToCheckForFullMarking(boolean isPartner) {
        if (isPartner) {
            return new String[]{"marking"};
        } else {
            return new String[]{"series", "size", "wheelFormula", "hubName", "executionMarking"};
        }
    }

    public void storeInitialValues(Map<String, Node> fieldControls) {
        String[] fieldNames = {"series", "size", "wheelFormula", "hubName", "marking"};
        initialValues = new HashMap<>();
        for (String name : fieldNames) {
            initialValues.put(name, getFieldValue(fieldControls, name));
        }

        initialIsPartner = isSelected(fieldControls, "isPartnerWheel");
        initialIsOwn = isSelected(fieldControls, "isOwnProduction");
        initialIsAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        initialIsWelded = isSelected(fieldControls, "isWeldedFromMaterials");
        initialExecutionMarking = getExecutionMarking(fieldControls);
    }
}
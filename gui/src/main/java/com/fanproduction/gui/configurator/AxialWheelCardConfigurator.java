package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import java.util.Map;

public class AxialWheelCardConfigurator extends BaseWheelCardConfigurator {

    private String lastAutoWheelDiameter = "";
    private boolean initialIsAssembled = false;
    private boolean initialIsWelded = false;

    @Override
    protected String[] getPartnerFields() {
        return new String[]{"marking"};
    }

    @Override
    protected String[] getOwnFields() {
        return new String[]{
                "series", "wheelHubComponentId", "wheelHubName", "wheelHubType",
                "maxBladeCount", "bladeComponentId", "bladeName", "bladeType",
                "bladeMaterial", "bladeCount", "bladeAngle", "hubComponentId",
                "hubName", "wheelFormula",
                "isAssembledFromComponents", "isWeldedFromMaterials"
        };
    }

    @Override
    protected String[] getFieldsToCheckForFullMarking(boolean isPartner) {
        if (isPartner) return new String[]{"marking"};
        return new String[]{"series", "size", "wheelFormula", "hubName"};
    }

    @Override
    protected String[] getInitializationFields() {
        return new String[]{
                "series", "size", "wheelFormula", "hubName", "marking",
                "wheelDiameter", "maxBladeCount", "bladeCount", "bladeAngle",
                "bladeMaterial", "bladeType", "wheelHubType", "wheelHubName",
                "bladeName"
        };
    }

    @Override
    protected String buildOwnFullMarking(Map<String, Node> fieldControls) {
        String series = getFieldValue(fieldControls, "series");
        String size = getFieldValue(fieldControls, "size");
        String executionMarking = getExecutionMarking(fieldControls);
        String wheelFormula = getFieldValue(fieldControls, "wheelFormula");
        String hubName = getFieldValue(fieldControls, "hubName");

        String formattedSize = formatSize(size);
        StringBuilder sb = new StringBuilder();
        if (!series.isEmpty()) sb.append(series);
        if (!formattedSize.isEmpty()) {
            if (!sb.isEmpty()) sb.append("(");
            sb.append(formattedSize);
            if (!series.isEmpty()) sb.append(")");
        }
        appendWithSeparator(sb, executionMarking);
        appendWithSeparator(sb, wheelFormula);
        appendWithSeparator(sb, hubName);
        return sb.toString();
    }

    @Override
    protected void updateWheelFormula(Map<String, Node> fieldControls) {
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
        String bladeIdentifier = isAssembled ? getFieldValue(fieldControls, "bladeName")
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

    @Override
    protected void setupSpecificListeners(Map<String, Node> fieldControls, boolean existingCardExists) {
        // Расчёт диаметра
        setupWheelDiameterCalculation(fieldControls);

        // Слушатели для формулы
        addTextFieldListener(fieldControls, "wheelDiameter", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeCount", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "maxBladeCount", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeAngle", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeMaterial", () -> updateWheelFormula(fieldControls));
        addTextFieldListener(fieldControls, "bladeType", () -> updateWheelFormula(fieldControls));
        addCheckBoxListener(fieldControls, "isAssembledFromComponents", () -> updateWheelFormula(fieldControls));
        addCheckBoxListener(fieldControls, "isWeldedFromMaterials", () -> updateWheelFormula(fieldControls));

        // Синхронизация при загрузке
        if (existingCardExists) {
            TextField wheelDiameterField = getTextField(fieldControls, "wheelDiameter");
            if (wheelDiameterField != null && !wheelDiameterField.getText().isEmpty()) {
                lastAutoWheelDiameter = wheelDiameterField.getText();
            }
            TextField wheelFormulaField = getTextField(fieldControls, "wheelFormula");
            if (wheelFormulaField != null && !wheelFormulaField.getText().isEmpty()) {
                lastAutoWheelFormula = wheelFormulaField.getText();
            }
        }
    }

    // ===== УНИКАЛЬНЫЕ МЕТОДЫ ДЛЯ ОСЕВОГО КОЛЕСА =====

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
                        long rounded = Math.round(size * (100 - trim));
                        String newValue = String.valueOf(rounded);
                        String currentValue = diameterField.getText();
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
                updateFullMarking(fieldControls);
            });
        }
        if (weldedCheck != null) {
            weldedCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && assembledCheck != null) assembledCheck.setSelected(false);
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateWheelFormula(fieldControls);
                updateFullMarking(fieldControls);
            });
        }
    }

    // Переопределяем видимость с учётом сборное/сварное
    @Override
    protected void setupVisibilityLogic(Map<String, Node> fieldControls,
                                        Map<String, Label> fieldLabels,
                                        Map<String, Label> fieldHints) {
        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");
        boolean isAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        boolean isWelded = isSelected(fieldControls, "isWeldedFromMaterials");

        // Партнёрское
        setVisible(fieldControls, fieldLabels, fieldHints, "marking", isPartner);

        // Фирменное (общие поля)
        setVisible(fieldControls, fieldLabels, fieldHints, "series", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "isAssembledFromComponents", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "isWeldedFromMaterials", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "maxBladeCount", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeCount", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeAngle", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "hubComponentId", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "hubName", isOwn);
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelFormula", isOwn);

        // Сборное
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelHubComponentId", isOwn && isAssembled);
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelHubName", isOwn && isAssembled);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeComponentId", isOwn && isAssembled);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeName", isOwn && isAssembled);

        // Сварное
        setVisible(fieldControls, fieldLabels, fieldHints, "wheelHubType", isOwn && isWelded);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeType", isOwn && isWelded);
        setVisible(fieldControls, fieldLabels, fieldHints, "bladeMaterial", isOwn && isWelded);
    }

    // ===== SETUP FIELDS =====
    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // 1. Слушатели на галочки партнёрское/фирменное (базовый метод)
        setupWheelTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // 2. Слушатели на сборное/сварное (свой метод)
        setupAssemblyTypeExclusiveSelection(fieldControls, fieldLabels, fieldHints);

        // 3. Видимость (переопределённый метод)
        setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);

        // 4. Остальная логика (условная видимость, слушатели, генерация)
        setupConditionalVisibility(fieldControls, fieldLabels, fieldHints, () -> updateFullMarking(fieldControls));
        setupExclusiveSelection(fieldControls, () -> updateFullMarking(fieldControls));

        setupWheelDiameterCalculation(fieldControls);
        setupSpecificListeners(fieldControls, existingCardExists); // вызывает нужные слушатели

        autoFillName(fieldControls, "Колесо осевое", existingCardExists);

        // 5. Синхронизация при загрузке
        if (!existingCardExists) return;

        storeInitialValues(fieldControls);
        initialIsAssembled = isSelected(fieldControls, "isAssembledFromComponents");
        initialIsWelded = isSelected(fieldControls, "isWeldedFromMaterials");

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

    // ===== ВАЛИДАЦИЯ =====
    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {

        boolean isPartner = Boolean.TRUE.equals(fields.get("isPartnerWheel"));
        boolean isOwn = Boolean.TRUE.equals(fields.get("isOwnProduction"));

        if (!isPartner && !isOwn) {
            showValidationError("Необходимо выбрать тип колеса:\n- Партнёрское рабочее колесо\n- Фирменное рабочее колесо");
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
                showValidationError("Для фирменного колеса необходимо выбрать тип изготовления:\n- Сборное из компонентов\n- Сварное из материалов");
                return false;
            }

            if (isAssembled) {
                Long wheelHubId = (Long) fields.get("wheelHubComponentId");
                Long bladeId = (Long) fields.get("bladeComponentId");
                Long hubId = (Long) fields.get("hubComponentId");
                if (wheelHubId == null || bladeId == null || hubId == null) {
                    showValidationError("Для сборного колеса необходимо выбрать:\n- Хаб (Ступица)\n- Лопатку\n- Установочную ступицу");
                    return false;
                }
            }

            if (isWelded) {
                String hubType = (String) fields.get("wheelHubType");
                String bladeType = (String) fields.get("bladeType");
                String bladeMaterial = (String) fields.get("bladeMaterial");
                Long hubId = (Long) fields.get("hubComponentId");
                if (hubType == null || hubType.isEmpty() ||
                        bladeType == null || bladeType.isEmpty() ||
                        bladeMaterial == null || bladeMaterial.isEmpty() ||
                        hubId == null) {
                    showValidationError("Для сварного колеса необходимо заполнить:\n- Тип хаба\n- Тип лопатки\n- Материал лопатки\n- Установочную ступицу");
                    return false;
                }
            }

            // Количество лопаток
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
                showValidationError("Количество лопаток не может превышать максимальное количество для данного хаба (" + maxBladeCount + ").");
                return false;
            }
        }

        return validateFullMarking(fields);
    }
}
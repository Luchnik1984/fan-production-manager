package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.config.OnSelectActionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.FieldType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Диалог редактирования одного поля шаблона (Спринт 9, US9.3).
 * <p>
 * Две вкладки:
 * <ul>
 *   <li><b>Основные</b> — key, label, type, required, defaultValue, hint, displayOrder.</li>
 *   <li><b>Дополнительно</b> — зависит от type:
 *     <ul>
 *       <li>COMBOBOX → options (список через запятую).</li>
 *       <li>SELECTABLE → referenceType, targetFieldName, role, addToProduct.</li>
 *       <li>COMPUTED → formula.</li>
 *       <li>Общее → pullFrom, visibleIf, requiredIf, onSelect.</li>
 *     </ul>
 *   </li>
 * </ul>
 * <p>
 * Редактор программный (не FXML) — из-за условной видимости секции «Дополнительно».
 */
public class FieldEditDialog {

    private static final List<String> REFERENCE_TYPES = List.of(
            "MOTOR", "MOTOR_WHEEL", "RADIAL_WHEEL", "AXIAL_WHEEL", "COMPONENT");
    private static final List<String> CONDITION_OPERATORS = List.of(
            "EQUALS", "NOT_EQUALS", "NOT_EMPTY", "EMPTY", "IN");

    private final Dialog<ButtonType> dialog;
    private final boolean isNew;
    private final int displayOrder;

    // ========== Основные ==========
    private final TextField keyField = new TextField();
    private final TextField labelField = new TextField();
    private final ComboBox<FieldType> typeCombo = new ComboBox<>();
    private final CheckBox requiredCheck = new CheckBox("Обязательное поле");
    private final TextField defaultValueField = new TextField();
    private final TextField hintField = new TextField();
    private final TextField orderField = new TextField();

    // ========== Дополнительно ==========
    private final TextField optionsField = new TextField();
    private final ComboBox<String> referenceTypeCombo = new ComboBox<>();
    private final TextField targetFieldNameField = new TextField();
    private final TextField roleField = new TextField();
    private final CheckBox addToProductCheck = new CheckBox("Добавлять в product_components");
    private final TextArea formulaArea = new TextArea();

    private final TextField pullFromField = new TextField();

    private final TextField visibleIfFieldField = new TextField();
    private final ComboBox<String> visibleIfOperatorCombo = new ComboBox<>();
    private final TextField visibleIfValueField = new TextField();

    private final TextField requiredIfFieldField = new TextField();
    private final ComboBox<String> requiredIfOperatorCombo = new ComboBox<>();
    private final TextField requiredIfValueField = new TextField();

    private final TextField onSelectActionField = new TextField();
    private final TextField onSelectRoleField = new TextField();
    private final TextField onSelectQuantityField = new TextField();

    // ========== Секции, зависящие от type ==========
    private VBox typeSpecificSection;
    private Node comboBoxParams;
    private Node selectableParams;
    private Node computedParams;

    public FieldEditDialog(Window owner, FieldDefinitionDto existing, int defaultDisplayOrder) {
        this.isNew = (existing == null);
        this.displayOrder = existing != null && existing.getDisplayOrder() != null
                ? existing.getDisplayOrder() : defaultDisplayOrder;

        this.dialog = TemplateControllerHelper.buildDialog(
                isNew ? "Добавить поле" : "Редактировать поле: " + existing.getKey(), owner);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(750);
        dialog.getDialogPane().setPrefHeight(620);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab mainTab = new Tab("Основные", buildMainTab());
        mainTab.setClosable(false);
        tabs.getTabs().add(mainTab);

        Tab advancedTab = new Tab("Дополнительно", buildAdvancedTab());
        advancedTab.setClosable(false);
        tabs.getTabs().add(advancedTab);

        dialog.getDialogPane().setContent(tabs);

        // Заполнить значениями существующего поля
        if (existing != null) {
            fromField(existing);
        } else {
            resetDefaults();
        }

        // Реакция на смену type — переключение видимости секции «Параметры типа»
        typeCombo.valueProperty().addListener((obs, old, val) -> updateTypeSpecificVisibility(val));
        updateTypeSpecificVisibility(typeCombo.getValue());
    }

    /**
     * Показать диалог. Возвращает отредактированное поле или {@code null} (Отмена).
     */
    public FieldDefinitionDto showAndGet() {
        return dialog.showAndWait()
                .filter(b -> b.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                .map(b -> collectAndValidate())
                .orElse(null);
    }

    // ==========================================================
    // ВКЛАДКИ
    // ==========================================================

    private Node buildMainTab() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        int row = 0;

        keyField.setPromptText("series, ductSize, motorWheelId...");
        labelField.setPromptText("Серия, Типоразмер, Мотор-колесо...");
        defaultValueField.setPromptText("Необязательно");
        hintField.setPromptText("Подсказка под полем");

        typeCombo.getItems().setAll(FieldType.values());
        typeCombo.setPrefWidth(220);

        orderField.setText(String.valueOf(displayOrder));
        orderField.setPrefWidth(100);

        grid.add(new Label("Ключ поля:*"), 0, row);
        grid.add(keyField, 1, row++);
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(labelField, 1, row++);
        grid.add(new Label("Тип:*"), 0, row);
        grid.add(typeCombo, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(requiredCheck, 1, row++);
        grid.add(new Label("Значение по умолчанию:"), 0, row);
        grid.add(defaultValueField, 1, row++);
        grid.add(new Label("Подсказка:"), 0, row);
        grid.add(hintField, 1, row++);
        grid.add(new Label("Порядок отображения:"), 0, row);
        grid.add(orderField, 1, row++);

        return wrapInScroll(grid);
    }

    private Node buildAdvancedTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        // --- Секция «Параметры типа» ---
        Label typeSectionTitle = new Label("Параметры типа");
        typeSectionTitle.setStyle("-fx-font-weight: bold;");

        typeSpecificSection = new VBox(10);
        comboBoxParams = buildComboBoxParams();
        selectableParams = buildSelectableParams();
        computedParams = buildComputedParams();
        typeSpecificSection.getChildren().addAll(comboBoxParams, selectableParams, computedParams);

        // --- Секция «Условия видимости и обязательности» ---
        Label conditionsTitle = new Label("Условия");
        conditionsTitle.setStyle("-fx-font-weight: bold;");

        VBox conditionsBox = new VBox(10);
        conditionsBox.getChildren().addAll(
                buildConditionRow("visibleIf:",
                        visibleIfFieldField, visibleIfOperatorCombo, visibleIfValueField,
                        "Показывать поле только при выполнении условия"),
                buildConditionRow("requiredIf:",
                        requiredIfFieldField, requiredIfOperatorCombo, requiredIfValueField,
                        "Требовать заполнения поля только при выполнении условия")
        );

        // --- Секция «Side-effects» ---
        Label sideTitle = new Label("Side-effects");
        sideTitle.setStyle("-fx-font-weight: bold;");

        GridPane sideGrid = TemplateControllerHelper.buildGrid();
        sideGrid.setPadding(new Insets(0));
        int row = 0;
        pullFromField.setPromptText("sourceField=targetField, sourceField2=targetField2");
        onSelectActionField.setPromptText("ADD_TO_PRODUCT");
        onSelectRoleField.setPromptText("Ступица");
        onSelectQuantityField.setPromptText("1.0");
        sideGrid.add(new Label("pullFrom:"), 0, row);
        sideGrid.add(pullFromField, 1, row++);
        sideGrid.add(new Label("onSelect action:"), 0, row);
        sideGrid.add(onSelectActionField, 1, row++);
        sideGrid.add(new Label("onSelect role:"), 0, row);
        sideGrid.add(onSelectRoleField, 1, row++);
        sideGrid.add(new Label("onSelect quantity:"), 0, row);
        sideGrid.add(onSelectQuantityField, 1, row++);

        content.getChildren().addAll(
                typeSectionTitle, typeSpecificSection,
                new Separator(),
                conditionsTitle, conditionsBox,
                new Separator(),
                sideTitle, sideGrid);

        return wrapInScroll(content);
    }

    private Node buildComboBoxParams() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));
        optionsField.setPromptText("2, 4, 6, 8 — через запятую");
        grid.add(new Label("options (через запятую):*"), 0, 0);
        grid.add(optionsField, 1, 0);
        return grid;
    }

    private Node buildSelectableParams() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        referenceTypeCombo.getItems().setAll(REFERENCE_TYPES);
        referenceTypeCombo.setPrefWidth(220);
        targetFieldNameField.setPromptText("Куда сохранять displayName (например, motorFullMarking)");
        roleField.setPromptText("Роль компонента (например, Ступица колеса)");

        int row = 0;
        grid.add(new Label("referenceType:*"), 0, row);
        grid.add(referenceTypeCombo, 1, row++);
        grid.add(new Label("targetFieldName:"), 0, row);
        grid.add(targetFieldNameField, 1, row++);
        grid.add(new Label("role:"), 0, row);
        grid.add(roleField, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(addToProductCheck, 1, row++);
        return grid;
    }

    private Node buildComputedParams() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        formulaArea.setPromptText("FLOOR(DIVIDE(FIELD('wheelSize'), 10))");
        formulaArea.setPrefRowCount(4);
        formulaArea.setWrapText(true);
        grid.add(new Label("formula:*"), 0, 0);
        grid.add(formulaArea, 1, 0);

        Label hint = new Label(
                """
                        Примеры DSL:
                          MAP(FIELD('voltage'), {380: 'D', 220: 'E'})
                          FLOOR(DIVIDE(FIELD('wheelSize'), 10))
                          CONCAT(FIELD('series'), '-', FIELD('size'))""");
        hint.setStyle("-fx-text-fill: #888; -fx-font-family: monospace; -fx-font-size: 10px;");
        grid.add(hint, 1, 1);

        return grid;
    }

    private Node buildConditionRow(String label,
                                   TextField fieldField,
                                   ComboBox<String> operatorCombo,
                                   TextField valueField,
                                   String hint) {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        operatorCombo.getItems().setAll(CONDITION_OPERATORS);
        operatorCombo.setPrefWidth(150);
        fieldField.setPromptText("Имя поля (например, isPartnerProduction)");
        valueField.setPromptText("Значение (для EQUALS / NOT_EQUALS / IN)");

        int row = 0;
        grid.add(new Label(label), 0, row);
        grid.add(fieldField, 1, row);
        grid.add(operatorCombo, 2, row);
        grid.add(valueField, 3, row++);
        Label h = new Label(hint);
        h.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        grid.add(h, 1, row, 3, 1);
        return grid;
    }

    private Node wrapInScroll(Node node) {
        ScrollPane scroll = new ScrollPane(node);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }

    private void updateTypeSpecificVisibility(FieldType type) {
        boolean isCombo = type == FieldType.COMBOBOX;
        boolean isSelectable = type == FieldType.SELECTABLE;
        boolean isComputed = type == FieldType.COMPUTED;

        setVisible(comboBoxParams, isCombo);
        setVisible(selectableParams, isSelectable);
        setVisible(computedParams, isComputed);
    }

    private void setVisible(Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // ==========================================================
    // ЗАПОЛНЕНИЕ И СБОР
    // ==========================================================

    private void resetDefaults() {
        keyField.setText("");
        labelField.setText("");
        typeCombo.setValue(FieldType.TEXT);
        requiredCheck.setSelected(false);
        defaultValueField.setText("");
        hintField.setText("");
        orderField.setText(String.valueOf(displayOrder));
    }

    private void fromField(FieldDefinitionDto f) {
        keyField.setText(f.getKey());
        labelField.setText(f.getLabel());
        typeCombo.setValue(f.getType() != null ? f.getType() : FieldType.TEXT);
        requiredCheck.setSelected(f.isRequired());
        defaultValueField.setText(f.getDefaultValue() != null ? f.getDefaultValue() : "");
        hintField.setText(f.getHint() != null ? f.getHint() : "");
        orderField.setText(f.getDisplayOrder() != null
                ? String.valueOf(f.getDisplayOrder()) : String.valueOf(displayOrder));

        // Type-specific
        if (f.getOptions() != null) {
            optionsField.setText(String.join(", ", f.getOptions()));
        }
        referenceTypeCombo.setValue(f.getReferenceType());
        targetFieldNameField.setText(f.getTargetFieldName() != null ? f.getTargetFieldName() : "");
        roleField.setText(f.getRole() != null ? f.getRole() : "");
        addToProductCheck.setSelected(Boolean.TRUE.equals(f.getAddToProduct()));
        formulaArea.setText(f.getFormula() != null ? f.getFormula() : "");

        // pullFrom
        if (f.getPullFrom() != null && !f.getPullFrom().isEmpty()) {
            pullFromField.setText(f.getPullFrom().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", ")));
        }

        // Conditions
        fillCondition(f.getVisibleIf(), visibleIfFieldField, visibleIfOperatorCombo, visibleIfValueField);
        fillCondition(f.getRequiredIf(), requiredIfFieldField, requiredIfOperatorCombo, requiredIfValueField);

        // onSelect
        if (f.getOnSelect() != null) {
            onSelectActionField.setText(f.getOnSelect().getAction() != null ? f.getOnSelect().getAction() : "");
            onSelectRoleField.setText(f.getOnSelect().getRole() != null ? f.getOnSelect().getRole() : "");
            onSelectQuantityField.setText(f.getOnSelect().getQuantity() != null
                    ? String.valueOf(f.getOnSelect().getQuantity()) : "");
        }
    }

    private void fillCondition(ConditionDto condition,
                               TextField fieldField,
                               ComboBox<String> operatorCombo,
                               TextField valueField) {
        if (condition == null) return;
        fieldField.setText(condition.getField() != null ? condition.getField() : "");
        operatorCombo.setValue(condition.getOperator());
        if (condition.getValue() != null) {
            valueField.setText(String.valueOf(condition.getValue()));
        } else if (condition.getValues() != null) {
            valueField.setText(condition.getValues().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
    }

    /**
     * Собрать DTO из полей формы и валидировать.
     * Возвращает {@code null} при ошибке (после показа алерта).
     */
    private FieldDefinitionDto collectAndValidate() {
        String key = keyField.getText().trim();
        String label = labelField.getText().trim();
        FieldType type = typeCombo.getValue();

        if (key.isEmpty()) { alert("Ключ поля обязателен"); return null; }
        if (label.isEmpty()) { alert("Наименование обязательно"); return null; }
        if (type == null) { alert("Тип обязателен"); return null; }

        if (type == FieldType.COMBOBOX
                && optionsField.getText().trim().isEmpty()) {
            alert("Для COMBOBOX необходимо указать options");
            return null;
        }
        if (type == FieldType.SELECTABLE
                && (referenceTypeCombo.getValue() == null
                || referenceTypeCombo.getValue().isEmpty())) {
            alert("Для SELECTABLE необходимо указать referenceType");
            return null;
        }
        if (type == FieldType.COMPUTED
                && formulaArea.getText().trim().isEmpty()) {
            alert("Для COMPUTED необходимо указать formula");
            return null;
        }

        FieldDefinitionDto dto = new FieldDefinitionDto();
        dto.setKey(key);
        dto.setLabel(label);
        dto.setType(type);
        dto.setRequired(requiredCheck.isSelected());
        dto.setDefaultValue(emptyToNull(defaultValueField.getText()));
        dto.setHint(emptyToNull(hintField.getText()));
        try {
            dto.setDisplayOrder(Integer.parseInt(orderField.getText().trim()));
        } catch (NumberFormatException e) {
            dto.setDisplayOrder(displayOrder);
        }

        // Type-specific
        if (type == FieldType.COMBOBOX) {
            dto.setOptions(parseList(optionsField.getText()));
        }
        if (type == FieldType.SELECTABLE) {
            dto.setReferenceType(referenceTypeCombo.getValue());
            dto.setTargetFieldName(emptyToNull(targetFieldNameField.getText()));
            dto.setRole(emptyToNull(roleField.getText()));
            dto.setAddToProduct(addToProductCheck.isSelected());
        }
        if (type == FieldType.COMPUTED) {
            dto.setFormula(emptyToNull(formulaArea.getText()));
        }

        dto.setPullFrom(parsePullFrom(pullFromField.getText()));
        dto.setVisibleIf(parseCondition(
                visibleIfFieldField, visibleIfOperatorCombo, visibleIfValueField));
        dto.setRequiredIf(parseCondition(
                requiredIfFieldField, requiredIfOperatorCombo, requiredIfValueField));

        OnSelectActionDto onSelect = parseOnSelect();
        dto.setOnSelect(onSelect);

        return dto;
    }

    // ==========================================================
    // ПАРСЕРЫ
    // ==========================================================

    private List<String> parseList(String text) {
        if (text == null || text.isBlank()) return null;
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Map<String, String> parsePullFrom(String text) {
        if (text == null || text.isBlank()) return null;
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair : text.split(",")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2) {
                String source = parts[0].trim();
                String target = parts[1].trim();
                if (!source.isEmpty() && !target.isEmpty()) {
                    result.put(source, target);
                }
            }
        }
        return result.isEmpty() ? null : result;
    }

    private ConditionDto parseCondition(TextField fieldField,
                                        ComboBox<String> operatorCombo,
                                        TextField valueField) {
        String field = fieldField.getText().trim();
        if (field.isEmpty()) return null;

        String operator = operatorCombo.getValue();
        if (operator == null || operator.isEmpty()) return null;

        ConditionDto c = new ConditionDto();
        c.setField(field);
        c.setOperator(operator);

        String valueText = valueField.getText().trim();
        if (!valueText.isEmpty()) {
            if ("IN".equals(operator)) {
                c.setValues(Arrays.stream(valueText.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(s -> (Object) s)
                        .collect(Collectors.toList()));
            } else {
                c.setValue(valueText);
            }
        }
        return c;
    }

    private OnSelectActionDto parseOnSelect() {
        String action = onSelectActionField.getText().trim();
        if (action.isEmpty()) return null;

        OnSelectActionDto a = new OnSelectActionDto();
        a.setAction(action);
        a.setRole(emptyToNull(onSelectRoleField.getText()));
        String q = onSelectQuantityField.getText().trim();
        if (!q.isEmpty()) {
            try {
                a.setQuantity(Double.parseDouble(q));
            } catch (NumberFormatException ignored) {
                // пропускаем некорректное значение
            }
        }
        return a;
    }

    private String emptyToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private void alert(String message) {
        TemplateControllerHelper.showAlert(
                dialog.getOwner(), "Ошибка", message, Alert.AlertType.WARNING);
    }
}

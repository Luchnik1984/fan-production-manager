package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.config.OnSelectActionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.FieldType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Диалог редактирования одного поля шаблона (Спринт 9, US9.3).
 * <p>
 * <b>Валидация до закрытия:</b> кнопка «ОК» перехватывается через
 * {@link Button#addEventFilter}. Если валидация не прошла — диалог
 * остаётся открытым; при ошибке на вкладке «Дополнительно» — она
 * автоматически активируется и нужное поле подсвечивается.
 * <p>
 * <b>Словарь типов:</b> справа от формы — краткое описание выбранного
 * {@link FieldType} и что надо заполнить.
 * <p>
 * <b>Уникальность key:</b> проверяется через {@code isKeyDuplicate}, переданный
 * снаружи (в контексте всего шаблона), — <b>до</b> закрытия диалога.
 */
public class FieldEditDialog {

    private static final List<String> REFERENCE_TYPES = List.of(
            "MOTOR", "MOTOR_WHEEL", "RADIAL_WHEEL", "AXIAL_WHEEL", "COMPONENT");
    private static final List<String> CONDITION_OPERATORS = List.of(
            "EQUALS", "NOT_EQUALS", "NOT_EMPTY", "EMPTY", "IN");

    private final Dialog<ButtonType> dialog;
    private final boolean isNew;
    private final int displayOrder;
    private final Predicate<String> isKeyDuplicate;

    private final TextField keyField = new TextField();
    private final TextField labelField = new TextField();
    private final ComboBox<FieldType> typeCombo = new ComboBox<>();
    private final CheckBox requiredCheck = new CheckBox("Обязательное поле");
    private final TextField defaultValueField = new TextField();
    private final TextField hintField = new TextField();
    private final TextField orderField = new TextField();

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

    private VBox comboBoxParams;
    private VBox selectableParams;
    private VBox computedParams;

    private TabPane tabs;
    private Tab mainTab;
    private Tab advancedTab;

    private Label typeDictionaryLabel;

    private FieldDefinitionDto result;

    public FieldEditDialog(Window owner,
                           FieldDefinitionDto existing,
                           int defaultDisplayOrder,
                           Predicate<String> isKeyDuplicate) {
        this.isNew = (existing == null);
        this.displayOrder = existing != null && existing.getDisplayOrder() != null
                ? existing.getDisplayOrder() : defaultDisplayOrder;
        this.isKeyDuplicate = isKeyDuplicate != null ? isKeyDuplicate : key -> false;

        this.dialog = TemplateControllerHelper.buildDialog(
                isNew ? "Добавить поле" : "Редактировать поле: " + existing.getKey(), owner);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(950);
        dialog.getDialogPane().setPrefHeight(650);

        tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        mainTab = new Tab("Основные", buildMainTab());
        mainTab.setClosable(false);
        tabs.getTabs().add(mainTab);

        advancedTab = new Tab("Дополнительно", buildAdvancedTab());
        advancedTab.setClosable(false);
        tabs.getTabs().add(advancedTab);

        dialog.getDialogPane().setContent(tabs);

        if (existing != null) {
            fromField(existing);
        } else {
            resetDefaults();
        }

        typeCombo.valueProperty().addListener((obs, old, val) -> {
            updateTypeSpecificVisibility(val);
            updateTypeDictionary(val);
            updateTypeTabHint(val);
        });
        updateTypeSpecificVisibility(typeCombo.getValue());
        updateTypeDictionary(typeCombo.getValue());
        updateTypeTabHint(typeCombo.getValue());

        // ============================================================
        // Валидация ДО закрытия (включая уникальность key)
        // ============================================================
        Button okButton = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            FieldDefinitionDto collected = collectAndValidate();
            if (collected == null) {
                event.consume();
            } else {
                this.result = collected;
            }
        });
    }

    public FieldDefinitionDto showAndGet() {
        dialog.showAndWait();
        return result;
    }

    // ==========================================================
    // ВКЛАДКА «ОСНОВНЫЕ»: форма + словарь типов справа
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

        // Словарь типов справа
        typeDictionaryLabel = new Label();
        typeDictionaryLabel.setWrapText(true);
        typeDictionaryLabel.setStyle(
                "-fx-background-color: #F5F5F5; -fx-padding: 15; " +
                        "-fx-border-color: #E0E0E0; -fx-border-radius: 3; " +
                        "-fx-font-size: 11px;");

        ScrollPane dictScroll = new ScrollPane(typeDictionaryLabel);
        dictScroll.setFitToWidth(true);
        dictScroll.setPrefWidth(300);
        dictScroll.setMinWidth(250);
        dictScroll.setStyle("-fx-background-color: transparent;");

        VBox dictContainer = new VBox(5,
                new Label("Описание типа:"),
                dictScroll);
        dictContainer.getChildren().get(0).setStyle("-fx-font-weight: bold;");
        VBox.setVgrow(dictScroll, Priority.ALWAYS);

        VBox leftBox = wrapInScrollVBox(grid);
        leftBox.setPadding(new Insets(0, 15, 0, 0));
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        HBox split = new HBox(10, leftBox, dictContainer);
        split.setPadding(new Insets(10));

        return split;
    }

    private VBox wrapInScrollVBox(Node node) {
        return new VBox(node);
    }

    private void updateTypeDictionary(FieldType type) {
        if (typeDictionaryLabel == null) return;
        typeDictionaryLabel.setText(describeType(type));
    }

    private String describeType(FieldType type) {
        if (type == null) return "Выберите тип поля.";
        return switch (type) {
            case TEXT -> """
                    TEXT — обычное текстовое поле.
                    
                    Пример: «P», «40-20», «VRK-PatAIR».
                    
                    • Обязательные поля на этой вкладке:
                      ключ, наименование.
                    • Параметров на вкладке «Дополнительно» нет.
                    """;
            case NUMBER -> """
                    NUMBER — целое число.
                    
                    Пример: 4 (полюсность), 8 (количество лопаток).
                    
                    • Обязательные поля на этой вкладке:
                      ключ, наименование.
                    • Параметров на вкладке «Дополнительно» нет.
                    """;
            case DOUBLE -> """
                    DOUBLE — дробное число.
                    
                    Пример: 5.5 (мощность кВт), 22.2 (размер колеса).
                    
                    • Обязательные поля на этой вкладке:
                      ключ, наименование.
                    • Параметров на вкладке «Дополнительно» нет.
                    """;
            case BOOLEAN -> """
                    BOOLEAN — галочка (да / нет).
                    
                    Пример: isPartnerProduction, fireproof.
                    
                    • Обязательные поля на этой вкладке:
                      ключ, наименование.
                    • Параметров на вкладке «Дополнительно» нет.
                    """;
            case COMBOBOX -> """
                    COMBOBOX — выпадающий список.
                    
                    Пример: полюсность (2/4/6/8), напряжение (220/380).
                    
                    ⚠ На вкладке «Дополнительно» ОБЯЗАТЕЛЬНО:
                      • options — варианты через запятую
                        (например: «2, 4, 6, 8»).
                    """;
            case SELECTABLE -> """
                    SELECTABLE — выбор связанной сущности
                    (электродвигатель, колесо, компонент).
                    
                    Пример: motorWheelId (мотор-колесо),
                             hubComponentId (ступица).
                    
                    ⚠ На вкладке «Дополнительно» ОБЯЗАТЕЛЬНО:
                      • referenceType — тип сущности
                        (MOTOR, MOTOR_WHEEL, RADIAL_WHEEL,
                        AXIAL_WHEEL, COMPONENT).
                    • Дополнительно (необязательно):
                      targetFieldName — куда сохранять
                        отображаемое имя,
                      role — роль компонента,
                      addToProduct — добавлять в product_components.
                    """;
            case COMPUTED -> """
                    COMPUTED — вычисляемое поле (формула DSL).
                    
                    Пример: wheelSizeMarking = FLOOR(DIVIDE(FIELD('wheelSize'), 10))
                    
                    ⚠ На вкладке «Дополнительно» ОБЯЗАТЕЛЬНО:
                      • formula — выражение на DSL.
                    
                    Функции DSL: DIVIDE, MULTIPLY, ADD, SUBTRACT,
                    FLOOR, CEIL, ROUND, CONCAT, UPPER, LOWER, TRIM,
                    FORMAT_NUMBER, IF, EQUALS, NOT_EQUALS, GREATER,
                    LESS, AND, OR, NOT, EMPTY, NOT_EMPTY, DEFAULT,
                    COALESCE, MAP.
                    """;
            case SEPARATOR -> """
                    SEPARATOR — заголовок-разделитель.
                    
                    Используется для визуального разделения
                    формы карточки на секции.
                    
                    • Значение поля используется как заголовок.
                    • В правиле маркировки SEPARATOR не участвует.
                    """;
            case HIDDEN -> """
                    HIDDEN — скрытое поле.
                    
                    Используется для служебных значений,
                    которые не показываются пользователю,
                    но участвуют в маркировке (через FIELD(...)).
                    """;
        };
    }

    private void updateTypeTabHint(FieldType type) {
        if (advancedTab == null) return;
        boolean needsAdvanced = type == FieldType.COMBOBOX
                || type == FieldType.SELECTABLE
                || type == FieldType.COMPUTED;
        advancedTab.setText(needsAdvanced
                ? "Дополнительно ⚠"
                : "Дополнительно");
    }

    // ==========================================================
    // ВКЛАДКА «ДОПОЛНИТЕЛЬНО»
    // ==========================================================

    private Node buildAdvancedTab() {
        VBox content = new VBox(15);
        content.setPadding(new Insets(15));

        Label typeSectionTitle = new Label("Параметры типа");
        typeSectionTitle.setStyle("-fx-font-weight: bold;");

        comboBoxParams = wrapSection(buildComboBoxParams());
        selectableParams = wrapSection(buildSelectableParams());
        computedParams = wrapSection(buildComputedParams());

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
                typeSectionTitle, comboBoxParams, selectableParams, computedParams,
                new Separator(),
                conditionsTitle, conditionsBox,
                new Separator(),
                sideTitle, sideGrid);

        return wrapInScroll(content);
    }

    private VBox wrapSection(Node content) {
        VBox box = new VBox(content);
        box.setPadding(new Insets(5, 0, 5, 0));
        return box;
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
    // ЗАПОЛНЕНИЕ
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

        if (f.getOptions() != null) {
            optionsField.setText(String.join(", ", f.getOptions()));
        }
        referenceTypeCombo.setValue(f.getReferenceType());
        targetFieldNameField.setText(f.getTargetFieldName() != null ? f.getTargetFieldName() : "");
        roleField.setText(f.getRole() != null ? f.getRole() : "");
        addToProductCheck.setSelected(Boolean.TRUE.equals(f.getAddToProduct()));
        formulaArea.setText(f.getFormula() != null ? f.getFormula() : "");

        if (f.getPullFrom() != null && !f.getPullFrom().isEmpty()) {
            pullFromField.setText(f.getPullFrom().entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", ")));
        }

        fillCondition(f.getVisibleIf(), visibleIfFieldField, visibleIfOperatorCombo, visibleIfValueField);
        fillCondition(f.getRequiredIf(), requiredIfFieldField, requiredIfOperatorCombo, requiredIfValueField);

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

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    private FieldDefinitionDto collectAndValidate() {
        String key = keyField.getText().trim();
        String label = labelField.getText().trim();
        FieldType type = typeCombo.getValue();

        if (key.isEmpty()) {
            alert("Ключ поля обязателен");
            focusField(keyField, mainTab);
            return null;
        }
        if (isKeyDuplicate.test(key)) {
            alert("Поле с ключом '" + key + "' уже существует");
            focusField(keyField, mainTab);
            return null;
        }
        if (label.isEmpty()) {
            alert("Наименование обязательно");
            focusField(labelField, mainTab);
            return null;
        }
        if (type == null) {
            alert("Тип обязателен");
            focusField(typeCombo, mainTab);
            return null;
        }

        if (type == FieldType.COMBOBOX && optionsField.getText().trim().isEmpty()) {
            alert("Для COMBOBOX необходимо указать options " +
                    "(вкладка «Дополнительно» → «Параметры типа» → options)");
            tabs.getSelectionModel().select(advancedTab);
            focusField(optionsField, advancedTab);
            return null;
        }
        if (type == FieldType.SELECTABLE
                && (referenceTypeCombo.getValue() == null
                || referenceTypeCombo.getValue().isEmpty())) {
            alert("Для SELECTABLE необходимо указать referenceType " +
                    "(вкладка «Дополнительно» → «Параметры типа» → referenceType)");
            tabs.getSelectionModel().select(advancedTab);
            focusField(referenceTypeCombo, advancedTab);
            return null;
        }
        if (type == FieldType.COMPUTED && formulaArea.getText().trim().isEmpty()) {
            alert("Для COMPUTED необходимо указать formula " +
                    "(вкладка «Дополнительно» → «Параметры типа» → formula)");
            tabs.getSelectionModel().select(advancedTab);
            focusField(formulaArea, advancedTab);
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
        dto.setOnSelect(parseOnSelect());

        return dto;
    }

    /**
     * Переключиться на вкладку и сфокусироваться на поле.
     * <p>
     * Если вкладка уже выбрана — просто фокусируемся (без повторного select).
     */
    private void focusField(Node field, Tab tab) {
        if (tab != null) {
            tabs.getSelectionModel().select(tab);
        }
        if (field != null) {
            field.requestFocus();
        }
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

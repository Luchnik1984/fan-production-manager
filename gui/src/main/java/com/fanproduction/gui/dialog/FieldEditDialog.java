package com.fanproduction.gui.dialog;

import com.fanproduction.core.dto.FieldInfoDto;
import com.fanproduction.gui.client.CardTypeFieldsClient;
import com.fanproduction.gui.client.FormulaPreviewClient;
import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.config.OnSelectActionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.FieldType;
import com.fanproduction.gui.util.FieldKeysCatalog;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Диалог редактирования одного поля шаблона (US9.3 + рефакторинг US9.9.e.1).
 * <p>
 * <b>Что нового (US9.9.e.1):</b>
 * <ul>
 *   <li>{@code pullFrom} — {@link TableView} с двумя ComboBox (source из
 *       {@link CardTypeFieldsClient}, target из полей шаблона) + кнопки.</li>
 *   <li>{@code targetFieldName} — {@link ComboBox} полей шаблона.</li>
 *   <li>{@code visibleIf.field} / {@code requiredIf.field} — {@link ComboBox}
 *       полей шаблона + системные.</li>
 *   <li>{@code onSelect.action} — {@link ComboBox} ({@code ADD_TO_PRODUCT}).</li>
 *   <li>Русские подписи для технических терминов.</li>
 *   <li>Блок «Проверить формулу» для {@code COMPUTED} с предпросмотром
 *       через {@link FormulaPreviewClient}.</li>
 * </ul>
 * <p>
 * <b>Без изменений:</b> key, label, type, required, defaultValue, hint, order;
 * options (COMBOBOX); referenceType, role, addToProduct (SELECTABLE);
 * формула (COMPUTED); структура вкладок; словарь типов справа.
 */
@Slf4j
public class FieldEditDialog {

    // ==========================================================
    // КОНСТАНТЫ
    // ==========================================================

    private static final List<String> REFERENCE_TYPES = List.of(
            "MOTOR", "MOTOR_WHEEL", "RADIAL_WHEEL", "AXIAL_WHEEL", "COMPONENT");

    private static final List<String> CONDITION_OPERATORS = List.of(
            "EQUALS", "NOT_EQUALS", "NOT_EMPTY", "EMPTY", "IN");

    private static final List<String> ON_SELECT_ACTIONS = List.of("ADD_TO_PRODUCT");

    // ==========================================================
    // DTO ДЛЯ ТАБЛИЦЫ pullFrom
    // ==========================================================

    /** Строка таблицы pullFrom: source (поле связанной сущности) → target (поле шаблона). */
    public static class PullFromRow {
        private final SimpleStringProperty sourceKey = new SimpleStringProperty();
        private final SimpleStringProperty targetKey = new SimpleStringProperty();

        public PullFromRow() {}

        public PullFromRow(String sourceKey, String targetKey) {
            this.sourceKey.set(sourceKey);
            this.targetKey.set(targetKey);
        }

        public SimpleStringProperty sourceKeyProperty() { return sourceKey; }
        public SimpleStringProperty targetKeyProperty() { return targetKey; }

        public String getSourceKey() { return sourceKey.get(); }
        public void setSourceKey(String v) { sourceKey.set(v); }

        public String getTargetKey() { return targetKey.get(); }
        public void setTargetKey(String v) { targetKey.set(v); }
    }

    // ==========================================================
    // СОСТОЯНИЕ
    // ==========================================================

    private final Dialog<ButtonType> dialog;
    private final boolean isNew;
    private final int displayOrder;
    private final Predicate<String> isKeyDuplicate;
    private final List<FieldDefinitionDto> templateFields;

    // --- Основные поля ---
    private final TextField keyField = new TextField();
    private final TextField labelField = new TextField();
    private final ComboBox<FieldType> typeCombo = new ComboBox<>();
    private final CheckBox requiredCheck = new CheckBox("Обязательное поле");
    private final TextField defaultValueField = new TextField();
    private final TextField hintField = new TextField();
    private final TextField orderField = new TextField();

    // --- COMBOBOX ---
    private final TextField optionsField = new TextField();

    // --- SELECTABLE ---
    private final ComboBox<String> referenceTypeCombo = new ComboBox<>();
    private final ComboBox<String> targetFieldNameCombo = new ComboBox<>();
    private final TextField roleField = new TextField();
    private final CheckBox addToProductCheck = new CheckBox("Добавлять в компоненты изделия");

    // --- COMPUTED ---
    private final TextArea formulaArea = new TextArea();

    // --- pullFrom (tableView) ---
    private final ObservableList<PullFromRow> pullFromRows = FXCollections.observableArrayList();
    private TableView<PullFromRow> pullFromTable;
    private final ObservableList<String> cardTypeFieldKeys = FXCollections.observableArrayList();
    private final ObservableList<String> templateFieldKeysCache = FXCollections.observableArrayList();

    // --- Conditions ---
    private final ComboBox<String> visibleIfFieldCombo = new ComboBox<>();
    private final ComboBox<String> visibleIfOperatorCombo = new ComboBox<>();
    private final TextField visibleIfValueField = new TextField();

    private final ComboBox<String> requiredIfFieldCombo = new ComboBox<>();
    private final ComboBox<String> requiredIfOperatorCombo = new ComboBox<>();
    private final TextField requiredIfValueField = new TextField();

    // --- onSelect ---
    private final ComboBox<String> onSelectActionCombo = new ComboBox<>();
    private final TextField onSelectRoleField = new TextField();
    private final TextField onSelectQuantityField = new TextField();

    // --- Секции ---
    private VBox comboBoxParams;
    private VBox selectableParams;
    private VBox computedParams;
    private VBox pullFromSection;
    private VBox formulaPreviewBlock;

    private final TabPane tabs;
    private final Tab mainTab;
    private final Tab advancedTab;

    private Label typeDictionaryLabel;

    private FieldDefinitionDto result;

    // ==========================================================
    // КОНСТРУКТОР
    // ==========================================================

    public FieldEditDialog(Window owner,
                           FieldDefinitionDto existing,
                           int defaultDisplayOrder,
                           Predicate<String> isKeyDuplicate,
                           List<FieldDefinitionDto> templateFields) {
        this.isNew = (existing == null);
        this.displayOrder = existing != null && existing.getDisplayOrder() != null
                ? existing.getDisplayOrder() : defaultDisplayOrder;
        this.isKeyDuplicate = isKeyDuplicate != null ? isKeyDuplicate : key -> false;
        this.templateFields = templateFields != null ? templateFields : List.of();
        this.templateFieldKeysCache.addAll(FieldKeysCatalog.collect(this.templateFields));

        this.dialog = TemplateControllerHelper.buildDialog(
                isNew ? "Добавить поле" : "Редактировать поле: " + existing.getKey(), owner);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(1000);
        dialog.getDialogPane().setPrefHeight(720);

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
            // Если SELECTABLE — загрузить поля связанной сущности
            if (val == FieldType.SELECTABLE) {
                loadCardTypeFields(referenceTypeCombo.getValue());
            }
        });

        referenceTypeCombo.valueProperty().addListener((obs, old, val) -> {
            if (typeCombo.getValue() == FieldType.SELECTABLE) {
                loadCardTypeFields(val);
            }
        });

        updateTypeSpecificVisibility(typeCombo.getValue());
        updateTypeDictionary(typeCombo.getValue());
        updateTypeTabHint(typeCombo.getValue());

        // Валидация ДО закрытия
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
    // ВКЛАДКА «ОСНОВНЫЕ»
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

        typeDictionaryLabel = new Label();
        typeDictionaryLabel.setWrapText(true);
        typeDictionaryLabel.setStyle(
                "-fx-background-color: #F5F5F5; -fx-padding: 15; " +
                        "-fx-border-color: #E0E0E0; -fx-border-radius: 3; " +
                        "-fx-font-size: 11px;");

        ScrollPane dictScroll = new ScrollPane(typeDictionaryLabel);
        dictScroll.setFitToWidth(true);
        dictScroll.setPrefWidth(320);
        dictScroll.setMinWidth(260);
        dictScroll.setStyle("-fx-background-color: transparent;");

        VBox dictContainer = new VBox(5, new Label("Описание типа:"), dictScroll);
        dictContainer.getChildren().get(0).setStyle("-fx-font-weight: bold;");
        VBox.setVgrow(dictScroll, Priority.ALWAYS);

        VBox leftBox = new VBox(grid);
        leftBox.setPadding(new Insets(0, 15, 0, 0));
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        HBox split = new HBox(10, leftBox, dictContainer);
        split.setPadding(new Insets(10));

        return split;
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
                    
                    • Обязательные поля: ключ, наименование.
                    • Параметров на «Дополнительно» нет.
                    """;
            case NUMBER -> """
                    NUMBER — целое число.
                    
                    Пример: 4 (полюсность), 8 (количество лопаток).
                    """;
            case DOUBLE -> """
                    DOUBLE — дробное число.
                    
                    Пример: 5.5 (мощность кВт), 22.2 (размер колеса).
                    """;
            case BOOLEAN -> """
                    BOOLEAN — галочка (да / нет).
                    
                    Пример: isPartnerProduction, fireproof.
                    """;
            case COMBOBOX -> """
                    COMBOBOX — выпадающий список.
                    
                    ⚠ На «Дополнительно» ОБЯЗАТЕЛЬНО:
                      • options — варианты через запятую
                        (например: «2, 4, 6, 8»).
                    """;
            case SELECTABLE -> """
                    SELECTABLE — выбор связанной сущности.
                    
                    ⚠ На «Дополнительно» ОБЯЗАТЕЛЬНО:
                      • referenceType — тип сущности
                        (MOTOR, MOTOR_WHEEL, RADIAL_WHEEL,
                        AXIAL_WHEEL, COMPONENT).
                    
                    Необязательно:
                      • targetFieldName — куда сохранять
                        отображаемое имя;
                      • роль компонента;
                      • addToProduct — добавлять в компоненты;
                      • pullFrom — наследование полей.
                    """;
            case COMPUTED -> """
                    COMPUTED — вычисляемое поле (формула DSL).
                    
                    ⚠ На «Дополнительно» ОБЯЗАТЕЛЬНО:
                      • formula — выражение на DSL.
                    
                    Нажмите «▶ Вычислить» для проверки
                    формулы на тестовых значениях.
                    """;
            case SEPARATOR -> """
                    SEPARATOR — заголовок-разделитель.
                    
                    • Значение поля — заголовок секции.
                    • В правиле маркировки не участвует.
                    """;
            case HIDDEN -> """
                    HIDDEN — скрытое поле.
                    
                    • Не показывается пользователю.
                    • Может участвовать в маркировке через FIELD(...).
                    """;
        };
    }

    private void updateTypeTabHint(FieldType type) {
        if (advancedTab == null) return;
        boolean needsAdvanced = type == FieldType.COMBOBOX
                || type == FieldType.SELECTABLE
                || type == FieldType.COMPUTED;
        advancedTab.setText(needsAdvanced ? "Дополнительно ⚠" : "Дополнительно");
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

        pullFromSection = wrapSection(buildPullFromSection());

        Label conditionsTitle = new Label("Условия");
        conditionsTitle.setStyle("-fx-font-weight: bold;");

        VBox conditionsBox = new VBox(10);
        conditionsBox.getChildren().addAll(
                buildConditionRow("Показывать, только если:",
                        visibleIfFieldCombo, visibleIfOperatorCombo, visibleIfValueField,
                        "Условие видимости поля (visibleIf)"),
                buildConditionRow("Обязательно, только если:",
                        requiredIfFieldCombo, requiredIfOperatorCombo, requiredIfValueField,
                        "Условие обязательности поля (requiredIf)")
        );

        Label sideTitle = new Label("Действие при выборе");
        sideTitle.setStyle("-fx-font-weight: bold;");

        GridPane sideGrid = TemplateControllerHelper.buildGrid();
        sideGrid.setPadding(new Insets(0));
        int row = 0;
        onSelectActionCombo.getItems().setAll(ON_SELECT_ACTIONS);
        onSelectActionCombo.setPrefWidth(220);
        onSelectRoleField.setPromptText("Ступица");
        onSelectQuantityField.setPromptText("1.0");

        sideGrid.add(new Label("onSelect action:"), 0, row);
        sideGrid.add(onSelectActionCombo, 1, row++);
        sideGrid.add(new Label("onSelect role:"), 0, row);
        sideGrid.add(onSelectRoleField, 1, row++);
        sideGrid.add(new Label("onSelect quantity:"), 0, row);
        sideGrid.add(onSelectQuantityField, 1, row++);

        content.getChildren().addAll(
                typeSectionTitle, comboBoxParams, selectableParams, computedParams,
                new Separator(),
                conditionsTitle, conditionsBox,
                new Separator(),
                pullFromSection,
                new Separator(),
                sideTitle, sideGrid);

        return wrapInScroll(content);
    }

    // ==========================================================
    // COMBOBOX PARAMS
    // ==========================================================

    private Node buildComboBoxParams() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));
        optionsField.setPromptText("2, 4, 6, 8 — через запятую");
        grid.add(new Label("options (через запятую):*"), 0, 0);
        grid.add(optionsField, 1, 0);
        return grid;
    }

    // ==========================================================
    // SELECTABLE PARAMS
    // ==========================================================

    private Node buildSelectableParams() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        referenceTypeCombo.getItems().setAll(REFERENCE_TYPES);
        referenceTypeCombo.setPrefWidth(220);

        targetFieldNameCombo.setPrefWidth(320);
        targetFieldNameCombo.setMaxWidth(Double.MAX_VALUE);
        targetFieldNameCombo.setPromptText("Поле для сохранения отображаемого имени");
        targetFieldNameCombo.setEditable(false);
        targetFieldNameCombo.setItems(templateFieldKeysCache);

        roleField.setPromptText("Роль компонента (например, Ступица колеса)");

        int row = 0;
        grid.add(new Label("Тип связанной сущности (referenceType):*"), 0, row);
        grid.add(referenceTypeCombo, 1, row++);
        grid.add(new Label("Поле для отображения (targetFieldName):"), 0, row);
        grid.add(targetFieldNameCombo, 1, row++);
        grid.add(new Label("Роль (role):"), 0, row);
        grid.add(roleField, 1, row++);
        grid.add(new Label(""), 0, row);
        grid.add(addToProductCheck, 1, row++);
        return grid;
    }

    // ==========================================================
    // COMPUTED PARAMS + ПРЕДПРОСМОТР ФОРМУЛЫ
    // ==========================================================

    private Node buildComputedParams() {
        VBox container = new VBox(10);

        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        formulaArea.setPromptText("FLOOR(DIVIDE(FIELD('wheelSize'), 10))");
        formulaArea.setPrefRowCount(4);
        formulaArea.setWrapText(true);

        Button helpButton = new Button("📖");
        helpButton.setTooltip(new Tooltip("Справочник по DSL"));
        helpButton.setOnAction(e -> new DslReferenceDialog(
                dialog.getOwner(), formulaArea).show());

        HBox formulaBox = new HBox(5, formulaArea, helpButton);
        HBox.setHgrow(formulaArea, Priority.ALWAYS);
        formulaArea.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("formula:*"), 0, 0);
        grid.add(formulaBox, 1, 0);

        Label hint = new Label(
                "Примеры: MAP(FIELD('voltage'), {380: 'D', 220: 'E'}), " +
                        "FLOOR(DIVIDE(FIELD('wheelSize'), 10)), CONCAT(FIELD('series'), '-', FIELD('size'))");
        hint.setStyle("-fx-text-fill: #888; -fx-font-family: monospace; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 1, 1);

        formulaPreviewBlock = buildFormulaPreviewBlock();

        container.getChildren().addAll(grid, formulaPreviewBlock);
        return container;
    }

    /**
     * Блок «Проверить формулу»: GridPane с TextField для каждого поля шаблона
     * + кнопка «▶ Вычислить» + Label для результата.
     */
    private VBox buildFormulaPreviewBlock() {
        VBox box = new VBox(8);
        box.setStyle("-fx-background-color: #F9F9F9; -fx-padding: 10; " +
                "-fx-border-color: #E0E0E0; -fx-border-radius: 3;");

        Label title = new Label("Проверить формулу");
        title.setStyle("-fx-font-weight: bold;");

        // GridPane для тестовых значений
        GridPane valuesGrid = new GridPane();
        valuesGrid.setHgap(10);
        valuesGrid.setVgap(6);
        valuesGrid.setPadding(new Insets(5, 0, 5, 0));

        // Собираем поля шаблона (без системных вычисляемых)
        List<FieldDefinitionDto> fieldsForValues = templateFields.stream()
                .filter(f -> f.getType() != FieldType.SEPARATOR
                        && f.getType() != FieldType.HIDDEN
                        && f.getType() != FieldType.COMPUTED)
                .toList();

        if (fieldsForValues.isEmpty()) {
            Label empty = new Label("Нет полей шаблона для тестовых значений");
            empty.setStyle("-fx-text-fill: #888; -fx-font-style: italic;");
            valuesGrid.add(empty, 0, 0);
        } else {
            int row = 0;
            for (FieldDefinitionDto field : fieldsForValues) {
                Label lbl = new Label(field.getLabel() + " (" + field.getKey() + "):");
                lbl.setStyle("-fx-font-size: 11px;");
                TextField tf = new TextField();
                tf.setPromptText("тестовое значение");
                tf.setPrefWidth(200);
                // Привязка к имени поля через userData
                tf.setUserData(field.getKey());
                valuesGrid.add(lbl, 0, row);
                valuesGrid.add(tf, 1, row);
                row++;
            }
        }

        ScrollPane valuesScroll = new ScrollPane(valuesGrid);
        valuesScroll.setFitToWidth(true);
        valuesScroll.setPrefHeight(140);
        valuesScroll.setStyle("-fx-background-color: transparent;");

        // Кнопка + результат
        Button computeButton = new Button("▶ Вычислить");
        computeButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        Label resultLabel = new Label();
        resultLabel.setWrapText(true);
        resultLabel.setStyle("-fx-font-family: monospace; -fx-font-size: 12px;");
        resultLabel.setMaxWidth(Double.MAX_VALUE);

        computeButton.setOnAction(e -> {
            String formula = formulaArea.getText().trim();
            if (formula.isEmpty()) {
                resultLabel.setText("⚠ Формула пуста");
                resultLabel.setStyle("-fx-text-fill: #E53935; -fx-font-family: monospace;");
                return;
            }

            // Собираем тестовые значения из TextField'ов
            Map<String, Object> values = new HashMap<>();
            for (Node n : valuesGrid.getChildren()) {
                if (n instanceof TextField tf) {
                    String key = (String) tf.getUserData();
                    String text = tf.getText() != null ? tf.getText().trim() : "";
                    if (key != null && !text.isEmpty()) {
                        values.put(key, text);
                    }
                }
            }

            computeButton.setDisable(true);
            resultLabel.setText("Вычисление…");
            resultLabel.setStyle("-fx-text-fill: #666; -fx-font-family: monospace;");

            final String finalFormula = formula;
            new Thread(() -> {
                try {
                    String res = FormulaPreviewClient.preview(finalFormula, values);
                    Platform.runLater(() -> {
                        computeButton.setDisable(false);
                        resultLabel.setText("Результат: " + (res != null ? res : "(пусто)"));
                        resultLabel.setStyle("-fx-text-fill: #2E7D32; -fx-font-family: monospace; -fx-font-weight: bold;");
                    });
                } catch (Exception ex) {
                    log.warn("Ошибка предпросмотра формулы: {}", ex.getMessage());
                    Platform.runLater(() -> {
                        computeButton.setDisable(false);
                        resultLabel.setText("Ошибка: " + ex.getMessage());
                        resultLabel.setStyle("-fx-text-fill: #E53935; -fx-font-family: monospace;");
                    });
                }
            }, "FieldEditDialog-formulaPreview").start();
        });

        HBox actionBar = new HBox(10, computeButton);
        actionBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        box.getChildren().addAll(title, valuesScroll, actionBar, resultLabel);
        return box;
    }

    // ==========================================================
    // PULLFROM SECTION (TableView)
    // ==========================================================

    private Node buildPullFromSection() {
        VBox box = new VBox(8);

        Label title = new Label("Наследование полей (pullFrom):");
        title.setStyle("-fx-font-weight: bold;");

        Label hint = new Label(
                "При выборе значения в этом поле — значения из связанной сущности " +
                        "автоматически подставятся в указанные поля шаблона.");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);

        pullFromTable = new TableView<>();
        pullFromTable.setItems(pullFromRows);
        pullFromTable.setEditable(true);
        pullFromTable.setPrefHeight(160);
        pullFromTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<PullFromRow, String> sourceCol = new TableColumn<>("Поле в связанной сущности");
        sourceCol.setCellValueFactory(cd -> cd.getValue().sourceKeyProperty());
        sourceCol.setCellFactory(col -> new ComboInTableCell(cardTypeFieldKeys, 0));
        sourceCol.setPrefWidth(240);

        TableColumn<PullFromRow, String> targetCol = new TableColumn<>("Поле в шаблоне");
        targetCol.setCellValueFactory(cd -> cd.getValue().targetKeyProperty());
        targetCol.setCellFactory(col -> new ComboInTableCell(templateFieldKeysCache, 1));
        targetCol.setPrefWidth(240);

        TableColumn<PullFromRow, Void> deleteCol = new TableColumn<>("");
        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("✖");
            {
                deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: red;");
                deleteBtn.setOnAction(e -> {
                    PullFromRow row = getTableRow() != null ? getTableRow().getItem() : null;
                    if (row != null) pullFromRows.remove(row);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });
        deleteCol.setPrefWidth(40);
        deleteCol.setResizable(false);

        pullFromTable.getColumns().add(sourceCol);
        pullFromTable.getColumns().add(targetCol);
        pullFromTable.getColumns().add(deleteCol);

        Button addPairButton = new Button("+ Добавить пару");
        addPairButton.setOnAction(e -> pullFromRows.add(new PullFromRow()));

        box.getChildren().addAll(title, hint, pullFromTable, addPairButton);
        return box;
    }

    /**
     * ComboBox в ячейке таблицы pullFrom.
     * <p>
     * <b>Ключевое:</b> ComboBox ссылается на тот же {@link ObservableList}, что
     * и источник данных (а не на копию). При изменении списка (например, при
     * асинхронной загрузке полей связанной сущности) ComboBox обновится
     * автоматически — без {@code TableView.refresh()}.
     * <p>
     * Индекс колонки ({@code 0} — source, {@code 1} — target) передаётся
     * явно, чтобы избежать сравнения по идентичности колонок
     * ({@code getTableColumn() == getTableView().getColumns().get(0)}).
     */
    private static class ComboInTableCell extends TableCell<PullFromRow, String> {
        private final ComboBox<String> combo;
        private final int columnIndex;

        ComboInTableCell(ObservableList<String> options, int columnIndex) {
            this.columnIndex = columnIndex;
            this.combo = new ComboBox<>(options);   // ← ссылка, не копия
            this.combo.setPrefWidth(200);
            this.combo.setEditable(false);
            this.combo.valueProperty().addListener((obs, old, val) -> {
                if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) return;
                PullFromRow row = getTableView().getItems().get(getIndex());
                if (row != null) {
                    if (columnIndex == 0) {
                        row.setSourceKey(val);
                    } else {
                        row.setTargetKey(val);
                    }
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            if (empty) {
                setGraphic(null);
            } else {
                combo.setValue(getItem());
                setGraphic(combo);
            }
        }
    }

    /**
     * Асинхронно загружает ключи полей связанной сущности по referenceType.
     * <p>
     * Результат пишется в {@link #cardTypeFieldKeys} (ObservableList), на который
     * ссылаются ComboBox'ы в ячейках pullFrom-таблицы. При {@code setAll} —
     * ComboBox'ы обновляются автоматически.
     */
    private void loadCardTypeFields(String referenceType) {
        if (referenceType == null || referenceType.isBlank()) {
            cardTypeFieldKeys.clear();
            return;
        }

        new Thread(() -> {
            try {
                List<FieldInfoDto> fields = CardTypeFieldsClient.getFields(referenceType);
                List<String> keys = fields.stream()
                        .map(FieldInfoDto::key)
                        .collect(Collectors.toList());
                log.info("FieldEditDialog: загружено {} полей для referenceType={}",
                        keys.size(), referenceType);
                Platform.runLater(() -> cardTypeFieldKeys.setAll(keys));
            } catch (Exception e) {
                log.warn("FieldEditDialog: не удалось загрузить поля для {}: {}",
                        referenceType, e.getMessage(), e);
            }
        }, "FieldEditDialog-cardTypeFields").start();
    }

    // ==========================================================
    // CONDITIONS
    // ==========================================================

    private Node buildConditionRow(String label,
                                   ComboBox<String> fieldCombo,
                                   ComboBox<String> operatorCombo,
                                   TextField valueField,
                                   String hint) {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        fieldCombo.setItems(templateFieldKeysCache);
        fieldCombo.setPrefWidth(220);
        fieldCombo.setEditable(false);
        fieldCombo.setPromptText("Имя поля шаблона");

        operatorCombo.getItems().setAll(CONDITION_OPERATORS);
        operatorCombo.setPrefWidth(150);

        valueField.setPromptText("Значение (для EQUALS / NOT_EQUALS / IN)");

        int row = 0;
        grid.add(new Label(label), 0, row);
        grid.add(fieldCombo, 1, row);
        grid.add(operatorCombo, 2, row);
        grid.add(valueField, 3, row++);
        Label h = new Label(hint);
        h.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        grid.add(h, 1, row, 3, 1);
        return grid;
    }

    // ==========================================================
    // ВИДИМОСТЬ
    // ==========================================================

    private void updateTypeSpecificVisibility(FieldType type) {
        boolean isCombo = type == FieldType.COMBOBOX;
        boolean isSelectable = type == FieldType.SELECTABLE;
        boolean isComputed = type == FieldType.COMPUTED;

        setVisible(comboBoxParams, isCombo);
        setVisible(selectableParams, isSelectable);
        setVisible(computedParams, isComputed);
        setVisible(pullFromSection, isSelectable);
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
        targetFieldNameCombo.setValue(f.getTargetFieldName());
        roleField.setText(f.getRole() != null ? f.getRole() : "");
        addToProductCheck.setSelected(Boolean.TRUE.equals(f.getAddToProduct()));
        formulaArea.setText(f.getFormula() != null ? f.getFormula() : "");

        // pullFrom — заполнить строки
        if (f.getPullFrom() != null && !f.getPullFrom().isEmpty()) {
            for (Map.Entry<String, String> e : f.getPullFrom().entrySet()) {
                pullFromRows.add(new PullFromRow(e.getKey(), e.getValue()));
            }
            // Загрузить cardTypeFields для referenceType
            if (f.getReferenceType() != null && !f.getReferenceType().isBlank()) {
                loadCardTypeFields(f.getReferenceType());
            }
        }

        fillCondition(f.getVisibleIf(), visibleIfFieldCombo, visibleIfOperatorCombo, visibleIfValueField);
        fillCondition(f.getRequiredIf(), requiredIfFieldCombo, requiredIfOperatorCombo, requiredIfValueField);

        if (f.getOnSelect() != null) {
            onSelectActionCombo.setValue(f.getOnSelect().getAction());
            onSelectRoleField.setText(f.getOnSelect().getRole() != null ? f.getOnSelect().getRole() : "");
            onSelectQuantityField.setText(f.getOnSelect().getQuantity() != null
                    ? String.valueOf(f.getOnSelect().getQuantity()) : "");
        }
    }

    private void fillCondition(ConditionDto condition,
                               ComboBox<String> fieldCombo,
                               ComboBox<String> operatorCombo,
                               TextField valueField) {
        if (condition == null) return;
        fieldCombo.setValue(condition.getField());
        operatorCombo.setValue(condition.getOperator());
        if (condition.getValue() != null) {
            valueField.setText(String.valueOf(condition.getValue()));
        } else if (condition.getValues() != null) {
            valueField.setText(condition.getValues().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", ")));
        }
    }

    // ==========================================================
    // ВАЛИДАЦИЯ И СБОР
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
        if (type == FieldType.SELECTABLE) {
            if (referenceTypeCombo.getValue() == null || referenceTypeCombo.getValue().isEmpty()) {
                alert("Для SELECTABLE необходимо указать referenceType " +
                        "(вкладка «Дополнительно» → «Параметры типа» → referenceType)");
                tabs.getSelectionModel().select(advancedTab);
                focusField(referenceTypeCombo, advancedTab);
                return null;
            }
        }
        if (type == FieldType.COMPUTED && formulaArea.getText().trim().isEmpty()) {
            alert("Для COMPUTED необходимо указать formula " +
                    "(вкладка «Дополнительно» → «Параметры типа» → formula)");
            tabs.getSelectionModel().select(advancedTab);
            focusField(formulaArea, advancedTab);
            return null;
        }

        // Валидация pullFrom: targetKey ∈ fieldsJson
        Set<String> templateKeys = templateFields.stream()
                .map(FieldDefinitionDto::getKey)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (PullFromRow row : pullFromRows) {
            String src = row.getSourceKey();
            String tgt = row.getTargetKey();
            if (src == null || src.isBlank() || tgt == null || tgt.isBlank()) {
                alert("В таблице «Наследование полей» есть незаполненные строки. " +
                        "Заполните оба поля или удалите строку.");
                tabs.getSelectionModel().select(advancedTab);
                return null;
            }
            if (!templateKeys.contains(tgt)) {
                alert("Поле-приёмник '" + tgt + "' не найдено в шаблоне. " +
                        "Создайте его в редакторе полей или выберите другое.");
                tabs.getSelectionModel().select(advancedTab);
                return null;
            }
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
            dto.setTargetFieldName(emptyToNull(targetFieldNameCombo.getValue()));
            dto.setRole(emptyToNull(roleField.getText()));
            dto.setAddToProduct(addToProductCheck.isSelected());
            dto.setPullFrom(collectPullFromFromTable());
        }
        if (type == FieldType.COMPUTED) {
            dto.setFormula(emptyToNull(formulaArea.getText()));
        }

        dto.setVisibleIf(parseCondition(visibleIfFieldCombo, visibleIfOperatorCombo, visibleIfValueField));
        dto.setRequiredIf(parseCondition(requiredIfFieldCombo, requiredIfOperatorCombo, requiredIfValueField));
        dto.setOnSelect(parseOnSelect());

        return dto;
    }

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

    private Map<String, String> collectPullFromFromTable() {
        if (pullFromRows.isEmpty()) return null;
        Map<String, String> result = new LinkedHashMap<>();
        for (PullFromRow row : pullFromRows) {
            String src = row.getSourceKey();
            String tgt = row.getTargetKey();
            if (src != null && !src.isBlank() && tgt != null && !tgt.isBlank()) {
                result.put(src, tgt);
            }
        }
        return result.isEmpty() ? null : result;
    }

    private ConditionDto parseCondition(ComboBox<String> fieldCombo,
                                        ComboBox<String> operatorCombo,
                                        TextField valueField) {
        String field = fieldCombo.getValue() != null ? fieldCombo.getValue().trim() : "";
        if (field.isEmpty()) return null;

        String operator = operatorCombo.getValue();
        if (operator == null || operator.isEmpty()) return null;

        ConditionDto c = new ConditionDto();
        c.setField(field);
        c.setOperator(operator);

        String valueText = valueField.getText() != null ? valueField.getText().trim() : "";
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
        String action = onSelectActionCombo.getValue();
        if (action == null || action.isEmpty()) return null;

        OnSelectActionDto a = new OnSelectActionDto();
        a.setAction(action);
        a.setRole(emptyToNull(onSelectRoleField.getText()));
        String q = onSelectQuantityField.getText() != null
                ? onSelectQuantityField.getText().trim() : "";
        if (!q.isEmpty()) {
            try {
                a.setQuantity(Double.parseDouble(q));
            } catch (NumberFormatException ignored) {
                // пропускаем
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

    private VBox wrapSection(Node content) {
        VBox box = new VBox(content);
        box.setPadding(new Insets(5, 0, 5, 0));
        return box;
    }

    private Node wrapInScroll(Node node) {
        ScrollPane scroll = new ScrollPane(node);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        return scroll;
    }
}

package com.fanproduction.gui.dialog;

import com.fanproduction.gui.controller.MarkingRuleEditorController;
import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Диалог редактирования блока CONDITION правила маркировки (US9.4.e).
 * <p>
 * Вкладки:
 * <ul>
 *   <li><b>Условие</b> — field / operator / value (или values для IN).</li>
 *   <li><b>THEN</b> — вложенный {@link MarkingRuleEditorController} (compact mode).</li>
 *   <li><b>ELSE</b> — вложенный {@link MarkingRuleEditorController} (compact mode).</li>
 * </ul>
 * <p>
 * Вложенные редакторы поддерживают CONDITION рекурсивно (без ограничения
 * глубины в UI; сервер тоже рекурсивный).
 */
public class ConditionEditDialog {

    private static final List<String> CONDITION_OPERATORS = List.of(
            "EQUALS", "NOT_EQUALS", "NOT_EMPTY", "EMPTY", "IN");

    private static final List<String> SYSTEM_COMPUTED_FIELDS = List.of(
            "wheelSizeMarking", "voltageCode", "executionMarking",
            "sizeMarking", "powerMarking", "bladeMarking",
            "wheelDiameter", "wheelFormulaAxial", "wheelFormulaRadial",
            "fireproofMarking", "explosionMarking",
            "climateTypeMarking", "positionMarking", "connectionTypeMarking"
    );

    private final Dialog<ButtonType> dialog;
    private final ComboBox<String> fieldCombo = new ComboBox<>();
    private final ComboBox<String> operatorCombo = new ComboBox<>();
    private final TextField valueField = new TextField();

    private final List<FieldDefinitionDto> templateFields;

    /** Вложенный редактор THEN. */
    private MarkingRuleEditorController thenController;
    /** Вложенный редактор ELSE. */
    private MarkingRuleEditorController elseController;

    private MarkingRuleElementDto result;

    public ConditionEditDialog(Window owner,
                               MarkingRuleElementDto existing,
                               List<FieldDefinitionDto> templateFields) {
        this.templateFields = templateFields;
        this.dialog = TemplateControllerHelper.buildDialog(
                existing == null ? "Добавить условие" : "Редактировать условие", owner);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(850);
        dialog.getDialogPane().setPrefHeight(650);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab conditionTab = new Tab("Условие", buildConditionTab());
        conditionTab.setClosable(false);
        tabs.getTabs().add(conditionTab);

        Tab thenTab = new Tab("THEN", buildBranchTab(true));
        thenTab.setClosable(false);
        tabs.getTabs().add(thenTab);

        Tab elseTab = new Tab("ELSE", buildBranchTab(false));
        elseTab.setClosable(false);
        tabs.getTabs().add(elseTab);

        dialog.getDialogPane().setContent(tabs);

        if (existing != null) {
            fromExisting(existing);
        } else {
            operatorCombo.setValue("NOT_EMPTY");
        }
        updateValueField(operatorCombo.getValue());

        Button okButton = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            MarkingRuleElementDto collected = collectAndValidate();
            if (collected == null) {
                event.consume();
            } else {
                this.result = collected;
            }
        });
    }

    public MarkingRuleElementDto showAndGet() {
        dialog.showAndWait();
        return result;
    }

    // ==========================================================
    // ВКЛАДКА «УСЛОВИЕ»
    // ==========================================================

    private VBox buildConditionTab() {
        Set<String> fieldOptions = collectAvailableFieldKeys(templateFields);
        fieldCombo.getItems().setAll(fieldOptions);
        fieldCombo.setPrefWidth(350);
        fieldCombo.setPromptText("Выберите поле");

        operatorCombo.getItems().setAll(CONDITION_OPERATORS);
        operatorCombo.setPrefWidth(150);
        operatorCombo.valueProperty().addListener((obs, o, n) -> updateValueField(n));

        valueField.setPrefWidth(300);
        valueField.setPromptText("Значение (для EQUALS/NOT_EQUALS/IN)");

        GridPane grid = TemplateControllerHelper.buildGrid();
        int row = 0;
        grid.add(new Label("Поле:*"), 0, row);
        grid.add(fieldCombo, 1, row++, 2, 1);
        grid.add(new Label("Оператор:*"), 0, row);
        grid.add(operatorCombo, 1, row++, 2, 1);
        grid.add(new Label("Значение:"), 0, row);
        grid.add(valueField, 1, row++, 2, 1);

        Label hint = new Label(
                """
                        Для EQUALS / NOT_EQUALS — значение.
                        Для IN — список через запятую: «У1, У2, УХЛ».
                        Для EMPTY / NOT_EMPTY — значение не требуется.
                        Ветки THEN и ELSE — на отдельных вкладках.""");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 0, row++, 3, 1);

        Button helpButton = new Button("📖 Справка по операторам");
        helpButton.setOnAction(e -> new DslReferenceDialog(dialog.getOwner(), null)
                .show("Операторы условий"));
        grid.add(helpButton, 0, row, 3, 1);

        VBox content = new VBox(grid);
        content.setPadding(new Insets(10));
        return content;
    }

    private void updateValueField(String operator) {
        boolean needsValue = "EQUALS".equals(operator)
                || "NOT_EQUALS".equals(operator)
                || "IN".equals(operator);
        valueField.setDisable(!needsValue);
        if ("IN".equals(operator)) {
            valueField.setPromptText("Значения через запятую: «У1, У2, УХЛ»");
        } else if (needsValue) {
            valueField.setPromptText("Значение (например, true, 380, «У1»)");
        } else {
            valueField.setPromptText("(не используется для этого оператора)");
            valueField.clear();
        }
    }

    // ==========================================================
    // ВКЛАДКИ THEN / ELSE
    // ==========================================================

    /**
     * Построить вкладку ветки (THEN или ELSE) — вложенный MarkingRuleEditorView
     * в compact-режиме.
     */
    private VBox buildBranchTab(boolean isThen) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/fanproduction/gui/view/MarkingRuleEditorView.fxml"));
            Parent root = loader.load();

            MarkingRuleEditorController controller = loader.getController();

            com.fanproduction.gui.dto.response.FanTemplateVersionDto fakeVersion =
                    new com.fanproduction.gui.dto.response.FanTemplateVersionDto();
            fakeVersion.setFieldsJson(templateFields);

            controller.init(fakeVersion, null);
            controller.setCompactMode(true);

            if (isThen) {
                this.thenController = controller;
            } else {
                this.elseController = controller;
            }

            // Обернуть в ScrollPane — на случай, если высоты диалога не хватает
            // (тогда появится вертикальный скролл, все кнопки будут доступны).
            javafx.scene.control.ScrollPane scroll = new javafx.scene.control.ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(false);
            scroll.setStyle("-fx-background-color: transparent;");
            javafx.scene.layout.VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

            container.getChildren().add(scroll);
        } catch (Exception e) {
            Label error = new Label("Не удалось открыть вложенный редактор: " + e.getMessage());
            error.setStyle("-fx-text-fill: red;");
            error.setWrapText(true);
            container.getChildren().add(error);
        }

        return container;
    }

    // ==========================================================
    // ЗАПОЛНЕНИЕ / СБОР
    // ==========================================================

    private void fromExisting(MarkingRuleElementDto block) {
        ConditionDto c = block.getCondition();
        if (c != null) {
            fieldCombo.setValue(c.getField());
            operatorCombo.setValue(c.getOperator());
            if (c.getValue() != null) {
                valueField.setText(String.valueOf(c.getValue()));
            } else if (c.getValues() != null) {
                valueField.setText(c.getValues().stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(", ")));
            }
        }
        if (thenController != null && block.getThen() != null) {
            thenController.setBlocks(block.getThen());
        }
        if (elseController != null && block.getOtherwise() != null) {
            elseController.setBlocks(block.getOtherwise());
        }
    }

    private Set<String> collectAvailableFieldKeys(List<FieldDefinitionDto> fields) {
        Set<String> result = new LinkedHashSet<>();
        if (fields != null) {
            for (FieldDefinitionDto f : fields) {
                if (f.getKey() != null) result.add(f.getKey());
            }
        }
        result.addAll(SYSTEM_COMPUTED_FIELDS);
        return result;
    }

    private MarkingRuleElementDto collectAndValidate() {
        String field = fieldCombo.getValue();
        if (field == null || field.isEmpty()) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", "Выберите поле", Alert.AlertType.WARNING);
            return null;
        }
        String operator = operatorCombo.getValue();
        if (operator == null || operator.isEmpty()) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", "Выберите оператор", Alert.AlertType.WARNING);
            return null;
        }

        ConditionDto c = new ConditionDto();
        c.setField(field);
        c.setOperator(operator);

        String valueText = valueField.getText().trim();
        if ("IN".equals(operator)) {
            if (valueText.isEmpty()) {
                TemplateControllerHelper.showAlert(dialog.getOwner(),
                        "Ошибка", "Для IN укажите значения через запятую",
                        Alert.AlertType.WARNING);
                return null;
            }
            c.setValues(Arrays.stream(valueText.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> (Object) s)
                    .collect(Collectors.toList()));
        } else if ("EQUALS".equals(operator) || "NOT_EQUALS".equals(operator)) {
            if (valueText.isEmpty()) {
                TemplateControllerHelper.showAlert(dialog.getOwner(),
                        "Ошибка", "Для " + operator + " укажите значение",
                        Alert.AlertType.WARNING);
                return null;
            }
            c.setValue(valueText);
        }

        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(RuleElementType.CONDITION);
        dto.setCondition(c);
        dto.setThen(thenController != null
                ? new ArrayList<>(thenController.getBlocks())
                : new ArrayList<>());
        dto.setOtherwise(elseController != null
                ? new ArrayList<>(elseController.getBlocks())
                : new ArrayList<>());
        return dto;
    }
}

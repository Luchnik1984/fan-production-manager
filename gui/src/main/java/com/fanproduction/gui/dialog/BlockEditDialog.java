package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
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
 * Универсальный диалог редактирования одного блока правила маркировки (US9.4.b).
 * <p>
 * В зависимости от {@link RuleElementType} показывает разные поля:
 * <ul>
 *   <li>{@code LITERAL} / {@code SEPARATOR} — текстовое поле {@code value}.</li>
 *   <li>{@code FIELD} — ComboBox с полями шаблона + системные вычисляемые.</li>
 *   <li>{@code CONDITION} — field / operator / value (в 9.4.b THEN/ELSE пустые).</li>
 * </ul>
 * <p>
 * Валидация — внутри event filter кнопки «ОК» (как в {@code FieldEditDialog}):
 * при ошибке диалог не закрывается, введённые данные сохраняются.
 */
public class BlockEditDialog {

    /** Системные вычисляемые поля (из ComputedFieldsLibrary сервера). */
    private static final List<String> SYSTEM_COMPUTED_FIELDS = List.of(
            "wheelSizeMarking", "voltageCode", "executionMarking",
            "sizeMarking", "powerMarking", "bladeMarking",
            "wheelDiameter", "wheelFormulaAxial", "wheelFormulaRadial",
            "fireproofMarking", "explosionMarking",
            "climateTypeMarking", "positionMarking", "connectionTypeMarking"
    );

    private static final List<String> CONDITION_OPERATORS = List.of(
            "EQUALS", "NOT_EQUALS", "NOT_EMPTY", "EMPTY", "IN");

    private final Dialog<ButtonType> dialog;
    private final RuleElementType type;

    // ========== Общие ==========
    private final TextField valueField = new TextField();

    // ========== Для FIELD ==========
    private final ComboBox<String> fieldKeyCombo = new ComboBox<>();

    // ========== Для CONDITION ==========
    private final ComboBox<String> conditionFieldCombo = new ComboBox<>();
    private final ComboBox<String> conditionOperatorCombo = new ComboBox<>();
    private final TextField conditionValueField = new TextField();

    private MarkingRuleElementDto result;

    /**
     * @param owner         owner-окно
     * @param type          тип блока
     * @param existing      существующий блок (null — при создании)
     * @param templateFields поля шаблона для FIELD/CONDITION (из version.getFieldsJson())
     */
    public BlockEditDialog(Window owner,
                           RuleElementType type,
                           MarkingRuleElementDto existing,
                           List<FieldDefinitionDto> templateFields) {
        this.type = type;

        this.dialog = TemplateControllerHelper.buildDialog(
                (existing == null ? "Добавить " : "Редактировать ") + typeLabel(type),
                owner);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(320);

        VBox content = new VBox(10);
        content.setPadding(new Insets(15));

        switch (type) {
            case LITERAL, SEPARATOR -> content.getChildren().add(buildValueSection());
            case FIELD -> content.getChildren().add(buildFieldSection(templateFields));
            case CONDITION -> content.getChildren().add(buildConditionSection(templateFields));
        }

        dialog.getDialogPane().setContent(content);

        if (existing != null) {
            fromBlock(existing);
        }

        // Валидация ДО закрытия
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
    // СЕКЦИИ
    // ==========================================================

    private Node buildValueSection() {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        String prompt = (type == RuleElementType.SEPARATOR)
                ? "Например, «-», «/», «.», «_»"
                : "Например, «VRK-PatAIR»";
        valueField.setPromptText(prompt);

        grid.add(new Label("Значение:*"), 0, 0);
        grid.add(valueField, 1, 0);

        Label hint = new Label(type == RuleElementType.SEPARATOR
                ? "Одиночный символ — разделитель между полями."
                : "Фиксированный текст — например, префикс маркировки.");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        grid.add(hint, 1, 1);

        return grid;
    }

    private Node buildFieldSection(List<FieldDefinitionDto> templateFields) {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        Set<String> options = collectAvailableFieldKeys(templateFields);
        fieldKeyCombo.getItems().setAll(options);
        fieldKeyCombo.setPrefWidth(300);
        fieldKeyCombo.setEditable(false);

        grid.add(new Label("Поле:*"), 0, 0);
        grid.add(fieldKeyCombo, 1, 0);

        Label hint = new Label(
                "Поля шаблона — из «Редактора полей».\n" +
                        "Системные вычисляемые — встроенные в программу " +
                        "(wheelSizeMarking, voltageCode, …).");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 1, 1);

        return grid;
    }

    private Node buildConditionSection(List<FieldDefinitionDto> templateFields) {
        GridPane grid = TemplateControllerHelper.buildGrid();
        grid.setPadding(new Insets(0));

        Set<String> options = collectAvailableFieldKeys(templateFields);
        conditionFieldCombo.getItems().setAll(options);
        conditionFieldCombo.setPrefWidth(300);

        conditionOperatorCombo.getItems().setAll(CONDITION_OPERATORS);
        conditionOperatorCombo.setPrefWidth(150);
        conditionOperatorCombo.valueProperty().addListener((obs, o, n) -> {
            boolean needsValue = "EQUALS".equals(n) || "NOT_EQUALS".equals(n) || "IN".equals(n);
            conditionValueField.setDisable(!needsValue);
        });

        conditionValueField.setPromptText("Значение (для EQUALS/NOT_EQUALS/IN)");
        conditionValueField.setPrefWidth(250);

        int row = 0;
        grid.add(new Label("Поле:*"), 0, row);
        grid.add(conditionFieldCombo, 1, row++, 2, 1);
        grid.add(new Label("Оператор:*"), 0, row);
        grid.add(conditionOperatorCombo, 1, row++, 2, 1);
        grid.add(new Label("Значение:"), 0, row);
        grid.add(conditionValueField, 1, row++, 2, 1);

        Label hint = new Label(
                """
                        Для IN — значения через запятую: «У1, У2, УХЛ».
                        Для EMPTY / NOT_EMPTY — значение не требуется.
                        
                        THEN / ELSE-ветки будут реализованы в US9.4.e.""");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 0, row, 3, 1);

        return grid;
    }

    // ==========================================================
    // ЗАПОЛНЕНИЕ / СБОР
    // ==========================================================

    private void fromBlock(MarkingRuleElementDto block) {
        switch (type) {
            case LITERAL, SEPARATOR -> valueField.setText(block.getValue() != null ? block.getValue() : "");
            case FIELD -> fieldKeyCombo.setValue(block.getFieldKey());
            case CONDITION -> {
                ConditionDto c = block.getCondition();
                if (c != null) {
                    conditionFieldCombo.setValue(c.getField());
                    conditionOperatorCombo.setValue(c.getOperator());
                    if (c.getValue() != null) {
                        conditionValueField.setText(String.valueOf(c.getValue()));
                    } else if (c.getValues() != null) {
                        conditionValueField.setText(c.getValues().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", ")));
                    }
                }
            }
        }
    }

    private MarkingRuleElementDto collectAndValidate() {
        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(type);

        switch (type) {
            case LITERAL -> {
                String value = valueField.getText().trim();
                if (value.isEmpty()) { alert("Введите значение литерала"); return null; }
                dto.setValue(value);
            }
            case SEPARATOR -> {
                String value = valueField.getText().trim();
                if (value.isEmpty()) { alert("Введите разделитель"); return null; }
                if (value.length() > 2) { alert("Разделитель — 1–2 символа"); return null; }
                dto.setValue(value);
            }
            case FIELD -> {
                String key = fieldKeyCombo.getValue();
                if (key == null || key.isEmpty()) { alert("Выберите поле"); return null; }
                dto.setFieldKey(key);
            }
            case CONDITION -> {
                String field = conditionFieldCombo.getValue();
                String operator = conditionOperatorCombo.getValue();
                if (field == null || field.isEmpty()) { alert("Выберите поле"); return null; }
                if (operator == null || operator.isEmpty()) { alert("Выберите оператор"); return null; }

                ConditionDto c = new ConditionDto();
                c.setField(field);
                c.setOperator(operator);

                String valueText = conditionValueField.getText().trim();
                if ("IN".equals(operator)) {
                    if (valueText.isEmpty()) { alert("Для IN укажите значения через запятую"); return null; }
                    c.setValues(Arrays.stream(valueText.split(","))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .map(s -> (Object) s)
                            .collect(Collectors.toList()));
                } else if ("EQUALS".equals(operator) || "NOT_EQUALS".equals(operator)) {
                    if (valueText.isEmpty()) { alert("Для " + operator + " укажите значение"); return null; }
                    c.setValue(valueText);
                }
                // EMPTY / NOT_EMPTY — без значения

                dto.setCondition(c);
                dto.setThen(new ArrayList<>());
                dto.setOtherwise(new ArrayList<>());
            }
        }

        return dto;
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    private Set<String> collectAvailableFieldKeys(List<FieldDefinitionDto> templateFields) {
        Set<String> result = new LinkedHashSet<>();
        if (templateFields != null) {
            for (FieldDefinitionDto f : templateFields) {
                if (f.getKey() != null) {
                    result.add(f.getKey());
                }
            }
        }
        result.addAll(SYSTEM_COMPUTED_FIELDS);
        return result;
    }

    private String typeLabel(RuleElementType type) {
        return switch (type) {
            case LITERAL -> "литерал";
            case SEPARATOR -> "разделитель";
            case FIELD -> "поле";
            case CONDITION -> "условие";
        };
    }

    private void alert(String message) {
        TemplateControllerHelper.showAlert(
                dialog.getOwner(), "Ошибка", message, Alert.AlertType.WARNING);
    }
}
package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Диалог редактирования блока CONDITION правила маркировки (US9.4.c).
 * <p>
 * Позволяет задать условие: field + operator + value/values.
 * <p>
 * <b>В 9.4.c:</b> THEN/ELSE-ветки отсутствуют (плейсхолдер-подсказка).
 * <b>В 9.4.e</b> сюда будут добавлены вкладки «THEN» и «ELSE» для
 * вложенных последовательностей блоков.
 * <p>
 * Операторы:
 * <ul>
 *   <li>{@code EQUALS} / {@code NOT_EQUALS} — требуют value;</li>
 *   <li>{@code IN} — требует список values через запятую;</li>
 *   <li>{@code EMPTY} / {@code NOT_EMPTY} — без значения.</li>
 * </ul>
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

    private MarkingRuleElementDto result;

    public ConditionEditDialog(Window owner,
                               MarkingRuleElementDto existing,
                               List<FieldDefinitionDto> templateFields) {
        this.dialog = TemplateControllerHelper.buildDialog(
                existing == null ? "Добавить условие" : "Редактировать условие", owner);
        dialog.setResizable(false);
        dialog.getDialogPane().setPrefWidth(650);

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
                        Для IN — значения через запятую: «У1, У2, УХЛ».
                        Для EMPTY / NOT_EMPTY — значение не требуется.
                        
                        THEN / ELSE-ветки будут реализованы в US9.4.e.""");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 0, row, 3, 1);

        VBox content = new VBox(grid);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        if (existing != null && existing.getCondition() != null) {
            fromCondition(existing.getCondition());
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

    private void fromCondition(ConditionDto c) {
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
        // EMPTY / NOT_EMPTY — без значения

        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(RuleElementType.CONDITION);
        dto.setCondition(c);
        // THEN / ELSE — пустые. Заполнение в US9.4.e.
        dto.setThen(null);
        dto.setOtherwise(null);
        return dto;
    }
}

package com.fanproduction.gui.component;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.util.FieldKeysCatalog;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Универсальный компонент для редактирования условия
 * (visibleIf / requiredIf / CONDITION).
 * <p>
 * <b>Извлекает дублирование между</b>:
 * <ul>
 *   <li>{@code FieldEditDialog.parseCondition} / {@code fillCondition} — visibleIf, requiredIf.</li>
 *   <li>{@code ConditionEditDialog.collectConditionForPreview} / {@code fromExisting} — CONDITION.</li>
 * </ul>
 * <p>
 * Содержит:
 * <ul>
 *   <li>{@code fieldCombo} — ComboBox полей шаблона + системные.</li>
 *   <li>{@code operatorCombo} — ComboBox операторов (EQUALS, NOT_EQUALS, NOT_EMPTY, EMPTY, IN).</li>
 *   <li>{@code valueField} — TextField для значения (включается по оператору).</li>
 * </ul>
 * <p>
 * API:
 * <ul>
 *   <li>{@link #setCondition(ConditionDto)} — заполнить из DTO.</li>
 *   <li>{@link #getCondition()} — собрать DTO (null, если field/operator пусты).</li>
 *   <li>{@link #validate()} — проверить заполненность (null = всё ок).</li>
 *   <li>{@link #clear()} — сбросить.</li>
 * </ul>
 */
public class ConditionEditor extends VBox {

    private static final List<String> OPERATORS = List.of(
            "EQUALS", "NOT_EQUALS", "NOT_EMPTY", "EMPTY", "IN");

    private final ComboBox<String> fieldCombo = new ComboBox<>();
    private final ComboBox<String> operatorCombo = new ComboBox<>();
    private final TextField valueField = new TextField();

    /**
     * @param labelPrefix   префикс label перед полями; если null/пусто — без label
     * @param templateFields поля шаблона (для заполнения fieldCombo)
     * @param fieldWidth    ширина ComboBox для field
     */
    public ConditionEditor(String labelPrefix,
                           List<FieldDefinitionDto> templateFields,
                           double fieldWidth) {
        setSpacing(6);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);

        // fieldCombo
        fieldCombo.setItems(FXCollections.observableArrayList(
                FieldKeysCatalog.collect(templateFields)));
        fieldCombo.setPrefWidth(fieldWidth);
        fieldCombo.setEditable(false);
        fieldCombo.setPromptText("Имя поля шаблона");

        // operatorCombo
        operatorCombo.getItems().setAll(OPERATORS);
        operatorCombo.setPrefWidth(150);
        operatorCombo.valueProperty().addListener(
                (obs, old, val) -> updateValueField(val));

        // valueField
        valueField.setPromptText("Значение (для EQUALS / NOT_EQUALS / IN)");
        valueField.setPrefWidth(200);

        int col = 0;
        boolean hasLabel = labelPrefix != null && !labelPrefix.isBlank();
        if (hasLabel) {
            grid.add(new Label(labelPrefix), col++, 0);
        }
        grid.add(fieldCombo, col++, 0);
        grid.add(operatorCombo, col++, 0);
        grid.add(valueField, col++, 0);

        Label hint = new Label("EQUALS / NOT_EQUALS — значение; "
                + "IN — список через запятую; "
                + "EMPTY / NOT_EMPTY — значение не требуется.");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, hasLabel ? 1 : 0, 1, col, 1);

        getChildren().add(grid);
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
    // PUBLIC API
    // ==========================================================

    public void setCondition(ConditionDto condition) {
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

    public ConditionDto getCondition() {
        String field = fieldCombo.getValue();
        String operator = operatorCombo.getValue();
        if (field == null || field.isBlank() || operator == null || operator.isBlank()) {
            return null;
        }

        ConditionDto c = new ConditionDto();
        c.setField(field.trim());
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

    /**
     * Валидация. Возвращает сообщение об ошибке или {@code null}, если всё корректно.
     * <p>
     * Пустое условие (все три контрола пусты) — не ошибка (условие отсутствует).
     */
    public String validate() {
        String field = fieldCombo.getValue();
        String operator = operatorCombo.getValue();
        String valueText = valueField.getText() != null ? valueField.getText().trim() : "";

        boolean allEmpty = (field == null || field.isBlank())
                && (operator == null || operator.isBlank())
                && valueText.isEmpty();
        if (allEmpty) return null;

        if (field == null || field.isBlank()) {
            return "Выберите поле условия";
        }
        if (operator == null || operator.isBlank()) {
            return "Выберите оператор условия";
        }
        if ("EQUALS".equals(operator) || "NOT_EQUALS".equals(operator)) {
            if (valueText.isEmpty()) {
                return "Для " + operator + " укажите значение";
            }
        }
        if ("IN".equals(operator)) {
            if (valueText.isEmpty()) {
                return "Для IN укажите значения через запятую";
            }
        }
        return null;
    }

    public void clear() {
        fieldCombo.setValue(null);
        operatorCombo.setValue(null);
        valueField.clear();
    }
}

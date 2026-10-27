package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Диалог редактирования блока LITERAL правила маркировки.
 * <p>
 * LITERAL — фиксированный текст, например «VRK-PatAIR».
 * <p>
 * Валидация: значение не пустое. При ошибке диалог не закрывается.
 */
public class LiteralEditDialog {

    private final Dialog<ButtonType> dialog;
    private final TextField valueField = new TextField();

    private MarkingRuleElementDto result;

    public LiteralEditDialog(Window owner, MarkingRuleElementDto existing) {
        this.dialog = TemplateControllerHelper.buildDialog(
                existing == null ? "Добавить литерал" : "Редактировать литерал", owner);
        dialog.setResizable(false);
        dialog.getDialogPane().setPrefWidth(500);

        GridPane grid = TemplateControllerHelper.buildGrid();
        int row = 0;

        valueField.setPromptText("Например: «VRK-PatAIR»");
        grid.add(new Label("Значение:*"), 0, row);
        grid.add(valueField, 1, row++);

        Label hint = new Label(
                "Фиксированный текст — обычно префикс маркировки, " +
                        "например «VRK-PatAIR» или «VO-PatAIR-».");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 1, row);

        VBox content = new VBox(grid);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        if (existing != null && existing.getValue() != null) {
            valueField.setText(existing.getValue());
        }

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

    private MarkingRuleElementDto collectAndValidate() {
        String value = valueField.getText().trim();
        if (value.isEmpty()) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", "Введите значение литерала", Alert.AlertType.WARNING);
            return null;
        }
        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(RuleElementType.LITERAL);
        dto.setValue(value);
        return dto;
    }
}

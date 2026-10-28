package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * Диалог редактирования блока SEPARATOR правила маркировки (US9.4.c).
 * <p>
 * SEPARATOR — одиночный символ-разделитель между полями.
 * Примеры: «-», «/», «.», «_», « ».
 * <p>
 * Валидация: значение 1–2 символа, не пустое.
 */
public class SeparatorEditDialog {

    /** Готовые варианты — быстрый выбор. */
    private static final String[] PRESETS = {"-", "/", ".", "_", " ", ",", "*"};

    private final Dialog<ButtonType> dialog;
    private final TextField valueField = new TextField();

    private MarkingRuleElementDto result;

    public SeparatorEditDialog(Window owner, MarkingRuleElementDto existing) {
        this.dialog = TemplateControllerHelper.buildDialog(
                existing == null ? "Добавить разделитель" : "Редактировать разделитель", owner);
        dialog.setResizable(false);
        dialog.getDialogPane().setPrefWidth(550);

        GridPane grid = TemplateControllerHelper.buildGrid();
        int row = 0;

        valueField.setPromptText("Один символ: «-», «/», «.», «_», « »");
        valueField.setPrefWidth(80);
        grid.add(new Label("Разделитель:*"), 0, row);
        grid.add(valueField, 1, row++);

        HBox presets = new HBox(5);
        Label presetsLabel = new Label("Готовые:");
        presetsLabel.setStyle("-fx-text-fill: #888;");
        presets.getChildren().add(presetsLabel);
        for (String preset : PRESETS) {
            Button b = new Button(displayOf(preset));
            b.setStyle("-fx-font-family: monospace; -fx-min-width: 40;");
            b.setOnAction(e -> valueField.setText(preset));
            presets.getChildren().add(b);
        }
        grid.add(presets, 1, row++);

        Label hint = new Label(
                "Одиночный символ. Пробел — тоже допустимый разделитель.");
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

    private String displayOf(String preset) {
        return preset.equals(" ") ? "␣" : preset;
    }

    private MarkingRuleElementDto collectAndValidate() {
        String value = valueField.getText();
        if (value == null || value.isEmpty()) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", "Введите разделитель", Alert.AlertType.WARNING);
            return null;
        }
        if (value.length() > 2) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", "Разделитель — 1–2 символа", Alert.AlertType.WARNING);
            return null;
        }
        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(RuleElementType.SEPARATOR);
        dto.setValue(value);
        return dto;
    }
}

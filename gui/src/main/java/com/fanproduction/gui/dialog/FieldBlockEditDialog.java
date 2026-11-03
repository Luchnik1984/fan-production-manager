package com.fanproduction.gui.dialog;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.util.FieldKeysCatalog;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.List;
import java.util.Set;

/**
 * Диалог редактирования блока FIELD правила маркировки.
 * <p>
 * FIELD — значение поля карточки или системного вычисляемого поля.
 * <p>
 * Список доступных полей:
 * <ul>
 *   <li>Поля шаблона (из {@code version.getFieldsJson()});</li>
 *   <li>14 системных вычисляемых полей из ComputedFieldsLibrary (сервер).</li>
 * </ul>
 * <p>
 * Валидация: поле выбрано.
 */
public class FieldBlockEditDialog {

    private final Dialog<ButtonType> dialog;
    private final ComboBox<String> fieldKeyCombo = new ComboBox<>();

    private MarkingRuleElementDto result;

    public FieldBlockEditDialog(Window owner,
                                MarkingRuleElementDto existing,
                                List<FieldDefinitionDto> templateFields) {
        this.dialog = TemplateControllerHelper.buildDialog(
                existing == null ? "Добавить поле" : "Редактировать поле", owner);
        dialog.setResizable(false);
        dialog.getDialogPane().setPrefWidth(600);

        Set<String> options = FieldKeysCatalog.collect(templateFields);
        fieldKeyCombo.getItems().setAll(options);
        fieldKeyCombo.setPrefWidth(350);
        fieldKeyCombo.setEditable(false);
        fieldKeyCombo.setPromptText("Выберите поле");

        GridPane grid = TemplateControllerHelper.buildGrid();
        int row = 0;
        grid.add(new Label("Поле:*"), 0, row);
        grid.add(fieldKeyCombo, 1, row++);

        Label hint = new Label(
                """
                        Поля шаблона — из «Редактора полей».
                        Системные поля типа/серии (US9.8):
                          • typeDesignation — обозначение типа (например, «VRK-PatAIR»);
                          • typeName — имя типа (например, «Вентилятор канальный»);
                          • seriesName — имя серии (например, «PRV»);
                          • speciesName — имя вида (например, «Прямоугольный»).
                        Системные вычисляемые поля:
                          • wheelSizeMarking, voltageCode, executionMarking, sizeMarking, …""");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 10px;");
        hint.setWrapText(true);
        grid.add(hint, 1, row);

        VBox content = new VBox(grid);
        content.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(content);

        if (existing != null && existing.getFieldKey() != null) {
            fieldKeyCombo.setValue(existing.getFieldKey());
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
        String key = fieldKeyCombo.getValue();
        if (key == null || key.isEmpty()) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", "Выберите поле", Alert.AlertType.WARNING);
            return null;
        }
        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(RuleElementType.FIELD);
        dto.setFieldKey(key);
        return dto;
    }
}

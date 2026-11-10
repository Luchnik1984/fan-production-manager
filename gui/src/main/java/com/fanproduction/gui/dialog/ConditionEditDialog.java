package com.fanproduction.gui.dialog;

import com.fanproduction.gui.client.ConditionPreviewClient;
import com.fanproduction.gui.component.ConditionEditor;
import com.fanproduction.gui.component.PreviewPanel;
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
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;

/**
 * Диалог редактирования блока CONDITION правила маркировки (US9.4.e).
 * <p>
 * Вкладки:
 * <ul>
 *   <li><b>Условие</b> — {@link ConditionEditor} + блок «Проверить условие».</li>
 *   <li><b>THEN</b> — вложенный {@link MarkingRuleEditorController} (compact mode).</li>
 *   <li><b>ELSE</b> — вложенный {@link MarkingRuleEditorController} (compact mode).</li>
 * </ul>
 * <p>
 * <b>US9.9.e.2 + fix:</b> используется {@link ConditionEditor} вместо
 * дублирующих {@code fieldCombo} / {@code operatorCombo} / {@code valueField}
 * + {@code collectConditionForPreview} / {@code fromExisting} (часть).
 */
public class ConditionEditDialog {

    private final Dialog<ButtonType> dialog;
    private final List<FieldDefinitionDto> templateFields;

    private ConditionEditor conditionEditor;

    private MarkingRuleEditorController thenController;
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

    // ==========================================================
    // ВКЛАДКА «УСЛОВИЕ»
    // ==========================================================

    private VBox buildConditionTab() {
        conditionEditor = new ConditionEditor(null, templateFields, 350);

        Button helpButton = new Button("📖 Справка по операторам");
        helpButton.setOnAction(e -> new DslReferenceDialog(dialog.getOwner(), null)
                .show("Операторы условий"));

        PreviewPanel previewPanel = new PreviewPanel(
                "Проверить условие",
                "▶ Проверить",
                templateFields,
                values -> {
                    String validationError = conditionEditor.validate();
                    if (validationError != null) {
                        throw new IllegalArgumentException(validationError);
                    }
                    ConditionDto c = conditionEditor.getCondition();
                    return ConditionPreviewClient.preview(c, values);
                },
                res -> Boolean.TRUE.equals(res)
                        ? "Результат: ИСТИНА → выполнится ветка THEN"
                        : "Результат: ЛОЖЬ → выполнится ветка ELSE"
        );

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        content.getChildren().addAll(conditionEditor, helpButton, previewPanel);
        return content;
    }

    // ==========================================================
    // ВКЛАДКИ THEN / ELSE
    // ==========================================================

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

            ScrollPane scroll = new ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setFitToHeight(false);
            scroll.setStyle("-fx-background-color: transparent;");
            VBox.setVgrow(scroll, javafx.scene.layout.Priority.ALWAYS);

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
            conditionEditor.setCondition(c);
        }
        if (thenController != null && block.getThen() != null) {
            thenController.setBlocks(block.getThen());
        }
        if (elseController != null && block.getOtherwise() != null) {
            elseController.setBlocks(block.getOtherwise());
        }
    }

    private MarkingRuleElementDto collectAndValidate() {
        String validationError = conditionEditor.validate();
        if (validationError != null) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    "Ошибка", validationError, Alert.AlertType.WARNING);
            return null;
        }

        ConditionDto c = conditionEditor.getCondition();
        if (c == null) {
            TemplateControllerHelper.showAlert(dialog.getOwner(),
                    """
                            Ошибка""",
                    "Заполните условие: выберите поле и оператор. " +
                            "Если условие больше не нужно — нажмите «Отмена» и удалите блок " +
                            "в списке правила (кнопка 🗑).",
                    Alert.AlertType.WARNING);
            return null;
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

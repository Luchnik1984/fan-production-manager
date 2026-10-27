package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.FanTemplateVersionClient;
import com.fanproduction.gui.dialog.ConditionEditDialog;
import com.fanproduction.gui.dialog.FieldBlockEditDialog;
import com.fanproduction.gui.dialog.LiteralEditDialog;
import com.fanproduction.gui.dialog.SeparatorEditDialog;
import com.fanproduction.gui.dialog.DslReferenceDialog;
import com.fanproduction.gui.dto.fan.MarkingRuleDto;
import com.fanproduction.gui.dto.fan.MarkingRuleElementDto;
import com.fanproduction.gui.dto.fan.RuleElementType;
import com.fanproduction.gui.dto.request.fan.UpdateFanTemplateVersionRequest;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер редактора правила маркировки DRAFT-версии (US9.4.b).
 * <p>
 * Открывается из {@link TemplateDetailController} по кнопке «⚙ Редактор правила»
 * в отдельном {@code Stage} (WINDOW_MODAL).
 * <p>
 * В 9.4.b:
 * <ul>
 *   <li>Список блоков (ListView) — вертикальный.</li>
 *   <li>Добавление блоков через кнопки «+ Литерал / Разделитель / Поле / Условие».</li>
 *   <li>Редактирование / удаление / перемещение кнопками ▲▼.</li>
 *   <li>CONDITION — только field / operator / value, без THEN/ELSE (9.4.e).</li>
 *   <li>Сохранение — через {@code updateDraft} (markingRuleJson заменяется, fieldsJson не трогается).</li>
 * </ul>
 * <p>
 * В 9.4.d добавится drag-and-drop для перемещения блоков.
 * В 9.4.e — вложенные THEN/ELSE для CONDITION.
 */
@Slf4j
public class MarkingRuleEditorController {

    // ==========================================================
    // FXML
    // ==========================================================

    @javafx.fxml.FXML private Label titleLabel;
    @javafx.fxml.FXML private Label statusLabel;

    @javafx.fxml.FXML private ListView<MarkingRuleElementDto> blocksList;

    @javafx.fxml.FXML private Button addLiteralButton;
    @javafx.fxml.FXML private Button addSeparatorButton;
    @javafx.fxml.FXML private Button addFieldButton;
    @javafx.fxml.FXML private Button addConditionButton;

    @javafx.fxml.FXML private Button editBlockButton;
    @javafx.fxml.FXML private Button deleteBlockButton;
    @javafx.fxml.FXML private Button moveUpButton;
    @javafx.fxml.FXML private Button moveDownButton;

    @javafx.fxml.FXML private Button validateButton;
    @javafx.fxml.FXML private Button saveButton;
    @javafx.fxml.FXML private Button closeButton;
    @javafx.fxml.FXML private Button helpButton;

    // ==========================================================
    // СОСТОЯНИЕ
    // ==========================================================

    private final ObservableList<MarkingRuleElementDto> blocks = FXCollections.observableArrayList();
    private FanTemplateVersionDto version;
    private Runnable onSaved;

    // ==========================================================
    // INIT
    // ==========================================================

    public void init(FanTemplateVersionDto version, Runnable onSaved) {
        this.version = version;
        this.onSaved = onSaved;

        titleLabel.setText("Редактор правила — v" + version.getVersion() + " [DRAFT]");

        if (version.getMarkingRuleJson() != null
                && version.getMarkingRuleJson().getSequence() != null) {
            for (MarkingRuleElementDto block : version.getMarkingRuleJson().getSequence()) {
                blocks.add(copyBlock(block));
            }
        }

        setupList();
        updateButtonsState();
    }

    @javafx.fxml.FXML
    private void initialize() {
        setupList();
    }

    private void setupList() {
        blocksList.setItems(blocks);
        blocksList.setCellFactory(lv -> new BlockCell());
        blocksList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> updateButtonsState());
    }

    private void updateButtonsState() {
        MarkingRuleElementDto selected = blocksList.getSelectionModel().getSelectedItem();
        int idx = blocksList.getSelectionModel().getSelectedIndex();
        boolean hasSelection = selected != null;

        editBlockButton.setDisable(!hasSelection);
        deleteBlockButton.setDisable(!hasSelection);
        moveUpButton.setDisable(!hasSelection || idx <= 0);
        moveDownButton.setDisable(!hasSelection || idx >= blocks.size() - 1);
    }

    // ==========================================================
    // CELL
    // ==========================================================

    /**
     * Визуальный «кубик» блока.
     */
    private class BlockCell extends ListCell<MarkingRuleElementDto> {
        private final HBox root = new HBox(10);
        private final Label typeLabel = new Label();
        private final Label contentLabel = new Label();
        private final Label indexLabel = new Label();

        {
            typeLabel.setStyle("-fx-font-weight: bold; -fx-font-family: monospace; " +
                    "-fx-min-width: 100; -fx-pref-width: 100;");
            indexLabel.setStyle("-fx-text-fill: #666; -fx-min-width: 40; -fx-pref-width: 40;");
            contentLabel.setWrapText(true);
            HBox.setHgrow(contentLabel, javafx.scene.layout.Priority.ALWAYS);
            root.setStyle("-fx-padding: 6;");
            root.getChildren().addAll(indexLabel, typeLabel, contentLabel);
        }

        @Override
        protected void updateItem(MarkingRuleElementDto item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                indexLabel.setText("#" + (getIndex() + 1));
                typeLabel.setText(typeName(item.getType()));
                contentLabel.setText(contentOf(item));
                contentLabel.setStyle(contentStyle(item.getType()));
                setGraphic(root);
            }
        }

        private String typeName(RuleElementType type) {
            if (type == null) return "?";
            return switch (type) {
                case LITERAL -> "LITERAL";
                case SEPARATOR -> "SEPARATOR";
                case FIELD -> "FIELD";
                case CONDITION -> "CONDITION";
            };
        }

        private String contentOf(MarkingRuleElementDto b) {
            return switch (b.getType()) {
                case LITERAL -> "'" + (b.getValue() != null ? b.getValue() : "") + "'";
                case SEPARATOR -> "\"" + (b.getValue() != null ? b.getValue() : "") + "\"";
                case FIELD -> "FIELD('" + (b.getFieldKey() != null ? b.getFieldKey() : "") + "')";
                case CONDITION -> {
                    if (b.getCondition() == null) yield "—";
                    String field = b.getCondition().getField() != null ? b.getCondition().getField() : "?";
                    String op = b.getCondition().getOperator() != null
                            ? b.getCondition().getOperator() : "?";
                    String val = "";
                    if (b.getCondition().getValue() != null) {
                        val = " " + b.getCondition().getValue();
                    } else if (b.getCondition().getValues() != null) {
                        val = " " + b.getCondition().getValues();
                    }
                    yield "IF " + field + " " + op + val + " THEN [...] ELSE [...]";
                }
            };
        }

        private String contentStyle(RuleElementType type) {
            if (type == null) return "";
            return switch (type) {
                case LITERAL -> "-fx-text-fill: #2E7D32; -fx-font-family: monospace;";
                case SEPARATOR -> "-fx-text-fill: #F57C00; -fx-font-family: monospace;";
                case FIELD -> "-fx-text-fill: #1565C0; -fx-font-family: monospace;";
                case CONDITION -> "-fx-text-fill: #7B1FA2; -fx-font-family: monospace;";
            };
        }
    }

    // ==========================================================
    // CRUD
    // ==========================================================

    @javafx.fxml.FXML
    private void handleAddLiteral() {
        addBlock(RuleElementType.LITERAL);
    }

    @javafx.fxml.FXML
    private void handleAddSeparator() {
        addBlock(RuleElementType.SEPARATOR);
    }

    @javafx.fxml.FXML
    private void handleAddField() {
        addBlock(RuleElementType.FIELD);
    }

    @javafx.fxml.FXML
    private void handleAddCondition() {
        addBlock(RuleElementType.CONDITION);
    }

    private void addBlock(RuleElementType type) {
        MarkingRuleElementDto block = openBlockDialog(type, null);
        if (block == null) return;

        blocks.add(block);
        blocksList.getSelectionModel().selectLast();
        markDirty();
    }

    /**
     * Открыть специализированный диалог редактирования блока по его типу.
     * <p>
     * Возвращает новый {@link MarkingRuleElementDto} или {@code null} при отмене.
     */
    private MarkingRuleElementDto openBlockDialog(RuleElementType type,
                                                  MarkingRuleElementDto existing) {
        return switch (type) {
            case LITERAL -> new LiteralEditDialog(owner(), existing).showAndGet();
            case SEPARATOR -> new SeparatorEditDialog(owner(), existing).showAndGet();
            case FIELD -> new FieldBlockEditDialog(owner(), existing,
                    version.getFieldsJson()).showAndGet();
            case CONDITION -> new ConditionEditDialog(owner(), existing,
                    version.getFieldsJson()).showAndGet();
        };
    }

    @javafx.fxml.FXML
    private void handleEditBlock() {
        MarkingRuleElementDto selected = blocksList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int idx = blocks.indexOf(selected);

        MarkingRuleElementDto result = openBlockDialog(selected.getType(), selected);
        if (result == null) return;

        blocks.set(idx, result);
        blocksList.getSelectionModel().select(idx);
        markDirty();
    }

    @javafx.fxml.FXML
    private void handleDeleteBlock() {
        MarkingRuleElementDto selected = blocksList.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!TemplateControllerHelper.confirm(owner(),
                "Удалить блок?",
                "Удалить блок #" + (blocks.indexOf(selected) + 1) + " (" + typeName(selected.getType()) + ")?",
                null)) {
            return;
        }

        blocks.remove(selected);
        markDirty();
    }

    @javafx.fxml.FXML
    private void handleMoveUp() {
        int idx = blocksList.getSelectionModel().getSelectedIndex();
        if (idx <= 0) return;
        swap(idx, idx - 1);
        blocksList.getSelectionModel().select(idx - 1);
    }

    @javafx.fxml.FXML
    private void handleMoveDown() {
        int idx = blocksList.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= blocks.size() - 1) return;
        swap(idx, idx + 1);
        blocksList.getSelectionModel().select(idx + 1);
    }

    private void swap(int i, int j) {
        MarkingRuleElementDto a = blocks.get(i);
        MarkingRuleElementDto b = blocks.get(j);
        blocks.set(i, b);
        blocks.set(j, a);
        markDirty();
    }

    // ==========================================================
    // HELP
    // ==========================================================

    @javafx.fxml.FXML
    private void handleOpenHelp() {
        new DslReferenceDialog(owner(), null).show();
    }

    // ==========================================================
    // СОХРАНЕНИЕ / ВАЛИДАЦИЯ / ЗАКРЫТИЕ
    // ==========================================================

    @javafx.fxml.FXML
    private void handleSave() {
        saveButton.setDisable(true);
        saveButton.setText("Сохранение…");

        MarkingRuleDto rule = new MarkingRuleDto(new ArrayList<>(blocks));
        TemplateControllerHelper.runAsync(owner(),
                () -> FanTemplateVersionClient.updateDraft(
                        version.getId(),
                        new UpdateFanTemplateVersionRequest(null, rule)),
                "Правило сохранено",
                () -> {
                    saveButton.setDisable(false);
                    saveButton.setText("💾 Сохранить черновик");
                    statusLabel.setText("Сохранено ✓");
                    if (onSaved != null) onSaved.run();
                });
    }

    @javafx.fxml.FXML
    private void handleValidate() {
        validateButton.setDisable(true);
        statusLabel.setText("Проверка…");

        new Thread(() -> {
            try {
                List<String> errors = FanTemplateVersionClient.validateVersion(version.getId());
                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    statusLabel.setText("Блоков: " + blocks.size());
                    if (errors == null || errors.isEmpty()) {
                        showAlert("Валидация",
                                "Ошибок не найдено. Шаблон готов к публикации.",
                                Alert.AlertType.INFORMATION);
                    } else {
                        StringBuilder sb = new StringBuilder("Найдены ошибки:\n");
                        for (String err : errors) sb.append("• ").append(err).append("\n");
                        showAlert("Ошибки валидации", sb.toString(), Alert.AlertType.WARNING);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    statusLabel.setText("Блоков: " + blocks.size());
                    showAlert("Ошибка",
                            "Не удалось выполнить валидацию: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
            }
        }, "MarkingRuleEditor-validate").start();
    }

    @javafx.fxml.FXML
    private void handleClose() {
        Window w = owner();
        if (w instanceof javafx.stage.Stage s) {
            s.close();
        }
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    private void markDirty() {
        statusLabel.setText("Блоков: " + blocks.size() + " (не сохранено)");
        updateButtonsState();
    }

    private Window owner() {
        return TemplateControllerHelper.resolveOwner(blocksList);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        TemplateControllerHelper.showAlert(owner(), title, message, type);
    }

    private String typeName(RuleElementType type) {
        if (type == null) return "?";
        return switch (type) {
            case LITERAL -> "LITERAL";
            case SEPARATOR -> "SEPARATOR";
            case FIELD -> "FIELD";
            case CONDITION -> "CONDITION";
        };
    }

    /**
     * Копия блока — защита от мутации исходного DTO до сохранения.
     * Для CONDITION копируется condition, then, otherwise (рекурсивно).
     */
    private MarkingRuleElementDto copyBlock(MarkingRuleElementDto src) {
        if (src == null) return null;
        MarkingRuleElementDto dto = new MarkingRuleElementDto();
        dto.setType(src.getType());
        dto.setValue(src.getValue());
        dto.setFieldKey(src.getFieldKey());
        dto.setCondition(src.getCondition());
        dto.setThen(copyList(src.getThen()));
        dto.setOtherwise(copyList(src.getOtherwise()));
        return dto;
    }

    private List<MarkingRuleElementDto> copyList(List<MarkingRuleElementDto> src) {
        if (src == null) return null;
        List<MarkingRuleElementDto> list = new ArrayList<>();
        for (MarkingRuleElementDto b : src) list.add(copyBlock(b));
        return list;
    }
}
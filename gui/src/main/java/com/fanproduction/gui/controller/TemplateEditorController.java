package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.FanTemplateVersionClient;
import com.fanproduction.gui.dialog.FieldEditDialog;
import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.request.fan.UpdateFanTemplateVersionRequest;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Контроллер редактора полей DRAFT-версии шаблона (US9.3).
 * <p>
 * Открывается из {@link TemplateDetailController} по кнопке «📝 Редактор полей»
 * в отдельном {@code Stage} (WINDOW_MODAL).
 * <p>
 * Работает с {@link FanTemplateVersionDto#getFieldsJson()} через промежуточный
 * список {@link #fieldsList}. Сохранение — через {@code updateDraft}
 * (fieldsJson обновляется, markingRuleJson не меняется).
 * <p>
 * После успешного сохранения вызывается {@link #onSaved}, чтобы родитель
 * (панель шаблона) мог перезагрузиться.
 */
@Slf4j
public class TemplateEditorController {

    // ==========================================================
    // FXML
    // ==========================================================

    @javafx.fxml.FXML private Label titleLabel;
    @javafx.fxml.FXML private Label statusLabel;

    @javafx.fxml.FXML private TableView<FieldDefinitionDto> fieldsTable;
    @javafx.fxml.FXML private TableColumn<FieldDefinitionDto, String> orderColumn;
    @javafx.fxml.FXML private TableColumn<FieldDefinitionDto, String> keyColumn;
    @javafx.fxml.FXML private TableColumn<FieldDefinitionDto, String> labelColumn;
    @javafx.fxml.FXML private TableColumn<FieldDefinitionDto, String> typeColumn;
    @javafx.fxml.FXML private TableColumn<FieldDefinitionDto, String> requiredColumn;

    @javafx.fxml.FXML private Button addButton;
    @javafx.fxml.FXML private Button editButton;
    @javafx.fxml.FXML private Button deleteButton;
    @javafx.fxml.FXML private Button moveUpButton;
    @javafx.fxml.FXML private Button moveDownButton;
    @javafx.fxml.FXML private Button saveButton;
    @javafx.fxml.FXML private Button validateButton;
    @javafx.fxml.FXML private Button closeButton;

    // ==========================================================
    // СОСТОЯНИЕ
    // ==========================================================

    private final ObservableList<FieldDefinitionDto> fieldsList = FXCollections.observableArrayList();
    private FanTemplateVersionDto version;
    private Runnable onSaved;

    // ==========================================================
    // INIT
    // ==========================================================

    /**
     * Инициализация после загрузки FXML. Вызывается из {@code TemplateDetailController}.
     *
     * @param version DRAFT-версия (fieldsJson берётся оттуда)
     * @param onSaved колбэк после успешного сохранения (для перезагрузки панели шаблона)
     */
    public void init(FanTemplateVersionDto version, Runnable onSaved) {
        this.version = version;
        this.onSaved = onSaved;

        titleLabel.setText("Редактор полей — v" + version.getVersion() + " [DRAFT]");

        if (version.getFieldsJson() != null) {
            // Копия, чтобы не мутировать исходный DTO до сохранения
            for (FieldDefinitionDto f : version.getFieldsJson()) {
                fieldsList.add(copyField(f));
            }
        }
        normalizeDisplayOrders();

        setupTable();
    }

    @javafx.fxml.FXML
    private void initialize() {
        // Основная инициализация — в init(...), здесь только колонки
        setupColumns();
    }

    private void setupColumns() {
        orderColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDisplayOrder() != null
                        ? String.valueOf(cd.getValue().getDisplayOrder()) : ""));

        keyColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getKey() != null ? cd.getValue().getKey() : ""));

        labelColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getLabel() != null ? cd.getValue().getLabel() : ""));

        typeColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getType() != null
                        ? cd.getValue().getType().name() : ""));

        requiredColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().isRequired() ? "✓" : ""));

        // Стилизация type для визуального выделения
        typeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(styleForType(item));
                }
            }
        });
    }

    private String styleForType(String type) {
        return switch (type) {
            case "COMPUTED" -> "-fx-text-fill: #7B1FA2; -fx-font-weight: bold;";
            case "SELECTABLE" -> "-fx-text-fill: #1565C0;";
            case "COMBOBOX" -> "-fx-text-fill: #2E7D32;";
            case "SEPARATOR", "HIDDEN" -> "-fx-text-fill: #BDBDBD; -fx-font-style: italic;";
            default -> "";
        };
    }

    private void setupTable() {
        fieldsTable.setItems(fieldsList);
        fieldsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> updateButtonsState());
        updateButtonsState();
        statusLabel.setText("Полей: " + fieldsList.size());
    }

    private void updateButtonsState() {
        FieldDefinitionDto selected = fieldsTable.getSelectionModel().getSelectedItem();
        int idx = fieldsTable.getSelectionModel().getSelectedIndex();
        boolean hasSelection = selected != null;

        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
        moveUpButton.setDisable(!hasSelection || idx <= 0);
        moveDownButton.setDisable(!hasSelection || idx >= fieldsList.size() - 1);
    }

    // ==========================================================
    // CRUD
    // ==========================================================

    @javafx.fxml.FXML
    private void handleAdd() {
        int nextOrder = fieldsList.size() + 1;
        FieldEditDialog dialog = new FieldEditDialog(
                owner(),
                null,
                nextOrder,
                key -> isKeyDuplicate(key, -1));
        FieldDefinitionDto result = dialog.showAndGet();
        if (result == null) return;

        fieldsList.add(result);
        normalizeDisplayOrders();
        fieldsTable.getSelectionModel().selectLast();
        statusLabel.setText("Полей: " + fieldsList.size() + " (не сохранено)");
    }

    @javafx.fxml.FXML
    private void handleEdit() {
        FieldDefinitionDto selected = fieldsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int idx = fieldsList.indexOf(selected);

        FieldEditDialog dialog = new FieldEditDialog(
                owner(),
                selected,
                idx + 1,
                key -> isKeyDuplicate(key, idx));
        FieldDefinitionDto result = dialog.showAndGet();
        if (result == null) return;

        fieldsList.set(idx, result);
        normalizeDisplayOrders();
        fieldsTable.getSelectionModel().select(idx);
        statusLabel.setText("Полей: " + fieldsList.size() + " (не сохранено)");
    }

    @javafx.fxml.FXML
    private void handleDelete() {
        FieldDefinitionDto selected = fieldsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (!TemplateControllerHelper.confirm(owner(),
                "Удалить поле?",
                "Удалить поле '" + selected.getKey() + "'?",
                "Возможно, поле используется в правиле маркировки. Проверьте правило после удаления.")) {
            return;
        }

        fieldsList.remove(selected);
        normalizeDisplayOrders();
        statusLabel.setText("Полей: " + fieldsList.size() + " (не сохранено)");
        updateButtonsState();
    }

    @javafx.fxml.FXML
    private void handleMoveUp() {
        int idx = fieldsTable.getSelectionModel().getSelectedIndex();
        if (idx <= 0) return;
        swap(idx, idx - 1);
        fieldsTable.getSelectionModel().select(idx - 1);
    }

    @javafx.fxml.FXML
    private void handleMoveDown() {
        int idx = fieldsTable.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= fieldsList.size() - 1) return;
        swap(idx, idx + 1);
        fieldsTable.getSelectionModel().select(idx + 1);
    }

    private void swap(int i, int j) {
        FieldDefinitionDto a = fieldsList.get(i);
        FieldDefinitionDto b = fieldsList.get(j);
        fieldsList.set(i, b);
        fieldsList.set(j, a);
        normalizeDisplayOrders();
        statusLabel.setText("Полей: " + fieldsList.size() + " (не сохранено)");
    }

    private void normalizeDisplayOrders() {
        for (int i = 0; i < fieldsList.size(); i++) {
            fieldsList.get(i).setDisplayOrder(i + 1);
        }
        fieldsTable.refresh();
    }

    private boolean isKeyDuplicate(String key, int excludeIndex) {
        for (int i = 0; i < fieldsList.size(); i++) {
            if (i == excludeIndex) continue;
            if (key.equals(fieldsList.get(i).getKey())) return true;
        }
        return false;
    }

    // ==========================================================
    // СОХРАНЕНИЕ / ВАЛИДАЦИЯ / ЗАКРЫТИЕ
    // ==========================================================

    @javafx.fxml.FXML
    private void handleSave() {
        saveButton.setDisable(true);
        saveButton.setText("Сохранение…");

        List<FieldDefinitionDto> snapshot = new ArrayList<>(fieldsList);
        TemplateControllerHelper.runAsync(owner(),
                () -> FanTemplateVersionClient.updateDraft(
                        version.getId(),
                        new UpdateFanTemplateVersionRequest(snapshot, null)),
                "Черновик сохранён",
                () -> {
                    saveButton.setDisable(false);
                    saveButton.setText("💾 Сохранить черновик");
                    statusLabel.setText("Сохранено ✓");
                    if (onSaved != null) {
                        onSaved.run();
                    }
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
                    statusLabel.setText("Полей: " + fieldsList.size());
                    if (errors == null || errors.isEmpty()) {
                        showAlert("Валидация",
                                "Ошибок не найдено. Шаблон готов к публикации.",
                                Alert.AlertType.INFORMATION);
                    } else {
                        StringBuilder sb = new StringBuilder("Найдены ошибки:\n");
                        for (String err : errors) {
                            sb.append("• ").append(err).append("\n");
                        }
                        showAlert("Ошибки валидации", sb.toString(), Alert.AlertType.WARNING);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    validateButton.setDisable(false);
                    statusLabel.setText("Полей: " + fieldsList.size());
                    showAlert("Ошибка",
                            "Не удалось выполнить валидацию: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
            }
        }, "TemplateEditor-validate").start();
    }

    @javafx.fxml.FXML
    private void handleClose() {
        Window window = owner();
        if (window != null) {
            ((javafx.stage.Stage) window).close();
        }
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    private Window owner() {
        return TemplateControllerHelper.resolveOwner(fieldsTable);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        TemplateControllerHelper.showAlert(owner(), title, message, type);
    }

    /**
     * Копия поля (защита от мутации исходного DTO до сохранения).
     */
    private FieldDefinitionDto copyField(FieldDefinitionDto src) {
        FieldDefinitionDto dto = new FieldDefinitionDto();
        dto.setKey(src.getKey());
        dto.setLabel(src.getLabel());
        dto.setType(src.getType());
        dto.setRequired(src.isRequired());
        dto.setDefaultValue(src.getDefaultValue());
        dto.setOptions(src.getOptions() != null ? new ArrayList<>(src.getOptions()) : null);
        dto.setHint(src.getHint());
        dto.setReferenceType(src.getReferenceType());
        dto.setTargetFieldName(src.getTargetFieldName());
        dto.setRole(src.getRole());
        dto.setAddToProduct(src.getAddToProduct());
        dto.setPullFrom(src.getPullFrom());
        dto.setVisibleIf(src.getVisibleIf());
        dto.setRequiredIf(src.getRequiredIf());
        dto.setOnSelect(src.getOnSelect());
        dto.setDisplayOrder(src.getDisplayOrder());
        dto.setFormula(src.getFormula());
        return dto;
    }
}

package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductTemplateClient;
import com.fanproduction.gui.client.ProductTemplateVersionClient;
import com.fanproduction.gui.client.ProductTypeClient;
import com.fanproduction.gui.dto.request.fan.CloneProductTemplateRequest;
import com.fanproduction.gui.dto.request.fan.CreateProductTemplateRequest;
import com.fanproduction.gui.dto.request.fan.CreateProductTemplateVersionRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductTemplateRequest;
import com.fanproduction.gui.dto.response.ProductSeriesDto;
import com.fanproduction.gui.dto.response.ProductTemplateDto;
import com.fanproduction.gui.dto.response.ProductTemplateVersionDto;
import com.fanproduction.gui.dto.response.ProductTypeDto;
import com.fanproduction.gui.util.ProductTreeBuilder;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Контроллер правой панели «Конструктор шаблонов» (US9.2).
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Загрузку шаблона по выбранной серии ({@link #showForSeries(ProductSeriesDto)}).</li>
 *   <li>Список версий с их статусами.</li>
 *   <li>Управление жизненным циклом версий (publish / deprecate / archive / delete / create).</li>
 *   <li>Клонирование шаблона для другой серии.</li>
 * </ul>
 * <p>
 * Редакторы полей (US9.3), правила маркировки (US9.4) и предпросмотр (US9.5)
 * показывают заглушку-алерт до реализации соответствующих подшагов.
 * <p>
 * <b>Race condition:</b> при быстром клике по разным сериям загрузки могут
 * завершаться в произвольном порядке. Поле {@link #generation} защищает от
 * «устаревшего» результата: применяется только результат последнего вызова
 * {@link #showForSeries(ProductSeriesDto)}.
 * <p>
 * Общие утилиты ({@code runAsync}, {@code showAlert}, {@code buildDialog},
 * {@code confirm}) — из {@link TemplateControllerHelper}.
 */
@Slf4j
public class TemplateDetailController {

    // ==========================================================
    // FXML
    // ==========================================================

    @javafx.fxml.FXML private StackPane noTemplatePane;
    @javafx.fxml.FXML private VBox templatePane;
    @javafx.fxml.FXML private Button createTemplateButton;

    @javafx.fxml.FXML private Label templateNameLabel;
    @javafx.fxml.FXML private Label templateDescriptionLabel;
    @javafx.fxml.FXML private Label currentVersionLabel;
    @javafx.fxml.FXML private Button editTemplateButton;
    @javafx.fxml.FXML private Button refreshButton;

    @javafx.fxml.FXML private Button openFieldsEditorButton;
    @javafx.fxml.FXML private Button openRuleEditorButton;
    @javafx.fxml.FXML private Button previewButton;

    @javafx.fxml.FXML private TableView<ProductTemplateVersionDto> versionsTable;
    @javafx.fxml.FXML private TableColumn<ProductTemplateVersionDto, String> versionColumn;
    @javafx.fxml.FXML private TableColumn<ProductTemplateVersionDto, String> statusColumn;
    @javafx.fxml.FXML private TableColumn<ProductTemplateVersionDto, String> createdAtColumn;
    @javafx.fxml.FXML private TableColumn<ProductTemplateVersionDto, String> publishedAtColumn;
    @javafx.fxml.FXML private TableColumn<ProductTemplateVersionDto, String> createdByColumn;

    @javafx.fxml.FXML private Button createVersionButton;
    @javafx.fxml.FXML private Button publishButton;
    @javafx.fxml.FXML private Button deprecateButton;
    @javafx.fxml.FXML private Button archiveButton;
    @javafx.fxml.FXML private Button deleteVersionButton;
    @javafx.fxml.FXML private Button cloneButton;
    @javafx.fxml.FXML private Label statusLabel;

    // ==========================================================
    // СОСТОЯНИЕ
    // ==========================================================

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final ObservableList<ProductTemplateVersionDto> versionsList = FXCollections.observableArrayList();

    private ProductSeriesDto currentSeries;
    private ProductTemplateDto currentTemplate;

    /** Поколение загрузок — защита от race condition при быстром клике. */
    private long generation = 0L;

    // ==========================================================
    // INIT
    // ==========================================================

    @javafx.fxml.FXML
    private void initialize() {
        setupTableColumns();
        versionsTable.setItems(versionsList);
        versionsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, sel) -> updateButtonsState());
        updateButtonsState();
    }

    private void setupTableColumns() {
        versionColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                "v" + cd.getValue().getVersion()));

        statusColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getStatus() != null ? cd.getValue().getStatus() : ""));

        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle(styleForStatus(item));
                }
            }
        });

        createdAtColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreatedAt() != null
                        ? cd.getValue().getCreatedAt().format(DATE_FORMATTER) : ""));

        publishedAtColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getPublishedAt() != null
                        ? cd.getValue().getPublishedAt().format(DATE_FORMATTER) : ""));

        createdByColumn.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getCreatedBy() != null ? cd.getValue().getCreatedBy() : ""));
    }

    private String styleForStatus(String status) {
        return switch (status) {
            case "DRAFT" -> "-fx-text-fill: #F57C00; -fx-font-weight: bold;";
            case "PUBLISHED" -> "-fx-text-fill: #2E7D32; -fx-font-weight: bold;";
            case "LEGACY" -> "-fx-text-fill: #1976D2;";
            case "DEPRECATED" -> "-fx-text-fill: #757575;";
            case "ARCHIVED" -> "-fx-text-fill: #BDBDBD; -fx-font-style: italic;";
            default -> "";
        };
    }

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    /**
     * Загрузить панель для серии.
     * <p>
     * Потокобезопасен, можно вызывать из FX-потока (что делает
     * {@link TemplateTreeController}). Сама загрузка — в фоне.
     */
    public void showForSeries(ProductSeriesDto series) {
        if (series == null) {
            return;
        }
        this.currentSeries = series;
        this.currentTemplate = null;
        long myGeneration = ++generation;

        Platform.runLater(() -> {
            versionsList.clear();
            templateNameLabel.setText("");
            templateDescriptionLabel.setText("");
            currentVersionLabel.setText("—");
            statusLabel.setText("Загрузка…");
            switchToLoadingState();
            updateButtonsState();
        });

        new Thread(() -> loadAsync(series, myGeneration),
                "TemplateDetail-loader").start();
    }

    // ==========================================================
    // АСИНХРОННАЯ ЗАГРУЗКА
    // ==========================================================

    private void loadAsync(ProductSeriesDto series, long myGeneration) {
        try {
            ProductTemplateDto template;
            try {
                template = ProductTemplateClient.getTemplateBySeries(series.getId());
            } catch (Exception e) {
                String msg = e.getMessage();
                if (msg != null && msg.contains("не найден")) {
                    Platform.runLater(() -> {
                        if (myGeneration != generation) return;
                        showNoTemplateState();
                    });
                    return;
                }
                throw e;
            }

            List<ProductTemplateVersionDto> versions = ProductTemplateClient.getVersionsByTemplate(template.getId());
            Platform.runLater(() -> {
                if (myGeneration != generation) return;
                showTemplateState(template, versions);
            });
        } catch (Exception e) {
            log.warn("Ошибка загрузки шаблона для серии {}: {}", series.getId(), e.getMessage());
            Platform.runLater(() -> {
                if (myGeneration != generation) return;
                statusLabel.setText("Ошибка загрузки");
                showAlert("Ошибка", "Не удалось загрузить шаблон: " + e.getMessage(),
                        Alert.AlertType.ERROR);
            });
        }
    }

    private void switchToLoadingState() {
        noTemplatePane.setVisible(false);
        noTemplatePane.setManaged(false);
        templatePane.setVisible(false);
        templatePane.setManaged(false);
    }

    private void showNoTemplateState() {
        noTemplatePane.setVisible(true);
        noTemplatePane.setManaged(true);
        templatePane.setVisible(false);
        templatePane.setManaged(false);
        statusLabel.setText("Нет шаблона");
    }

    private void showTemplateState(ProductTemplateDto template, List<ProductTemplateVersionDto> versions) {
        this.currentTemplate = template;

        templateNameLabel.setText(template.getName());
        templateDescriptionLabel.setText(template.getDescription() != null ? template.getDescription() : "");

        versionsList.setAll(versions);

        ProductTemplateVersionDto currentVersion = versions.stream()
                .filter(v -> Objects.equals(v.getId(), template.getCurrentVersionId()))
                .findFirst().orElse(null);
        if (currentVersion != null) {
            currentVersionLabel.setText("v" + currentVersion.getVersion()
                    + " [" + currentVersion.getStatus() + "]");
        } else {
            currentVersionLabel.setText("— (нет опубликованной)");
        }

        noTemplatePane.setVisible(false);
        noTemplatePane.setManaged(false);
        templatePane.setVisible(true);
        templatePane.setManaged(true);

        statusLabel.setText("Версий: " + versions.size());
        updateButtonsState();
    }

    // ==========================================================
    // СОСТОЯНИЕ КНОПОК
    // ==========================================================

    private void updateButtonsState() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        boolean hasSelection = selected != null;
        String status = hasSelection ? selected.getStatus() : null;

        boolean hasDraft = versionsList.stream()
                .anyMatch(v -> "DRAFT".equals(v.getStatus()));

        boolean hasTemplate = currentTemplate != null;

        createVersionButton.setDisable(!hasTemplate || hasDraft);

        publishButton.setDisable(!"DRAFT".equals(status));
        deprecateButton.setDisable(!"LEGACY".equals(status));
        archiveButton.setDisable(!"DEPRECATED".equals(status));
        deleteVersionButton.setDisable(!("DRAFT".equals(status) || "ARCHIVED".equals(status)));

        openFieldsEditorButton.setDisable(!"DRAFT".equals(status));
        openRuleEditorButton.setDisable(!"DRAFT".equals(status));
        previewButton.setDisable(!hasSelection);

        cloneButton.setDisable(!hasTemplate);
        editTemplateButton.setDisable(!hasTemplate);
        refreshButton.setDisable(currentSeries == null);
    }

    // ==========================================================
    // LIFECYCLE: TEMPLATE
    // ==========================================================

    @javafx.fxml.FXML
    private void handleCreateTemplate() {
        if (currentSeries == null) return;

        TextField nameField = new TextField();
        nameField.setPromptText("Например, «Шаблон P-серии»");
        if (currentSeries.getName() != null && !currentSeries.getName().isBlank()) {
            nameField.setText("Шаблон " + currentSeries.getName());
        }

        TextArea descField = new TextArea();
        descField.setPrefRowCount(3);
        descField.setPromptText("Описание (необязательно)");

        Dialog<ButtonType> dialog = buildTemplateFormDialog(
                "Создать шаблон", nameField, descField);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Ошибка", "Наименование шаблона обязательно",
                        Alert.AlertType.WARNING);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            String name = nameField.getText().trim();
            String description = descField.getText().trim();

            runAsync(() -> ProductTemplateClient.createTemplate(
                    new CreateProductTemplateRequest(
                            currentSeries.getId(),
                            name,
                            description.isEmpty() ? null : description,
                            null,
                            null
                    )), "Шаблон создан");
        });
    }

    @javafx.fxml.FXML
    private void handleEditTemplate() {
        if (currentTemplate == null) return;

        TextField nameField = new TextField(currentTemplate.getName());
        TextArea descField = new TextArea(currentTemplate.getDescription() != null
                ? currentTemplate.getDescription() : "");
        descField.setPrefRowCount(3);

        Dialog<ButtonType> dialog = buildTemplateFormDialog(
                "Редактировать шаблон", nameField, descField);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nameField.getText().trim().isEmpty()) {
                showAlert("Ошибка", "Наименование обязательно", Alert.AlertType.WARNING);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            String name = nameField.getText().trim();
            String description = descField.getText().trim();

            runAsync(() -> ProductTemplateClient.updateTemplate(
                            currentTemplate.getId(),
                            new UpdateProductTemplateRequest(name,
                                    description.isEmpty() ? null : description)),
                    "Шаблон обновлён");
        });
    }

    @javafx.fxml.FXML
    private void handleRefresh() {
        if (currentSeries != null) {
            showForSeries(currentSeries);
        }
    }


    // ==========================================================
    // LIFECYCLE: VERSION
    // ==========================================================
    @javafx.fxml.FXML
    private void handleCreateVersion() {
        if (currentTemplate == null) return;

        Dialog<ButtonType> dialog = buildDialog("Создать версию");
        Label info = new Label(
                "Будет создана новая DRAFT-версия. Поля и правило маркировки\n" +
                        "скопируются из текущей опубликованной версии (если есть).");
        info.setWrapText(true);
        dialog.getDialogPane().setContent(info);

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;
            runAsync(() -> ProductTemplateClient.createNewVersion(
                            currentTemplate.getId(),
                            new CreateProductTemplateVersionRequest(null, null)),
                    "Версия создана");
        });
    }

    @javafx.fxml.FXML
    private void handlePublish() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"DRAFT".equals(selected.getStatus())) return;

        if (!confirm("Опубликовать версию v" + selected.getVersion() + "?",
                "Предыдущая PUBLISHED (если была) станет LEGACY.")) return;

        runAsync(() -> ProductTemplateVersionClient.publishVersion(selected.getId()),
                "Версия опубликована");
    }

    @javafx.fxml.FXML
    private void handleDeprecate() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"LEGACY".equals(selected.getStatus())) return;

        if (!confirm("Пометить версию v" + selected.getVersion() + " как устаревшую?",
                "Новые карточки по этой версии создавать будет нельзя. " +
                        "Существующие карточки продолжат работать")) return;

        runAsync(() -> ProductTemplateVersionClient.deprecateVersion(selected.getId()),
                "Версия помечена как DEPRECATED");
    }

    @javafx.fxml.FXML
    private void handleArchive() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"DEPRECATED".equals(selected.getStatus())) return;

        if (!confirm("Архивировать версию v" + selected.getVersion() + "?",
                "Версия будет скрыта из UI. Существующие карточки продолжат работать.")) return;

        runAsync(() -> ProductTemplateVersionClient.archiveVersion(selected.getId()),
                "Версия архивирована");
    }

    @javafx.fxml.FXML
    private void handleDeleteVersion() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        String status = selected.getStatus();
        if (!"DRAFT".equals(status) && !"ARCHIVED".equals(status)) return;

        if (!confirm("Удалить версию v" + selected.getVersion() + "?",
                "Это действие необратимо.")) return;

        runAsync(() -> ProductTemplateVersionClient.deleteVersion(selected.getId()),
                "Версия удалена");
    }


    // ==========================================================
    // CLONE
    // ==========================================================
    @javafx.fxml.FXML
    private void handleClone() {
        if (currentTemplate == null) return;

        Dialog<ButtonType> dialog = buildDialog("Клонировать шаблон");
        dialog.setResizable(true);

        TextField nameField = new TextField("Клон: " + currentTemplate.getName());
        nameField.setPromptText("Наименование нового шаблона");

        TreeView<Object> treeView = new TreeView<>();
        treeView.setPrefHeight(300);
        treeView.setShowRoot(false);
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof ProductTypeDto t) {
                    setText("📁 " + t.getDisplayName());
                } else if (item instanceof com.fanproduction.gui.dto.response.ProductSpeciesDto s) {
                    setText("  📁 " + s.getDisplayName());
                } else if (item instanceof ProductSeriesDto s) {
                    setText("    📄 " + s.getDisplayName());
                } else {
                    setText(item.toString());
                }
            }
        });

        Label hint = new Label("Выберите целевую серию (у неё не должно быть шаблона):");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        GridPane grid = buildGrid();
        int row = 0;
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(hint, 0, row, 2, 1);
        row++;
        grid.add(treeView, 0, row, 2, 1);
        row++;
        grid.add(errorLabel, 0, row, 2, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefWidth(520);

        new Thread(() -> {
            try {
                List<ProductTypeDto> types = ProductTypeClient.getTree();
                TreeItem<Object> root = ProductTreeBuilder.buildTree(types);
                Platform.runLater(() -> treeView.setRoot(root));
            } catch (Exception e) {
                Platform.runLater(() -> errorLabel.setText(
                        "Не удалось загрузить дерево: " + e.getMessage()));
            }
        }, "TemplateDetail-cloneTree").start();

        Button okButton = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        okButton.setDisable(true);
        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, o, n) -> okButton.setDisable(
                        n == null || !(n.getValue() instanceof ProductSeriesDto)));

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            TreeItem<Object> sel = treeView.getSelectionModel().getSelectedItem();
            if (sel == null || !(sel.getValue() instanceof ProductSeriesDto targetSeries)) return;

            String newName = nameField.getText().trim();

            runAsync(() -> ProductTemplateClient.cloneTemplate(
                            currentTemplate.getId(),
                            new CloneProductTemplateRequest(
                                    targetSeries.getId(),
                                    newName.isEmpty() ? null : newName)),
                    "Шаблон клонирован");
        });
    }

    @javafx.fxml.FXML
    private void handleOpenFieldsEditor() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"DRAFT".equals(selected.getStatus())) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/fanproduction/gui/view/TemplateEditorView.fxml"));
            javafx.scene.Parent root = loader.load();

            TemplateEditorController controller = loader.getController();
            controller.init(selected, () -> {
                // После сохранения — перезагрузить панель шаблона
                if (currentSeries != null) {
                    showForSeries(currentSeries);
                }
            });

            javafx.stage.Stage editorStage = new javafx.stage.Stage();
            editorStage.setTitle("Редактор полей — v" + selected.getVersion());
            editorStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            editorStage.initOwner(owner());
            editorStage.setScene(new javafx.scene.Scene(root, 950, 700));
            editorStage.setMinWidth(800);
            editorStage.setMinHeight(600);
            editorStage.show();
        } catch (Exception e) {
            log.error("Не удалось открыть редактор полей", e);
            showAlert("Ошибка",
                    "Не удалось открыть редактор полей: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @javafx.fxml.FXML
    private void handleOpenRuleEditor() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null || !"DRAFT".equals(selected.getStatus())) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/fanproduction/gui/view/MarkingRuleEditorView.fxml"));
            javafx.scene.Parent root = loader.load();

            MarkingRuleEditorController controller = loader.getController();
            controller.init(selected, () -> {
                // После сохранения — перезагрузить панель шаблона
                if (currentSeries != null) {
                    showForSeries(currentSeries);
                }
            });

            javafx.stage.Stage editorStage = new javafx.stage.Stage();
            editorStage.setTitle("Редактор правила — v" + selected.getVersion());
            editorStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            editorStage.initOwner(owner());
            editorStage.setScene(new javafx.scene.Scene(root, 1000, 720));
            editorStage.setMinWidth(900);
            editorStage.setMinHeight(650);
            editorStage.show();
        } catch (Exception e) {
            log.error("Не удалось открыть редактор правила", e);
            showAlert("Ошибка",
                    "Не удалось открыть редактор правила: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }

    @javafx.fxml.FXML
    private void handlePreview() {
        ProductTemplateVersionDto selected = versionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/fanproduction/gui/view/TemplatePreviewView.fxml"));
            javafx.scene.Parent root = loader.load();

            TemplatePreviewController controller = loader.getController();
            controller.init(selected);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Предпросмотр — v" + selected.getVersion());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.initOwner(owner());
            stage.setScene(new javafx.scene.Scene(root, 1000, 720));
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.show();
        } catch (Exception e) {
            log.error("Не удалось открыть предпросмотр", e);
            showAlert("Ошибка",
                    "Не удалось открыть предпросмотр: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
    }


    // ==========================================================
    // УТИЛИТЫ — тонкие обёртки над TemplateControllerHelper
    // ==========================================================
    private Window owner() {
        return TemplateControllerHelper.resolveOwner(versionsTable);
    }

    private void runAsync(TemplateControllerHelper.ThrowingRunnable action, String successMessage) {
        TemplateControllerHelper.runAsync(owner(), action, successMessage,
                currentSeries != null ? () -> showForSeries(currentSeries) : null);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        TemplateControllerHelper.showAlert(owner(), title, message, type);
    }

    private Dialog<ButtonType> buildDialog(String title) {
        return TemplateControllerHelper.buildDialog(title, owner());
    }

    private GridPane buildGrid() {
        return TemplateControllerHelper.buildGrid();
    }

    private boolean confirm(String header, String content) {
        return TemplateControllerHelper.confirm(owner(), "Подтверждение", header, content);
    }

    /**
     * Построить диалог с формой «наименование + описание».
     * <p>
     * Используется в {@link #handleCreateTemplate()} и {@link #handleEditTemplate()},
     * чтобы не дублировать построение GridPane и setContent.
     */
    private Dialog<ButtonType> buildTemplateFormDialog(String title,
                                                       TextField nameField,
                                                       TextArea descField) {
        Dialog<ButtonType> dialog = buildDialog(title);

        GridPane grid = buildGrid();
        int row = 0;
        grid.add(new Label("Наименование:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(descField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        return dialog;
    }
}
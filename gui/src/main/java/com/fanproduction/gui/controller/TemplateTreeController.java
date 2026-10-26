package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.FanSeriesClient;
import com.fanproduction.gui.client.FanSpeciesClient;
import com.fanproduction.gui.client.FanTypeClient;
import com.fanproduction.gui.dto.request.fan.CreateFanSeriesRequest;
import com.fanproduction.gui.dto.request.fan.CreateFanSpeciesRequest;
import com.fanproduction.gui.dto.request.fan.CreateFanTypeRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanSeriesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanSpeciesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateFanTypeRequest;
import com.fanproduction.gui.dto.response.FanSeriesDto;
import com.fanproduction.gui.dto.response.FanSpeciesDto;
import com.fanproduction.gui.dto.response.FanTypeDto;
import com.fanproduction.gui.util.FanTreeBuilder;
import com.fanproduction.gui.util.TemplateControllerHelper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Контроллер вкладки «Конструктор шаблонов» (Спринт 9, US9.1).
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Загрузку дерева Тип → Вид → Серия.</li>
 *   <li>CRUD типов, видов, серий через контекстное меню.</li>
 *   <li>Загрузку правой панели шаблона {@code TemplateDetailView}
 *       (лениво, при первом выборе серии).</li>
 * </ul>
 * <p>
 * Общие утилиты ({@code runAsync}, {@code showAlert}, {@code buildDialog},
 * {@code confirmDelete}) — из {@link TemplateControllerHelper}.
 */
@Slf4j
public class TemplateTreeController {

    // ==========================================================
    // FXML
    // ==========================================================

    @javafx.fxml.FXML private TreeView<Object> hierarchyTree;
    @javafx.fxml.FXML private StackPane detailPane;
    @javafx.fxml.FXML private Button createTypeButton;
    @javafx.fxml.FXML private Button refreshButton;
    @javafx.fxml.FXML private Label statusLabel;

    // ==========================================================
    // СОСТОЯНИЕ
    // ==========================================================

    /** Лениво загружаемая правая панель шаблона. */
    private Parent templateDetailView;
    private TemplateDetailController templateDetailController;

    // ==========================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ==========================================================

    @javafx.fxml.FXML
    private void initialize() {
        setupTreeCellFactory();
        setupTreeSelectionListener();
        loadTree();
    }

    // ==========================================================
    // ЗАГРУЗКА ДЕРЕВА
    // ==========================================================

    @javafx.fxml.FXML
    private void handleRefresh() {
        loadTree();
    }

    /**
     * Загрузить дерево иерархии.
     * <p>
     * Потокобезопасен: может вызываться из любого потока. Обновление UI
     * (statusLabel, applyTree) всегда оборачивается в {@link Platform#runLater}.
     */
    private void loadTree() {
        Platform.runLater(() -> statusLabel.setText("Загрузка…"));

        new Thread(() -> {
            try {
                List<FanTypeDto> tree = FanTypeClient.getTree();
                Platform.runLater(() -> applyTree(tree));
            } catch (Exception e) {
                log.warn("Не удалось загрузить дерево: {}", e.getMessage());
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка загрузки");
                    showAlert("Ошибка",
                            "Не удалось загрузить дерево: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
            }
        }, "TemplateTree-loader").start();
    }

    private void applyTree(List<FanTypeDto> types) {
        TreeItem<Object> root = FanTreeBuilder.buildTree(types);
        hierarchyTree.setRoot(root);
        hierarchyTree.setShowRoot(false);
        statusLabel.setText("Типов: " + (types != null ? types.size() : 0));
    }

    /**
     * Загрузить правую панель шаблона для выбранной серии.
     * <p>
     * Панель {@code TemplateDetailView} создаётся лениво — при первом выборе
     * серии. При последующих выборах переиспользуется тот же контроллер:
     * внутри {@link TemplateDetailController#showForSeries(FanSeriesDto)}
     * сбрасывается состояние и запускается новая загрузка.
     */
    private void loadTemplateDetail(FanSeriesDto series) {
        if (templateDetailView == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(
                        "/com/fanproduction/gui/view/TemplateDetailView.fxml"));
                templateDetailView = loader.load();
                templateDetailController = loader.getController();
            } catch (Exception e) {
                log.error("Не удалось загрузить панель шаблона", e);
                showAlert("Ошибка",
                        "Не удалось открыть панель шаблона: " + e.getMessage(),
                        Alert.AlertType.ERROR);
                return;
            }
        }

        templateDetailController.showForSeries(series);
        detailPane.getChildren().setAll(templateDetailView);
    }

    // ==========================================================
    // CELL FACTORY + СЛУШАТЕЛЬ ВЫБОРА
    // ==========================================================

    private void setupTreeCellFactory() {
        hierarchyTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else if (item instanceof FanTypeDto type) {
                    setText(type.getDisplayName());
                    setStyle("-fx-font-weight: bold;");
                } else if (item instanceof FanSpeciesDto species) {
                    setText(species.getDisplayName());
                    setStyle("-fx-text-fill: #444;");
                } else if (item instanceof FanSeriesDto series) {
                    setText(series.getDisplayName());
                    setStyle("");
                } else {
                    setText(item.toString());
                    setStyle("");
                }
            }
        });
    }

    private void setupTreeSelectionListener() {
        hierarchyTree.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newItem) -> {
                    if (newItem != null && newItem.getValue() instanceof FanSeriesDto series) {
                        loadTemplateDetail(series);
                    } else if (newItem != null) {
                        showDetailPlaceholder("Выбран " + describeNode(newItem.getValue()) + ". " +
                                "Для управления шаблоном выберите серию.");
                    } else {
                        showDetailPlaceholder("Выберите серию в дереве");
                    }
                });

        setupContextMenu();
    }

    private String describeNode(Object value) {
        if (value instanceof FanTypeDto) return "тип";
        if (value instanceof FanSpeciesDto) return "вид";
        return "элемент";
    }

    private void showDetailPlaceholder(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        label.setWrapText(true);
        StackPane.setMargin(label, new Insets(20));
        detailPane.getChildren().setAll(label);
    }

    // ==========================================================
    // КОНТЕКСТНОЕ МЕНЮ
    // ==========================================================

    private void setupContextMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem createChild = new MenuItem("Создать …");
        MenuItem edit = new MenuItem("Редактировать");
        MenuItem delete = new MenuItem("Удалить");
        MenuItem openTemplate = new MenuItem("Открыть шаблон");

        hierarchyTree.setOnContextMenuRequested(event -> {
            TreeItem<Object> item = hierarchyTree.getSelectionModel().getSelectedItem();
            if (item == null || item.getValue() == null) {
                return;
            }

            Object value = item.getValue();
            menu.getItems().clear();

            if (value instanceof FanTypeDto type) {
                createChild.setText("Создать вид");
                createChild.setOnAction(e -> handleCreateSpecies(type));
                edit.setOnAction(e -> handleEditType(type));
                delete.setOnAction(e -> handleDeleteType(type));
                menu.getItems().addAll(createChild, edit, delete);
            } else if (value instanceof FanSpeciesDto species) {
                createChild.setText("Создать серию");
                createChild.setOnAction(e -> handleCreateSeries(species));
                edit.setOnAction(e -> handleEditSpecies(species));
                delete.setOnAction(e -> handleDeleteSpecies(species));
                menu.getItems().addAll(createChild, edit, delete);
            } else if (value instanceof FanSeriesDto series) {
                edit.setOnAction(e -> handleEditSeries(series));
                delete.setOnAction(e -> handleDeleteSeries(series));
                openTemplate.setOnAction(e -> loadTemplateDetail(series));
                menu.getItems().addAll(openTemplate, edit, delete);
            }

            if (!menu.getItems().isEmpty()) {
                menu.show(hierarchyTree, event.getScreenX(), event.getScreenY());
            }
        });
    }

    // ==========================================================
    // ТИП
    // ==========================================================

    @javafx.fxml.FXML
    private void handleCreateType() {
        showTypeDialog(null);
    }

    private void handleEditType(FanTypeDto type) {
        showTypeDialog(type);
    }

    private void handleDeleteType(FanTypeDto type) {
        if (confirmDelete("Удалить тип «" + type.getDisplayName() + "»?",
                "Если у типа есть виды, удаление невозможно.")) {
            runAsync(() -> FanTypeClient.deleteType(type.getId()), "Тип удалён");
        }
    }

    private void showTypeDialog(FanTypeDto existing) {
        Dialog<ButtonType> dialog = buildDialog(
                existing == null ? "Создать тип" : "Редактировать тип");

        TextField nameField = new TextField();
        TextField designationField = new TextField();

        if (existing != null) {
            nameField.setText(existing.getName() != null ? existing.getName() : "");
            designationField.setText(existing.getDesignation() != null ? existing.getDesignation() : "");
        }

        nameField.setPromptText("Вентилятор канальный");
        designationField.setPromptText("VRK-PatAIR (обязательно, уникально)");

        GridPane grid = buildGrid();
        int row = 0;
        grid.add(new Label("Наименование:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Обозначение:*"), 0, row);
        grid.add(designationField, 1, row++);

        dialog.getDialogPane().setContent(grid);

        // Валидация ДО закрытия: пустое designation → диалог остаётся открытым
        Button okButton = (Button) dialog.getDialogPane().lookupButton(
                dialog.getDialogPane().getButtonTypes().get(0));
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (designationField.getText().trim().isEmpty()) {
                showAlert("Ошибка", "Обозначение типа обязательно", Alert.AlertType.WARNING);
                event.consume();
            }
        });

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            String name = nameField.getText().trim();
            String designation = designationField.getText().trim();

            runAsync(() -> {
                if (existing == null) {
                    FanTypeClient.createType(new CreateFanTypeRequest(
                            name.isEmpty() ? null : name, designation));
                } else {
                    FanTypeClient.updateType(existing.getId(),
                            new UpdateFanTypeRequest(
                                    name.isEmpty() ? null : name, designation));
                }
            }, existing == null ? "Тип создан" : "Тип обновлён");
        });
    }

    // ==========================================================
    // ВИД
    // ==========================================================

    private void handleCreateSpecies(FanTypeDto parentType) {
        showSpeciesDialog(parentType.getId(), null);
    }

    private void handleEditSpecies(FanSpeciesDto species) {
        showSpeciesDialog(species.getTypeId(), species);
    }

    private void handleDeleteSpecies(FanSpeciesDto species) {
        if (confirmDelete("Удалить вид «" + species.getDisplayName() + "»?",
                "Если у вида есть серии, удаление невозможно.")) {
            runAsync(() -> FanSpeciesClient.deleteSpecies(species.getId()), "Вид удалён");
        }
    }

    private void showSpeciesDialog(Long typeId, FanSpeciesDto existing) {
        Dialog<ButtonType> dialog = buildDialog(
                existing == null ? "Создать вид" : "Редактировать вид");

        TextField nameField = new TextField();
        if (existing != null && existing.getName() != null) {
            nameField.setText(existing.getName());
        }
        nameField.setPromptText("Прямоугольный (может быть пустым)");

        GridPane grid = buildGrid();
        int row = 0;
        grid.add(new Label("Наименование:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("(если пусто — No_species)"), 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            String name = nameField.getText().trim();

            runAsync(() -> {
                if (existing == null) {
                    FanSpeciesClient.createSpecies(new CreateFanSpeciesRequest(
                            typeId, name.isEmpty() ? null : name));
                } else {
                    FanSpeciesClient.updateSpecies(existing.getId(),
                            new UpdateFanSpeciesRequest(
                                    name.isEmpty() ? null : name));
                }
            }, existing == null ? "Вид создан" : "Вид обновлён");
        });
    }

    // ==========================================================
    // СЕРИЯ
    // ==========================================================

    private void handleCreateSeries(FanSpeciesDto parentSpecies) {
        showSeriesDialog(parentSpecies.getId(), null);
    }

    private void handleEditSeries(FanSeriesDto series) {
        showSeriesDialog(series.getSpeciesId(), series);
    }

    private void handleDeleteSeries(FanSeriesDto series) {
        if (confirmDelete("Удалить серию «" + series.getDisplayName() + "»?", null)) {
            runAsync(() -> FanSeriesClient.deleteSeries(series.getId()), "Серия удалена");
        }
    }

    private void showSeriesDialog(Long speciesId, FanSeriesDto existing) {
        Dialog<ButtonType> dialog = buildDialog(
                existing == null ? "Создать серию" : "Редактировать серию");

        TextField nameField = new TextField();
        if (existing != null && existing.getName() != null) {
            nameField.setText(existing.getName());
        }
        nameField.setPromptText("P, PKV (может быть пустым)");

        GridPane grid = buildGrid();
        int row = 0;
        grid.add(new Label("Наименование:"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("(если пусто — No_series)"), 1, row++);

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait().ifPresent(button -> {
            if (button.getButtonData() != ButtonBar.ButtonData.OK_DONE) return;

            String name = nameField.getText().trim();

            runAsync(() -> {
                if (existing == null) {
                    FanSeriesClient.createSeries(new CreateFanSeriesRequest(
                            speciesId, name.isEmpty() ? null : name));
                } else {
                    FanSeriesClient.updateSeries(existing.getId(),
                            new UpdateFanSeriesRequest(
                                    name.isEmpty() ? null : name));
                }
            }, existing == null ? "Серия создана" : "Серия обновлена");
        });
    }

    // ==========================================================
    // ОБЩИЕ УТИЛИТЫ — тонкие обёртки над TemplateControllerHelper
    // ==========================================================

    private Window owner() {
        return TemplateControllerHelper.resolveOwner(hierarchyTree);
    }

    private void runAsync(TemplateControllerHelper.ThrowingRunnable action, String successMessage) {
        TemplateControllerHelper.runAsync(owner(), action, successMessage, this::loadTree);
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

    private boolean confirmDelete(String header, String additionalHint) {
        return TemplateControllerHelper.confirmDelete(owner(), header, additionalHint);
    }
}
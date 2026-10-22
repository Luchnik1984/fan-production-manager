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
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

/**
 * Контроллер вкладки «Конструктор шаблонов».
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

    /**
     * Колбэк при выборе серии. В US9.2 будет заменён на открытие панели шаблона.
     * Пока — просто логирует.
     */
    private final Consumer<FanSeriesDto> onSeriesSelected = series ->
            log.debug("Серия выбрана: {} (id={})", series.getName(), series.getId());

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
                        onSeriesSelected.accept(series);
                        showSeriesPlaceholder(series);
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

    private void showSeriesPlaceholder(FanSeriesDto series) {
        VBox box = new VBox(10);
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-padding: 20px;");

        Label title = new Label("Серия: " + series.getDisplayName());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label hint = new Label("Панель управления шаблоном появится в следующем подшаге (US9.2).");
        hint.setStyle("-fx-text-fill: #888; -fx-font-size: 12px;");
        hint.setWrapText(true);

        box.getChildren().addAll(title, hint);
        detailPane.getChildren().setAll(box);
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
                openTemplate.setOnAction(e -> onSeriesSelected.accept(series));
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

        dialog.showAndWait().ifPresent(button -> {
            if (button != dialog.getDialogPane().getButtonTypes().get(0)) return;

            String name = nameField.getText().trim();
            String designation = designationField.getText().trim();

            if (designation.isEmpty()) {
                showAlert("Ошибка", "Обозначение типа обязательно", Alert.AlertType.WARNING);
                return;
            }

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
            if (button != dialog.getDialogPane().getButtonTypes().get(0)) return;

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
            if (button != dialog.getDialogPane().getButtonTypes().get(0)) return;

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
    // ОБЩИЕ УТИЛИТЫ
    // ==========================================================

    private Dialog<ButtonType> buildDialog(String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.initModality(Modality.WINDOW_MODAL);

        Window owner = getOwnerWindow();
        if (owner != null) {
            dialog.initOwner(owner);
        }

        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        return dialog;
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }

    private Window getOwnerWindow() {
        return hierarchyTree.getScene() != null ? hierarchyTree.getScene().getWindow() : null;
    }

    private boolean confirmDelete(String message, String additionalHint) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(message);

        StringBuilder content = new StringBuilder("Это действие необратимо.");
        if (additionalHint != null) {
            content.append("\n").append(additionalHint);
        }
        alert.setContentText(content.toString());

        Window owner = getOwnerWindow();
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }

        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Выполнить асинхронное действие и, при успехе, перезагрузить дерево.
     * <p>
     * <b>Важно:</b> {@code action} должна содержать <b>только</b> сетевой вызов.
     * Перезагрузка дерева ({@link #loadTree()}) выполняется отдельно — после
     * успешного {@code action}, в FX-потоке.
     */
    private void runAsync(ThrowingRunnable action, String successMessage) {
        new Thread(() -> {
            try {
                action.run();
                // Успех: логируем и перезагружаем дерево — обе операции в FX-потоке
                Platform.runLater(() -> {
                    log.info(successMessage);
                    loadTree();
                });
            } catch (Exception e) {
                log.warn("{} — ошибка: {}", successMessage, e.getMessage());
                Platform.runLater(() -> {
                    Alert.AlertType type = isBusinessError(e.getMessage())
                            ? Alert.AlertType.WARNING
                            : Alert.AlertType.ERROR;
                    String title = type == Alert.AlertType.WARNING
                            ? "Действие невозможно"
                            : "Ошибка";
                    showAlert(title, e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка", type);
                });
            }
        }, "TemplateTree-action").start();
    }

    private boolean isBusinessError(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("уже существует")
                || lower.contains("нельзя")
                || lower.contains("невозможно")
                || lower.contains("не найден")
                || lower.contains("не найдена");
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Window owner = getOwnerWindow();
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.showAndWait();
    }

    /**
     * Функциональный интерфейс, позволяющий бросать checked-исключения.
     * <p>
     * Используется в {@link #runAsync(ThrowingRunnable, String)} — методы клиентов
     * объявлены как {@code throws Exception}, а {@link Runnable#run()} это запрещает.
     */
    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
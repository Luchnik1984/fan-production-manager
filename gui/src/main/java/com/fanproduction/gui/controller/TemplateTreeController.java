package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductSeriesClient;
import com.fanproduction.gui.client.ProductSpeciesClient;
import com.fanproduction.gui.client.ProductTypeClient;
import com.fanproduction.gui.client.ProductTemplateClient;
import com.fanproduction.gui.dto.request.fan.CreateProductSeriesRequest;
import com.fanproduction.gui.dto.request.fan.CreateProductSpeciesRequest;
import com.fanproduction.gui.dto.request.fan.CreateProductTypeRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductSeriesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductSpeciesRequest;
import com.fanproduction.gui.dto.request.fan.UpdateProductTypeRequest;
import com.fanproduction.gui.dto.response.ProductSeriesDto;
import com.fanproduction.gui.dto.response.ProductSpeciesDto;
import com.fanproduction.gui.dto.response.ProductTypeDto;
import com.fanproduction.gui.dto.response.ProductTemplateDto;
import com.fanproduction.gui.dto.response.ProductTemplateVersionDto;
import com.fanproduction.gui.util.ProductTreeBuilder;
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
                List<ProductTypeDto> tree = ProductTypeClient.getTree();
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

    private void applyTree(List<ProductTypeDto> types) {
        TreeItem<Object> root = ProductTreeBuilder.buildTree(types);
        hierarchyTree.setRoot(root);
        hierarchyTree.setShowRoot(false);
        statusLabel.setText("Типов: " + (types != null ? types.size() : 0));
    }

    /**
     * Загрузить правую панель шаблона для выбранной серии.
     * <p>
     * Панель {@code TemplateDetailView} создаётся лениво — при первом выборе
     * серии. При последующих выборах переиспользуется тот же контроллер:
     * внутри {@link TemplateDetailController#showForSeries(ProductSeriesDto)}
     * сбрасывается состояние и запускается новая загрузка.
     */
    private void loadTemplateDetail(ProductSeriesDto series) {
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

    /**
     * Открыть редактор правила маркировки прямо из дерева (shortcut).
     * <p>
     * Проверяет, что:
     * <ul>
     *   <li>У серии есть шаблон;</li>
     *   <li>У шаблона есть DRAFT-версия.</li>
     * </ul>
     * Если DRAFT нет — предлагает создать новую версию (или сообщает,
     * если нет даже шаблона).
     * <p>
     * Открывает {@link MarkingRuleEditorController} в отдельном Stage,
     * минуя панель шаблона.
     */
    private void handleOpenRuleEditorFromTree(ProductSeriesDto series) {
        new Thread(() -> {
            try {
                ProductTemplateDto template;
                try {
                    template = ProductTemplateClient.getTemplateBySeries(series.getId());
                } catch (Exception e) {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("не найден")) {
                        Platform.runLater(() -> showAlert("Нет шаблона",
                                "Для серии «" + series.getDisplayName() + "» ещё нет шаблона.\n" +
                                        "Создайте его на панели шаблона (выберите серию в дереве).",
                                Alert.AlertType.INFORMATION));
                        return;
                    }
                    throw e;
                }

                ProductTemplateVersionDto draft = ProductTemplateClient.getDraftVersion(template.getId());
                if (draft == null) {
                    Platform.runLater(() -> showAlert("Нет DRAFT-версии",
                            "У шаблона серии «" + series.getDisplayName() + "» нет DRAFT-версии.\n" +
                                    "Откройте панель шаблона и создайте новую версию.",
                            Alert.AlertType.INFORMATION));
                    return;
                }

                Platform.runLater(() -> openMarkingRuleEditor(draft));
            } catch (Exception e) {
                log.warn("Ошибка при открытии редактора правила для серии {}: {}",
                        series.getId(), e.getMessage());
                Platform.runLater(() -> showAlert("Ошибка",
                        "Не удалось открыть редактор правила: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }, "TemplateTree-openRule").start();
    }

    /**
     * Открыть редактор правила маркировки для указанной DRAFT-версии.
     */
    private void openMarkingRuleEditor(ProductTemplateVersionDto draft) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(
                    "/com/fanproduction/gui/view/MarkingRuleEditorView.fxml"));
            Parent root = loader.load();

            MarkingRuleEditorController controller = loader.getController();
            controller.init(draft, () -> {
                // После сохранения — перезагрузить панель шаблона, если серия выбрана
                if (templateDetailController != null
                        && hierarchyTree.getSelectionModel().getSelectedItem() != null
                        && hierarchyTree.getSelectionModel().getSelectedItem().getValue()
                        instanceof ProductSeriesDto current) {
                    templateDetailController.showForSeries(current);
                }
            });

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Редактор правила — v" + draft.getVersion());
            stage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            stage.initOwner(owner());
            stage.setScene(new javafx.scene.Scene(root, 1000, 720));
            stage.setMinWidth(900);
            stage.setMinHeight(650);
            stage.show();
        } catch (Exception e) {
            log.error("Не удалось открыть редактор правила", e);
            showAlert("Ошибка",
                    "Не удалось открыть редактор правила: " + e.getMessage(),
                    Alert.AlertType.ERROR);
        }
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
                } else if (item instanceof ProductTypeDto type) {
                    setText(type.getDisplayName());
                    setStyle("-fx-font-weight: bold;");
                } else if (item instanceof ProductSpeciesDto species) {
                    setText(species.getDisplayName());
                    setStyle("-fx-text-fill: #444;");
                } else if (item instanceof ProductSeriesDto series) {
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
                    if (newItem != null && newItem.getValue() instanceof ProductSeriesDto series) {
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
        if (value instanceof ProductTypeDto) return "тип";
        if (value instanceof ProductSpeciesDto) return "вид";
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
        MenuItem openRule = new MenuItem("⚙ Открыть редактор правила");

        hierarchyTree.setOnContextMenuRequested(event -> {
            TreeItem<Object> item = hierarchyTree.getSelectionModel().getSelectedItem();
            if (item == null || item.getValue() == null) {
                return;
            }

            Object value = item.getValue();
            menu.getItems().clear();

            if (value instanceof ProductTypeDto type) {
                createChild.setText("Создать вид");
                createChild.setOnAction(e -> handleCreateSpecies(type));
                edit.setOnAction(e -> handleEditType(type));
                delete.setOnAction(e -> handleDeleteType(type));
                menu.getItems().addAll(createChild, edit, delete);
            } else if (value instanceof ProductSpeciesDto species) {
                createChild.setText("Создать серию");
                createChild.setOnAction(e -> handleCreateSeries(species));
                edit.setOnAction(e -> handleEditSpecies(species));
                delete.setOnAction(e -> handleDeleteSpecies(species));
                menu.getItems().addAll(createChild, edit, delete);
            }else if (value instanceof ProductSeriesDto series) {
                edit.setOnAction(e -> handleEditSeries(series));
                delete.setOnAction(e -> handleDeleteSeries(series));
                openTemplate.setOnAction(e -> loadTemplateDetail(series));
                openRule.setOnAction(e -> handleOpenRuleEditorFromTree(series));

                menu.getItems().addAll(openTemplate, openRule, edit, delete);
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

    private void handleEditType(ProductTypeDto type) {
        showTypeDialog(type);
    }

    private void handleDeleteType(ProductTypeDto type) {
        if (confirmDelete("Удалить тип «" + type.getDisplayName() + "»?",
                "Если у типа есть виды, удаление невозможно.")) {
            runAsync(() -> ProductTypeClient.deleteType(type.getId()), "Тип удалён");
        }
    }

    private void showTypeDialog(ProductTypeDto existing) {
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
                    ProductTypeClient.createType(new CreateProductTypeRequest(
                            name.isEmpty() ? null : name, designation));
                } else {
                    ProductTypeClient.updateType(existing.getId(),
                            new UpdateProductTypeRequest(
                                    name.isEmpty() ? null : name, designation));
                }
            }, existing == null ? "Тип создан" : "Тип обновлён");
        });
    }

    // ==========================================================
    // ВИД
    // ==========================================================

    private void handleCreateSpecies(ProductTypeDto parentType) {
        showSpeciesDialog(parentType.getId(), null);
    }

    private void handleEditSpecies(ProductSpeciesDto species) {
        showSpeciesDialog(species.getTypeId(), species);
    }

    private void handleDeleteSpecies(ProductSpeciesDto species) {
        if (confirmDelete("Удалить вид «" + species.getDisplayName() + "»?",
                "Если у вида есть серии, удаление невозможно.")) {
            runAsync(() -> ProductSpeciesClient.deleteSpecies(species.getId()), "Вид удалён");
        }
    }

    private void showSpeciesDialog(Long typeId, ProductSpeciesDto existing) {
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
                    ProductSpeciesClient.createSpecies(new CreateProductSpeciesRequest(
                            typeId, name.isEmpty() ? null : name));
                } else {
                    ProductSpeciesClient.updateSpecies(existing.getId(),
                            new UpdateProductSpeciesRequest(
                                    name.isEmpty() ? null : name));
                }
            }, existing == null ? "Вид создан" : "Вид обновлён");
        });
    }

    // ==========================================================
    // СЕРИЯ
    // ==========================================================

    private void handleCreateSeries(ProductSpeciesDto parentSpecies) {
        showSeriesDialog(parentSpecies.getId(), null);
    }

    private void handleEditSeries(ProductSeriesDto series) {
        showSeriesDialog(series.getSpeciesId(), series);
    }

    private void handleDeleteSeries(ProductSeriesDto series) {
        if (confirmDelete("Удалить серию «" + series.getDisplayName() + "»?", null)) {
            runAsync(() -> ProductSeriesClient.deleteSeries(series.getId()), "Серия удалена");
        }
    }

    private void showSeriesDialog(Long speciesId, ProductSeriesDto existing) {
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
                    ProductSeriesClient.createSeries(new CreateProductSeriesRequest(
                            speciesId, name.isEmpty() ? null : name));
                } else {
                    ProductSeriesClient.updateSeries(existing.getId(),
                            new UpdateProductSeriesRequest(
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
package com.fanproduction.gui.component;

import com.fanproduction.gui.client.FanTemplateClient;
import com.fanproduction.gui.client.FanTypeClient;
import com.fanproduction.gui.dto.response.FanSeriesDto;
import com.fanproduction.gui.dto.response.FanSpeciesDto;
import com.fanproduction.gui.dto.response.FanTemplateDto;
import com.fanproduction.gui.dto.response.FanTemplateVersionDto;
import com.fanproduction.gui.dto.response.FanTypeDto;
import com.fanproduction.gui.util.FanTreeBuilder;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.List;

/**
 * Диалог выбора версии шаблона FAN.
 * <p>
 * Сценарий:
 * <ol>
 *   <li>Пользователь выбирает серию из дерева Тип → Вид → Серия.</li>
 *   <li>Справа загружаются доступные версии шаблона (PUBLISHED + LEGACY).</li>
 *   <li>Пользователь выбирает версию.</li>
 *   <li>Нажимает «Создать карточку».</li>
 * </ol>
 * <p>
 * Результат: {@link #getSelectedVersionId()}.
 */
public class FanTemplateSelectionDialog {

    private final Stage dialog;
    private final TreeView<Object> treeView;
    private final ListView<FanTemplateVersionDto> versionsList;
    private final Button selectButton;
    private final Label statusLabel;

    @Getter
    private Long selectedVersionId;
    @Getter
    private FanTemplateVersionDto selectedVersion;

    private Long selectedSeriesId;
    private FanTemplateDto selectedTemplate;

    public FanTemplateSelectionDialog(Stage owner) {
        this.dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Выбор шаблона вентилятора");
        dialog.setResizable(true);

        treeView = new TreeView<>();
        treeView.setPrefWidth(350);
        treeView.setPrefHeight(500);
        treeView.setShowRoot(false);

        versionsList = new ListView<>();
        versionsList.setPrefWidth(350);
        versionsList.setPrefHeight(400);

        versionsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FanTemplateVersionDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String label = "v" + item.getVersion() + " [" + item.getStatus() + "]";
                    if (item.getPublishedAt() != null) {
                        label += "  —  " + item.getPublishedAt().toLocalDate();
                    }
                    setText(label);
                }
            }
        });

        statusLabel = new Label("Загрузка...");

        selectButton = new Button("Создать карточку");
        selectButton.setDefaultButton(true);
        selectButton.setDisable(true);
        selectButton.setOnAction(e -> onSelect());

        Button cancelButton = new Button("Отмена");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, selectButton, cancelButton);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        VBox leftBox = new VBox(5, new Label("Серия:"), treeView);
        VBox rightBox = new VBox(5, new Label("Версия шаблона:"), versionsList);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        HBox content = new HBox(10, leftBox, rightBox);
        content.setPadding(new Insets(10));

        VBox root = new VBox(10, content, statusLabel, buttons);
        root.setPadding(new Insets(10));

        dialog.setScene(new Scene(root, 800, 600));

        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> onTreeSelection(newVal));

        versionsList.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> onVersionSelection(newVal));

        loadTree();
    }

    public void show() {
        dialog.showAndWait();
    }

    // ==========================================================
    // ЗАГРУЗКА ДЕРЕВА
    // ==========================================================

    private void loadTree() {
        statusLabel.setText("Загрузка дерева...");
        new Thread(() -> {
            try {
                List<FanTypeDto> tree = FanTypeClient.getTree();
                Platform.runLater(() -> {
                    TreeItem<Object> rootItem = FanTreeBuilder.buildTree(tree);
                    treeView.setRoot(rootItem);
                    treeView.setCellFactory(tv -> new TreeCell<>() {
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else if (item instanceof FanTypeDto t) {
                                setText(t.getDisplayName());
                            } else if (item instanceof FanSpeciesDto s) {
                                setText(s.getDisplayName());
                            } else if (item instanceof FanSeriesDto s) {
                                setText(s.getDisplayName());
                            } else {
                                setText(item.toString());
                            }
                        }
                    });
                    statusLabel.setText("Выберите серию");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка загрузки: " + e.getMessage()));
            }
        }).start();
    }

    private void onTreeSelection(TreeItem<Object> item) {
        if (item == null || !(item.getValue() instanceof FanSeriesDto series)) {
            selectedSeriesId = null;
            versionsList.getItems().clear();
            selectButton.setDisable(true);
            return;
        }

        selectedSeriesId = series.getId();
        statusLabel.setText("Загрузка шаблона для серии " + series.getDisplayName() + "...");
        versionsList.getItems().clear();
        selectButton.setDisable(true);

        new Thread(() -> {
            try {
                FanTemplateDto template = FanTemplateClient.getTemplateBySeries(selectedSeriesId);
                if (template == null) {
                    Platform.runLater(() -> statusLabel.setText("Для серии нет шаблона"));
                    return;
                }
                selectedTemplate = template;
                List<FanTemplateVersionDto> versions =
                        FanTemplateClient.getAvailableVersions(template.getId());

                Platform.runLater(() -> {
                    versionsList.getItems().setAll(versions);
                    if (versions.isEmpty()) {
                        statusLabel.setText("Нет доступных версий (PUBLISHED/LEGACY)");
                    } else {
                        statusLabel.setText("Выберите версию");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }).start();
    }

    private void onVersionSelection(FanTemplateVersionDto version) {
        if (version == null) {
            selectButton.setDisable(true);
            return;
        }
        selectedVersion = version;
        selectedVersionId = version.getId();
        selectButton.setDisable(false);
    }

    private void onSelect() {
        if (selectedVersionId == null) {
            return;
        }
        dialog.close();
    }
}

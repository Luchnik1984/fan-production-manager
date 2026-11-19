package com.fanproduction.gui.component;

import com.fanproduction.gui.client.ProductSectionClient;
import com.fanproduction.gui.client.ProductTemplateClient;
import com.fanproduction.gui.client.ProductTypeClient;
import com.fanproduction.gui.dto.response.ProductSectionDto;
import com.fanproduction.gui.dto.response.ProductSeriesDto;
import com.fanproduction.gui.dto.response.ProductSpeciesDto;
import com.fanproduction.gui.dto.response.ProductTemplateDto;
import com.fanproduction.gui.dto.response.ProductTemplateVersionDto;
import com.fanproduction.gui.dto.response.ProductTypeDto;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;

/**
 * Единый диалог создания карточки продукции.
 * <p>
 * Заменяет два последовательных окна («выбор типа карточки» → «выбор шаблона»).
 * Слева — дерево продукции, справа — то, что нужно для выбранного типа:
 * <ul>
 *   <li>для <b>динамического</b> типа: выбрать вид → серию → версию шаблона
 *       (PUBLISHED или LEGACY);</li>
 *   <li>для <b>системного</b> типа (двигатель, колёса): версия не нужна —
 *       показывается «Форма по умолчанию (хардкод)», кнопка сразу активна.</li>
 * </ul>
 * Результат: {@link #getCardType()} + {@link #getSelectedVersionId()}
 * (для системного типа {@code null}).
 */
public class CardCreationDialog {

    private final Stage dialog;
    private final TreeView<Object> treeView;
    private final ListView<ProductTemplateVersionDto> versionsList;
    private final Label defaultFormLabel;
    private final Button createButton;
    private final Label statusLabel;

    /** Код типа карточки: MOTOR / MOTOR_WHEEL / RADIAL_WHEEL / AXIAL_WHEEL / DYNAMIC. */
    @Getter
    private String cardType;

    /** Выбранная версия шаблона; для системного типа — {@code null}. */
    @Getter
    private Long selectedVersionId;

    @Getter
    private ProductTemplateVersionDto selectedVersion;

    public CardCreationDialog(Stage owner) {
        this.dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Создание карточки продукции");
        dialog.setResizable(true);

        treeView = new TreeView<>();
        treeView.setPrefWidth(380);
        treeView.setPrefHeight(500);
        treeView.setShowRoot(true);

        versionsList = new ListView<>();
        versionsList.setPrefHeight(400);
        versionsList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProductTemplateVersionDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                String label = "v" + item.getVersion() + " [" + item.getStatus() + "]";
                if (item.getPublishedAt() != null) {
                    label += "  —  " + item.getPublishedAt().toLocalDate();
                }
                setText(label);
            }
        });

        defaultFormLabel = new Label(
                "Для этого типа используется фиксированная форма (хардкод).\n"
                        + "Версия шаблона не требуется — нажмите «Создать».");
        defaultFormLabel.setWrapText(true);
        defaultFormLabel.setStyle("-fx-text-fill: #555; -fx-padding: 10;");
        defaultFormLabel.setVisible(false);
        defaultFormLabel.setManaged(false);

        statusLabel = new Label("Загрузка...");

        createButton = new Button("Создать");
        createButton.setDefaultButton(true);
        createButton.setDisable(true);
        createButton.setOnAction(e -> dialog.close());

        Button cancelButton = new Button("Отмена");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, createButton, cancelButton);
        buttons.setPadding(new Insets(10, 0, 0, 0));

        VBox rightBox = new VBox(5,
                new Label("Версия шаблона / форма:"),
                versionsList,
                defaultFormLabel);
        VBox.setVgrow(versionsList, Priority.ALWAYS);

        VBox leftBox = new VBox(5, new Label("Изделие:"), treeView);
        HBox.setHgrow(leftBox, Priority.ALWAYS);

        HBox content = new HBox(10, leftBox, rightBox);
        HBox.setHgrow(rightBox, Priority.ALWAYS);
        content.setPadding(new Insets(10));

        VBox root = new VBox(10, content, statusLabel, buttons);
        root.setPadding(new Insets(10));
        VBox.setVgrow(content, Priority.ALWAYS);

        dialog.setScene(new Scene(root, 860, 620));

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
    // ДЕРЕВО
    // ==========================================================

    private void loadTree() {
        statusLabel.setText("Загрузка дерева...");
        new Thread(() -> {
            try {
                List<ProductSectionDto> sections = ProductSectionClient.getTree();
                List<ProductTypeDto> unassigned = ProductTypeClient.getAllTypes().stream()
                        .filter(type -> type.getSectionId() == null)
                        .toList();
                Platform.runLater(() -> {
                    TreeItem<Object> root = new TreeItem<>("Типы продукции");
                    sections.forEach(section -> root.getChildren().add(buildSectionItem(section)));
                    if (!unassigned.isEmpty()) {
                        TreeItem<Object> noSection = new TreeItem<>("Без раздела");
                        unassigned.stream()
                                .sorted(Comparator.comparing(this::sortKey))
                                .forEach(type -> noSection.getChildren().add(buildTypeItem(type)));
                        root.getChildren().add(noSection);
                    }
                    root.setExpanded(true);

                    treeView.setRoot(root);
                    treeView.setCellFactory(tv -> new TreeCell<>() {
                        @Override
                        protected void updateItem(Object item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                                setStyle("");
                            } else if (item instanceof String title) {
                                setText(title);
                                setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
                            } else if (item instanceof ProductSectionDto section) {
                                setText("📁 " + section.getName());
                                setStyle("-fx-font-weight: bold; -fx-text-fill: #2b5797;");
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
                            }
                        }
                    });
                    statusLabel.setText("Выберите изделие в дереве");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка загрузки: " + e.getMessage()));
            }
        }, "CardCreationDialog-loader").start();
    }

    private TreeItem<Object> buildSectionItem(ProductSectionDto section) {
        TreeItem<Object> item = new TreeItem<>(section);
        if (section.getSections() != null) {
            section.getSections().forEach(child -> item.getChildren().add(buildSectionItem(child)));
        }
        if (section.getTypes() != null) {
            section.getTypes().stream()
                    .sorted(Comparator.comparing(this::sortKey))
                    .forEach(type -> item.getChildren().add(buildTypeItem(type)));
        }
        item.setExpanded(true);
        return item;
    }

    private TreeItem<Object> buildTypeItem(ProductTypeDto type) {
        TreeItem<Object> item = new TreeItem<>(type);
        if (type.getSpecies() != null) {
            type.getSpecies().forEach(species -> {
                TreeItem<Object> speciesItem = new TreeItem<>(species);
                if (species.getSeries() != null) {
                    species.getSeries().forEach(series ->
                            speciesItem.getChildren().add(new TreeItem<>(series)));
                }
                item.getChildren().add(speciesItem);
            });
        }
        return item;
    }

    private String sortKey(ProductTypeDto type) {
        return type.getDisplayName() != null ? type.getDisplayName().toLowerCase() : "";
    }

    // ==========================================================
    // ВЫБОР
    // ==========================================================

    private void onTreeSelection(TreeItem<Object> item) {
        if (item == null || item.getValue() == null) {
            resetSelection();
            return;
        }

        Object value = item.getValue();

        // Системный тип: версия не нужна, форма фиксированная
        if (value instanceof ProductTypeDto type && type.getSystemCardType() != null) {
            cardType = type.getSystemCardType();
            selectedVersionId = null;
            selectedVersion = null;
            versionsList.getItems().clear();
            showDefaultForm(true);
            createButton.setDisable(false);
            statusLabel.setText("Тип «" + type.getDisplayName() + "»: форма по умолчанию");
            return;
        }

        // Динамический тип или вид — нужно спуститься до серии
        if (value instanceof ProductTypeDto || value instanceof ProductSpeciesDto) {
            resetSelection();
            statusLabel.setText("Выберите вид и серию изделия");
            return;
        }

        if (value instanceof ProductSeriesDto series) {
            showDefaultForm(false);
            loadVersions(series);
            return;
        }

        resetSelection();
    }

    private void resetSelection() {
        cardType = null;
        selectedVersionId = null;
        selectedVersion = null;
        versionsList.getItems().clear();
        showDefaultForm(false);
        createButton.setDisable(true);
    }

    private void showDefaultForm(boolean visible) {
        defaultFormLabel.setVisible(visible);
        defaultFormLabel.setManaged(visible);
        versionsList.setVisible(!visible);
        versionsList.setManaged(!visible);
    }

    private void loadVersions(ProductSeriesDto series) {
        statusLabel.setText("Загрузка шаблона для изделия " + series.getDisplayName() + "...");
        versionsList.getItems().clear();
        createButton.setDisable(true);
        cardType = "DYNAMIC";

        new Thread(() -> {
            try {
                ProductTemplateDto template = ProductTemplateClient.getTemplateBySeries(series.getId());
                if (template == null) {
                    Platform.runLater(() -> statusLabel.setText("Для изделия нет шаблона"));
                    return;
                }
                List<ProductTemplateVersionDto> versions =
                        ProductTemplateClient.getAvailableVersions(template.getId());

                Platform.runLater(() -> {
                    versionsList.getItems().setAll(versions);
                    statusLabel.setText(versions.isEmpty()
                            ? "Нет доступных версий (PUBLISHED/LEGACY)"
                            : "Выберите версию шаблона");
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
            }
        }, "CardCreationDialog-versions").start();
    }

    private void onVersionSelection(ProductTemplateVersionDto version) {
        if (version == null) {
            selectedVersionId = null;
            selectedVersion = null;
            createButton.setDisable(true);
            return;
        }
        selectedVersion = version;
        selectedVersionId = version.getId();
        createButton.setDisable(false);
    }
}

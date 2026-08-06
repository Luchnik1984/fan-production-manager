package com.fanproduction.gui.controller;

import com.fanproduction.core.dto.Displayable;
import com.fanproduction.gui.builder.ComponentTreeBuilder;
import com.fanproduction.gui.client.ComponentCategoryClient;
import com.fanproduction.gui.client.ComponentClassClient;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.dto.response.ComponentCategoryDto;
import com.fanproduction.gui.dto.response.ComponentClassDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentSelectorController {

    @FXML
    private TreeView<Object> categoryTreeView;

    @FXML
    private Label selectedComponentLabel;

    @FXML
    private Button selectButton;

    @FXML
    private Button cancelButton;

    @Setter
    private Stage dialogStage;
    @Getter
    private ComponentDto selectedComponent;

    private List<ComponentCategoryDto> allCategories;
    private List<ComponentClassDto> allClasses;
    private List<ComponentDto> allComponents;
    private Map<Long, List<ComponentDto>> componentsByClassId;

    @FXML
    private void initialize() {
        selectButton.setDisable(true);
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                allCategories = ComponentCategoryClient.getAllCategories();
                allClasses = ComponentClassClient.getAllClasses();
                allComponents = ComponentClient.getAllComponents();

                componentsByClassId = allComponents.stream()
                        .collect(Collectors.groupingBy(ComponentDto::getClassId));

                Platform.runLater(this::buildTree);
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка загрузки данных: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void buildTree() {
        // 1. Строим дерево через билдер
        ComponentTreeBuilder builder = new ComponentTreeBuilder(allCategories, allClasses, allComponents);
        TreeItem<Object> rootItem = builder.buildTree();

        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false);

        // 2. Настройка отображения ячеек (используем Displayable)
        categoryTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof Displayable) {
                    setText(((Displayable) item).getDisplayName());
                } else {
                    setText(item.toString());
                }
            }
        });

        // 3. Настройка обработки выбора
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() instanceof ComponentDto) {
                        selectedComponent = (ComponentDto) newVal.getValue();
                        selectedComponentLabel.setText("Выбран: " + selectedComponent.getDisplayName());
                        selectButton.setDisable(false);
                    } else {
                        selectedComponent = null;
                        selectedComponentLabel.setText("Выбран: ");
                        selectButton.setDisable(true);
                    }
                });
    }

    @FXML
    private void handleSelect() {
        if (selectedComponent != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        selectedComponent = null;
        dialogStage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.client.ComponentClassClient;
import com.fanproduction.gui.client.ComponentCategoryClient;
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
    private ComponentDto selectedComponent;  // ← храним ComponentDto, а не ComponentSelectionResult
    private List<ComponentCategoryDto> allCategories;
    private List<ComponentClassDto> allClasses;
    private List<ComponentDto> allComponents;
    private Map<Long, List<ComponentDto>> componentsByClassId = new java.util.HashMap<>();

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
        TreeItem<Object> rootItem = new TreeItem<>();
        rootItem.setValue(null);
        rootItem.setExpanded(true);

        TreeItem<Object> allItem = new TreeItem<>("Все компоненты");
        allItem.setExpanded(true);
        rootItem.getChildren().add(allItem);

        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (ComponentCategoryDto category : rootCategories) {
            TreeItem<Object> categoryItem = buildCategoryTreeItem(category);
            allItem.getChildren().add(categoryItem);
        }

        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false);

        categoryTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else if (item instanceof ComponentCategoryDto) {
                    setText(((ComponentCategoryDto) item).getName());
                } else if (item instanceof ComponentClassDto) {
                    setText(((ComponentClassDto) item).getName());
                } else if (item instanceof ComponentDto comp) {
                    String display = comp.getName();
                    if (comp.getVendorCode() != null && !comp.getVendorCode().isEmpty()) {
                        display += " (" + comp.getVendorCode() + ")";
                    }
                    if (comp.getUnitCode() != null) {
                        display += " - " + comp.getUnitCode();
                    }
                    setText(display);
                } else {
                    setText(item.toString());
                }
            }
        });

        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() instanceof ComponentDto) {
                        selectedComponent = (ComponentDto) newVal.getValue();
                        String display = selectedComponent.getName();
                        if (selectedComponent.getUnitCode() != null) {
                            display += " (" + selectedComponent.getUnitCode() + ")";
                        }
                        selectedComponentLabel.setText("Выбран: " + display);
                        selectButton.setDisable(false);
                    } else {
                        selectedComponent = null;
                        selectedComponentLabel.setText("Выбран: ");
                        selectButton.setDisable(true);
                    }
                });
    }

    private TreeItem<Object> buildCategoryTreeItem(ComponentCategoryDto category) {
        TreeItem<Object> categoryItem = new TreeItem<>(category);
        categoryItem.setExpanded(true);

        List<ComponentClassDto> classesInCategory = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(category.getId()))
                .toList();

        for (ComponentClassDto cls : classesInCategory) {
            TreeItem<Object> classItem = new TreeItem<>(cls);
            classItem.setExpanded(true);

            List<ComponentDto> components = componentsByClassId.getOrDefault(cls.getId(), List.of());
            for (ComponentDto comp : components) {
                classItem.getChildren().add(new TreeItem<>(comp));
            }

            categoryItem.getChildren().add(classItem);
        }

        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            categoryItem.getChildren().add(buildCategoryTreeItem(child));
        }

        return categoryItem;
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
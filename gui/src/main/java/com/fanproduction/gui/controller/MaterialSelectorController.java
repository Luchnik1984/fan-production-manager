package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.MaterialCategoryClient;
import com.fanproduction.gui.client.MaterialClassClient;
import com.fanproduction.gui.client.MaterialClient;
import com.fanproduction.gui.dto.response.MaterialCategoryDto;
import com.fanproduction.gui.dto.response.MaterialClassDto;
import com.fanproduction.gui.dto.response.MaterialDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MaterialSelectorController {

    @FXML
    private TreeView<Object> categoryTreeView;

    @FXML
    private Label selectedMaterialLabel;

    @FXML
    private Button selectButton;

    @FXML
    private Button cancelButton;

    @Setter
    private Stage dialogStage;
    @Getter
    private MaterialDto selectedMaterial;
    private List<MaterialCategoryDto> allCategories;
    private List<MaterialClassDto> allClasses;
    private List<MaterialDto> allMaterials;
    private Map<Long, List<MaterialDto>> materialsByClassId;

    @FXML
    private void initialize() {
        selectButton.setDisable(true);
        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                allCategories = MaterialCategoryClient.getAllCategories();
                allClasses = MaterialClassClient.getAllClasses();
                allMaterials = MaterialClient.getAllMaterials();

                materialsByClassId = allMaterials.stream()
                        .collect(Collectors.groupingBy(MaterialDto::getClassId));

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

        TreeItem<Object> allItem = new TreeItem<>("Все материалы");
        allItem.setExpanded(true);
        rootItem.getChildren().add(allItem);

        List<MaterialCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (MaterialCategoryDto category : rootCategories) {
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
                } else if (item instanceof MaterialCategoryDto) {
                    setText(((MaterialCategoryDto) item).getName());
                } else if (item instanceof MaterialClassDto) {
                    setText(((MaterialClassDto) item).getName());
                } else if (item instanceof MaterialDto m) {
                    String display = m.getName();
                    if (m.getVendorCode() != null && !m.getVendorCode().isEmpty()) {
                        display += " (" + m.getVendorCode() + ")";
                    }
                    if (m.getUnitCode() != null) {
                        display += " - " + m.getUnitCode();
                    }
                    setText(display);
                } else {
                    setText(item.toString());
                }
            }
        });

        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() instanceof MaterialDto) {
                        selectedMaterial = (MaterialDto) newVal.getValue();
                        String display = selectedMaterial.getName();
                        if (selectedMaterial.getUnitCode() != null) {
                            display += " (" + selectedMaterial.getUnitCode() + ")";
                        }
                        selectedMaterialLabel.setText("Выбран: " + display);
                        selectButton.setDisable(false);
                    } else {
                        selectedMaterial = null;
                        selectedMaterialLabel.setText("Выбран: ");
                        selectButton.setDisable(true);
                    }
                });
    }

    private TreeItem<Object> buildCategoryTreeItem(MaterialCategoryDto category) {
        TreeItem<Object> categoryItem = new TreeItem<>(category);
        categoryItem.setExpanded(true);

        List<MaterialClassDto> classesInCategory = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(category.getId()))
                .toList();

        for (MaterialClassDto cls : classesInCategory) {
            TreeItem<Object> classItem = new TreeItem<>(cls);
            classItem.setExpanded(true);

            List<MaterialDto> materials = materialsByClassId.getOrDefault(cls.getId(), List.of());
            for (MaterialDto m : materials) {
                classItem.getChildren().add(new TreeItem<>(m));
            }

            categoryItem.getChildren().add(classItem);
        }

        List<MaterialCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .toList();

        for (MaterialCategoryDto child : children) {
            categoryItem.getChildren().add(buildCategoryTreeItem(child));
        }

        return categoryItem;
    }

    @FXML
    private void handleSelect() {
        if (selectedMaterial != null) {
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        selectedMaterial = null;
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

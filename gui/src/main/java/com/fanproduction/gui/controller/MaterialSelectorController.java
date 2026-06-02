package com.fanproduction.gui.controller;

import com.fanproduction.core.dto.Displayable;
import com.fanproduction.gui.builder.MaterialTreeBuilder;
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
        // 1. Строим дерево через билдер
        MaterialTreeBuilder builder = new MaterialTreeBuilder(allCategories, allClasses, allMaterials);
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
                    if (newVal != null && newVal.getValue() instanceof MaterialDto) {
                        selectedMaterial = (MaterialDto) newVal.getValue();
                        selectedMaterialLabel.setText("Выбран: " + selectedMaterial.getDisplayName());
                        selectButton.setDisable(false);
                    } else {
                        selectedMaterial = null;
                        selectedMaterialLabel.setText("Выбран: ");
                        selectButton.setDisable(true);
                    }
                });
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

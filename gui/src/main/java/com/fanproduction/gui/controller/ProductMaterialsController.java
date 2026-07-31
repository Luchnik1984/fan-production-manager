package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductMaterialClient;
import com.fanproduction.gui.dto.ProductMaterialItemDto;
import com.fanproduction.gui.dto.response.MaterialDto;
import com.fanproduction.gui.util.TooltipUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductMaterialsController {

    @FXML
    private Button selectMaterialButton;
    @FXML
    private Label selectedMaterialLabel;
    @FXML
    private TextField quantityField;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;
    @FXML
    private TableView<ProductMaterialItemDto> materialsTable;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> nameColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> classNameColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> standardColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> specificationColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> materialTypeColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> vendorCodeColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> unitColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, Double> quantityColumn;
    @FXML
    private TableColumn<ProductMaterialItemDto, String> noteColumn;
    @FXML
    private Label statusLabel;


    private final ObservableList<ProductMaterialItemDto> materialsList = FXCollections.observableArrayList();
    @Setter
    private Long productCardId;
    private Long selectedMaterialId;
    private String selectedMaterialName;
    private String selectedMaterialUnitCode;
    @Setter
    private CardFormController parentController;

    public void refresh(Long productCardId) {
        this.productCardId = productCardId;
        loadMaterials();
        clearSelectedMaterial();
        addButton.setDisable(false);
        deleteButton.setDisable(false);
    }

    public void showNotSavedMessage() {
        Platform.runLater(() -> {
            materialsList.clear();
            statusLabel.setText("Сохраните карточку, чтобы добавить материалы");
            addButton.setDisable(true);
            deleteButton.setDisable(true);
            selectMaterialButton.setDisable(true);
        });
    }

    @FXML
    private void initialize() {
        setupTable();
        setupQuantityValidation();
    }

    private void setupTable() {
        // Наименование (с подсказкой)
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDisplayName()));
        nameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Класс (с подсказкой)
        classNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        classNameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // ГОСТ/ТУ (с подсказкой)
        standardColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStandard() != null ? cellData.getValue().getStandard() : ""));
        standardColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Тех. параметры (с подсказкой)
        specificationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getSpecification() != null ? cellData.getValue().getSpecification() : ""));
        specificationColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Тип (с подсказкой)
        materialTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getMaterialType() != null ? cellData.getValue().getMaterialType() : ""));
        materialTypeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Артикул (с подсказкой)
        vendorCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVendorCode() != null ? cellData.getValue().getVendorCode() : ""));
        vendorCodeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Количество (редактируемое)
        quantityColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantityPerUnit()).asObject());
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            ProductMaterialItemDto item = event.getRowValue();
            Double newQuantity = event.getNewValue();
            if (newQuantity != null && newQuantity > 0) {
                updateQuantity(item, newQuantity);
            } else {
                materialsTable.refresh();
                showAlert("Ошибка", "Количество должно быть больше 0", Alert.AlertType.ERROR);
            }
        });

        // Ед. изм.
        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUnitCode() != null ? cellData.getValue().getUnitCode() : ""));

        // Примечание (редактируемое, с подсказкой)
        noteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNote()));
        noteColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());
        noteColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        noteColumn.setOnEditCommit(event -> {
            ProductMaterialItemDto item = event.getRowValue();
            String newNote = event.getNewValue();
            updateNote(item, newNote);
        });

        materialsTable.setItems(materialsList);
        materialsTable.setEditable(true);
    }

    private void setupQuantityValidation() {
        quantityField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*(\\.\\d*)?")) {
                quantityField.setText(old);
            }
        });
    }

    private void clearSelectedMaterial() {
        selectedMaterialId = null;
        selectedMaterialName = null;
        selectedMaterialUnitCode = null;
        selectedMaterialLabel.setText("Не выбран");
    }

    @FXML
    private void handleSelectMaterial() {
        if (productCardId == null) {
            showAlert("Внимание", "Сначала сохраните карточку", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/MaterialSelectorView.fxml"));
            Parent root = loader.load();

            MaterialSelectorController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Выбор материала");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(selectMaterialButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root, 650, 550));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            MaterialDto selected = controller.getSelectedMaterial();
            if (selected != null) {
                selectedMaterialId = selected.getId();
                selectedMaterialName = selected.getName();
                selectedMaterialUnitCode = selected.getUnitCode();
                String display = selectedMaterialName;
                if (selectedMaterialUnitCode != null) {
                    display += " (" + selectedMaterialUnitCode + ")";
                }
                selectedMaterialLabel.setText(display);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть окно выбора материала", Alert.AlertType.ERROR);
        }
    }

    private void loadMaterials() {
        if (productCardId == null) return;

        statusLabel.setText("Загрузка...");
        materialsList.clear();

        new Thread(() -> {
            try {
                List<Map<String, Object>> items = ProductMaterialClient.getProductMaterials(productCardId);

                Platform.runLater(() -> {
                    for (Map<String, Object> item : items) {
                        ProductMaterialItemDto dto = new ProductMaterialItemDto();
                        dto.setProductMaterialId(((Number) item.get("id")).longValue());
                        dto.setMaterialId(((Number) item.get("materialId")).longValue());
                        dto.setName((String) item.get("materialName"));
                        dto.setClassName((String) item.get("materialClass"));
                        dto.setStandard((String) item.get("standard"));
                        dto.setSpecification((String) item.get("specification"));
                        dto.setMaterialType((String) item.get("materialType"));
                        dto.setVendorCode((String) item.get("vendorCode"));
                        dto.setUnitCode((String) item.get("unitCode"));
                        dto.setQuantityPerUnit(item.get("quantityPerUnit") != null ? ((Number) item.get("quantityPerUnit")).doubleValue() : 1.0);
                        dto.setNote((String) item.get("note"));
                        materialsList.add(dto);
                    }
                    statusLabel.setText("Материалов: " + materialsList.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddMaterial() {
        if (productCardId == null) {
            showAlert("Внимание", "Сначала сохраните карточку", Alert.AlertType.WARNING);
            return;
        }

        if (selectedMaterialId == null) {
            showAlert("Внимание", "Выберите материал", Alert.AlertType.WARNING);
            return;
        }

        if (parentController == null || parentController.getMaterialManager() == null) {
            showAlert("Ошибка", "Менеджер материалов не инициализирован", Alert.AlertType.ERROR);
            return;
        }

        Long materialId = selectedMaterialId;
        String quantityText = quantityField.getText().trim();
        Double quantityPerUnit;
        try {
            quantityPerUnit = Double.parseDouble(quantityText);
            if (quantityPerUnit <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество (больше 0)", Alert.AlertType.ERROR);
            return;
        }

        boolean alreadyExists = materialsList.stream()
                .anyMatch(m -> m.getMaterialId().equals(materialId));
        if (alreadyExists) {
            showAlert("Внимание", "Этот материал уже добавлен. Измените количество в таблице.",
                    Alert.AlertType.WARNING);
            return;
        }

        addButton.setDisable(true);
        addButton.setText("Сохранение...");

        new Thread(() -> {
            try {
                parentController.getMaterialManager().addLocal(materialId, quantityPerUnit, null);
                Platform.runLater(() -> {
                    addButton.setDisable(false);
                    addButton.setText("➕ Добавить");
                    quantityField.setText("1.0");
                    clearSelectedMaterial();
                    statusLabel.setText("Материал добавлен (будет сохранён при сохранении карточки)");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addButton.setDisable(false);
                    addButton.setText("➕ Добавить");
                    showAlert("Ошибка", "Не удалось добавить материал: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleDeleteSelected() {
        ProductMaterialItemDto selected = materialsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите материал для удаления", Alert.AlertType.WARNING);
            return;
        }

        if (parentController == null || parentController.getMaterialManager() == null) {
            showAlert("Ошибка", "Менеджер материалов не инициализирован", Alert.AlertType.ERROR);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление материала");
        confirm.setContentText("Удалить материал \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        parentController.getMaterialManager().removeLocal(selected.getMaterialId());
                        Platform.runLater(() -> statusLabel.setText("Материал удалён (изменение будет сохранено при сохранении карточки)"));
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить материал: " + e.getMessage(), Alert.AlertType.ERROR));
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    /**
     * Обновляет количество материала (локально, без отправки на сервер).
     * Изменение будет сохранено при сохранении карточки.
     */
    private void updateQuantity(ProductMaterialItemDto item, Double newQuantity) {
        if (parentController == null || parentController.getMaterialManager() == null) {
            showAlert("Ошибка", "Менеджер материалов не инициализирован", Alert.AlertType.ERROR);
            return;
        }

        try {
            parentController.getMaterialManager().updateQuantityLocal(item.getMaterialId(), newQuantity);
            statusLabel.setText("Количество обновлено (будет сохранено при сохранении карточки)");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось обновить количество: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    /**
     * Обновляет примечание материала (локально, без отправки на сервер).
     * Изменение будет сохранено при сохранении карточки.
     */
    private void updateNote(ProductMaterialItemDto item, String newNote) {
        if (parentController == null || parentController.getMaterialManager() == null) {
            showAlert("Ошибка", "Менеджер материалов не инициализирован", Alert.AlertType.ERROR);
            return;
        }

        try {
            parentController.getMaterialManager().updateNoteLocal(item.getMaterialId(), newNote);
            statusLabel.setText("Примечание обновлено (будет сохранено при сохранении карточки)");
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось обновить примечание: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void showLoadingMessage() {
        Platform.runLater(() -> {
            materialsList.clear();
            statusLabel.setText("Загрузка...");
            addButton.setDisable(true);
            selectMaterialButton.setDisable(true);
        });
    }

    public void enableControls() {
        Platform.runLater(() -> {
            addButton.setDisable(false);
            selectMaterialButton.setDisable(false);
        });
    }

    /**
     * Возвращает список материалов для внешнего использования
     */
    public ObservableList<ProductMaterialItemDto> getItems() {
        return materialsList;
    }

    /**
     * Удаляет материал из списка
     */
    public void removeItem(ProductMaterialItemDto item) {
        if (item != null) {
            materialsList.remove(item);
        }
    }

    public void addItem(ProductMaterialItemDto item) {
        if (item != null) {
            materialsList.add(item);
        }
    }

    public void refreshItem(ProductMaterialItemDto item) {
        int index = materialsList.indexOf(item);
        if (index >= 0) {
            materialsList.set(index, item);
        } else {
            materialsList.add(item);
        }
        materialsTable.refresh();
    }
}

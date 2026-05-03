package com.fanproduction.gui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.ProductComponentItemDto;
import com.fanproduction.gui.dto.SelectableItem;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ComponentDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductComponentsController {

    @FXML
    private ComboBox<SelectableItem> componentComboBox;

    @FXML
    private TextField quantityField;

    @FXML
    private TextField positionField;

    @FXML
    private Button addButton;

    @FXML
    private TableView<ProductComponentItemDto> componentsTable;

    @FXML
    private TableColumn<ProductComponentItemDto, String> nameColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, String> classColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, Double> quantityColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, String> positionColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, String> noteColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, Void> actionsColumn;

    @FXML
    private Label statusLabel;

    private final ObservableList<ProductComponentItemDto> componentsList = FXCollections.observableArrayList();
    @Setter
    private Long productCardId;

    public void refresh(Long productCardId) {
        this.productCardId = productCardId;
        loadComponents();
        loadAvailableComponents();
        addButton.setDisable(false);
    }

    public void showNotSavedMessage() {
        Platform.runLater(() -> {
            componentsList.clear();
            statusLabel.setText("Сохраните карточку, чтобы добавить комплектующие");
            addButton.setDisable(true);
        });
    }

    @FXML
    private void initialize() {
        setupTable();
        setupQuantityValidation();
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        classColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        quantityColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantity()).asObject());
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            ProductComponentItemDto item = event.getRowValue();
            Double newQuantity = event.getNewValue();
            if (newQuantity != null && newQuantity > 0) {
                updateQuantity(item, newQuantity);
            }
        });
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        noteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNote()));

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑️");
            {
                deleteButton.setOnAction(e -> {
                    ProductComponentItemDto item = getTableView().getItems().get(getIndex());
                    deleteComponent(item);
                });
                deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        componentsTable.setItems(componentsList);
        componentsTable.setEditable(true);
    }

    private void setupQuantityValidation() {
        quantityField.textProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*(\\.\\d*)?")) {
                quantityField.setText(old);
            }
        });
    }

    private void loadAvailableComponents() {
        if (productCardId == null) return;

        componentComboBox.getItems().clear();
        componentComboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                TypeReference<ApiResponse<List<ComponentDto>>> typeRef = new TypeReference<>() {};
                ApiResponse<List<ComponentDto>> response = ApiClient.get("/components", typeRef);

                Platform.runLater(() -> {
                    componentComboBox.getItems().clear();
                    if (response.isSuccess() && response.getData() != null) {
                        List<ComponentDto> components = response.getData();
                        if (components.isEmpty()) {
                            componentComboBox.getItems().add(new SelectableItem(null, "Нет компонентов"));
                        } else {
                            for (ComponentDto dto : components) {
                                String displayName = dto.getName();
                                if (dto.getVendorCode() != null && !dto.getVendorCode().isEmpty()) {
                                    displayName += " (" + dto.getVendorCode() + ")";
                                }
                                if (dto.getUnitCode() != null) {
                                    displayName += " - " + dto.getUnitCode();
                                }
                                componentComboBox.getItems().add(new SelectableItem(dto.getId(), displayName));
                            }
                        }
                    } else {
                        componentComboBox.getItems().add(new SelectableItem(null, "Ошибка: " + response.getMessage()));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    componentComboBox.getItems().clear();
                    componentComboBox.getItems().add(new SelectableItem(null, "Ошибка: " + e.getMessage()));
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadComponents() {
        if (productCardId == null) return;

        statusLabel.setText("Загрузка...");
        componentsList.clear();

        new Thread(() -> {
            try {
                TypeReference<ApiResponse<List<Map<String, Object>>>> typeRef = new TypeReference<>() {};
                ApiResponse<List<Map<String, Object>>> response = ApiClient.get(
                        "/components/product/" + productCardId, typeRef);

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        for (Map<String, Object> item : response.getData()) {
                            ProductComponentItemDto dto = new ProductComponentItemDto();
                            dto.setProductComponentId(((Number) item.get("id")).longValue());
                            dto.setComponentId(((Number) item.get("componentId")).longValue());
                            dto.setName((String) item.get("componentName"));
                            dto.setClassName((String) item.get("componentClass"));
                            dto.setUnitCode((String) item.get("unitCode"));
                            dto.setQuantity(item.get("quantity") != null ? ((Number) item.get("quantity")).doubleValue() : 1.0);
                            dto.setPosition((String) item.get("position"));
                            dto.setNote((String) item.get("note"));
                            componentsList.add(dto);
                        }
                        statusLabel.setText("Компонентов: " + componentsList.size());
                    } else {
                        statusLabel.setText("Ошибка загрузки: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Ошибка: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleAddComponent() {
        if (productCardId == null) {
            showAlert("Внимание", "Сначала сохраните карточку", Alert.AlertType.WARNING);
            return;
        }

        SelectableItem selected = componentComboBox.getValue();
        if (selected == null || selected.getId() == null) {
            showAlert("Внимание", "Выберите компонент", Alert.AlertType.WARNING);
            return;
        }

        Long componentId = selected.getId();
        String quantityText = quantityField.getText().trim();
        Double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество (больше 0)", Alert.AlertType.ERROR);
            return;
        }
        String position = positionField.getText().trim();
        String note = "";

        boolean alreadyExists = componentsList.stream()
                .anyMatch(c -> c.getComponentId().equals(componentId));
        if (alreadyExists) {
            showAlert("Внимание", "Этот компонент уже добавлен. Измените количество в таблице.",
                    Alert.AlertType.WARNING);
            return;
        }

        addButton.setDisable(true);
        addButton.setText("Сохранение...");

        new Thread(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("componentId", componentId);
                request.put("quantity", quantity);
                if (position != null && !position.isEmpty()) {
                    request.put("position", position);
                }
                if (note != null && !note.isEmpty()) {
                    request.put("note", note);
                }

                TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
                ApiResponse<Map<String, Object>> response = ApiClient.post(
                        "/components/product/" + productCardId, request, typeRef);

                Platform.runLater(() -> {
                    addButton.setDisable(false);
                    addButton.setText("➕ Добавить");
                    if (response.isSuccess()) {
                        quantityField.setText("1.0");
                        positionField.clear();
                        loadComponents();
                        loadAvailableComponents();
                    } else {
                        showAlert("Ошибка", "Не удалось добавить компонент: " + response.getMessage(),
                                Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    addButton.setDisable(false);
                    addButton.setText("➕ Добавить");
                    showAlert("Ошибка", "Ошибка: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void updateQuantity(ProductComponentItemDto item, Double newQuantity) {
        new Thread(() -> {
            try {
                Map<String, Double> request = new HashMap<>();
                request.put("quantity", newQuantity);

                ApiClient.put("/components/product/" + productCardId + "/" + item.getComponentId() + "/quantity",
                        request, new TypeReference<ApiResponse<Void>>() {});

                Platform.runLater(() -> {
                    item.setQuantity(newQuantity);
                    componentsTable.refresh();
                    statusLabel.setText("Количество обновлено");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Ошибка", "Не удалось обновить количество: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                    loadComponents();
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void deleteComponent(ProductComponentItemDto item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setHeaderText("Удаление компонента");
        confirm.setContentText("Удалить компонент \"" + item.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiClient.delete("/components/product/" + productCardId + "/" + item.getComponentId(),
                                new TypeReference<ApiResponse<Void>>() {});

                        Platform.runLater(() -> {
                            componentsList.remove(item);
                            statusLabel.setText("Компонент удалён");
                            loadAvailableComponents();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Ошибка", "Не удалось удалить: " + e.getMessage(),
                                    Alert.AlertType.ERROR);
                        });
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

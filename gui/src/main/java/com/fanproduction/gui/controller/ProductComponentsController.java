package com.fanproduction.gui.controller;

import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.util.TooltipUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.ProductComponentItemDto;
import com.fanproduction.gui.dto.response.ApiResponse;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductComponentsController {

    @FXML
    private Button selectComponentButton;

    @FXML
    private Label selectedComponentLabel;

    @FXML
    private TextField quantityField;

    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

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
    private TableColumn<ProductComponentItemDto, String> vendorCodeColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, String> unitCodeColumn;
    @FXML
    private TableColumn<ProductComponentItemDto, String> descriptionColumn;


    @FXML
    private Label statusLabel;

    private final ObservableList<ProductComponentItemDto> componentsList = FXCollections.observableArrayList();
    @Setter
    private Long productCardId;
    private Long selectedComponentId;
    private String selectedComponentName;
    private String selectedComponentUnitCode;

    public void refresh(Long productCardId) {
        this.productCardId = productCardId;
        loadComponents();
        clearSelectedComponent();
        addButton.setDisable(false);
    }

    public void showNotSavedMessage() {
        Platform.runLater(() -> {
            componentsList.clear();
            statusLabel.setText("Сохраните карточку, чтобы добавить комплектующие");
            addButton.setDisable(true);
            selectComponentButton.setDisable(true);
        });
    }

    @FXML
    private void initialize() {
        setupTable();
        setupQuantityValidation();
    }

    private void setupTable() {
        // Наименование (только чтение, с подсказкой)
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Класс (только чтение, с подсказкой)
        classColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        classColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Артикул (только чтение, с подсказкой)
        vendorCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVendorCode() != null ? cellData.getValue().getVendorCode() : ""));
        vendorCodeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Количество (редактируемое, с подсказкой)
        quantityColumn.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getQuantity()).asObject());
        quantityColumn.setCellFactory(column -> TooltipUtil.createEditableDoubleTooltipCell());
        quantityColumn.setOnEditCommit(event -> {
            ProductComponentItemDto item = event.getRowValue();
            Double newQuantity = event.getNewValue();
            if (newQuantity != null && newQuantity > 0) {
                updateQuantity(item, newQuantity);
            } else {
                componentsTable.refresh();
                showAlert("Ошибка", "Количество должно быть больше 0", Alert.AlertType.ERROR);
            }
        });

        // Ед.изм. (только чтение, без подсказки)
        unitCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUnitCode() != null ? cellData.getValue().getUnitCode() : ""));

        // Описание (только чтение, с подсказкой)
        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : ""));
        descriptionColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        // Место установки (редактируемое, с подсказкой)
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        positionColumn.setCellFactory(column -> TooltipUtil.createEditableStringTooltipCell());
        positionColumn.setOnEditCommit(event -> {
            ProductComponentItemDto item = event.getRowValue();
            String newPosition = event.getNewValue();
            updatePosition(item, newPosition);
        });

        // Примечание (редактируемое, с подсказкой)
        noteColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNote()));
        noteColumn.setCellFactory(column -> TooltipUtil.createEditableStringTooltipCell());
        noteColumn.setOnEditCommit(event -> {
            ProductComponentItemDto item = event.getRowValue();
            String newNote = event.getNewValue();
            updateNote(item, newNote);
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

    private void clearSelectedComponent() {
        selectedComponentId = null;
        selectedComponentName = null;
        selectedComponentUnitCode = null;
        selectedComponentLabel.setText("Не выбран");
    }

    @FXML
    private void handleSelectComponent() {
        if (productCardId == null) {
            showAlert("Внимание", "Сначала сохраните карточку", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ComponentSelectorView.fxml"));
            Parent root = loader.load();

            ComponentSelectorController controller = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Выбор компонента");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(selectComponentButton.getScene().getWindow());
            dialogStage.setScene(new Scene(root, 450, 550));
            controller.setDialogStage(dialogStage);

            dialogStage.showAndWait();

            ComponentDto selected = controller.getSelectedComponent();
            if (selected != null) {
                selectedComponentId = selected.getId();
                selectedComponentName = selected.getName();
                selectedComponentUnitCode = selected.getUnitCode();
                String display = selectedComponentName;
                if (selectedComponentUnitCode != null) {
                    display += " (" + selectedComponentUnitCode + ")";
                }
                selectedComponentLabel.setText(display);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Ошибка", "Не удалось открыть окно выбора компонента", Alert.AlertType.ERROR);
        }
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
                            dto.setVendorCode((String) item.get("vendorCode"));
                            dto.setUnitCode((String) item.get("unitCode"));
                            dto.setQuantity(item.get("quantity") != null ? ((Number) item.get("quantity")).doubleValue() : 1.0);
                            dto.setDescription((String) item.get("description"));
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

        if (selectedComponentId == null) {
            showAlert("Внимание", "Выберите компонент", Alert.AlertType.WARNING);
            return;
        }

        Long componentId = selectedComponentId;
        String quantityText = quantityField.getText().trim();
        Double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите корректное количество (больше 0)", Alert.AlertType.ERROR);
            return;
        }

        // Позиция и примечание — не обязательные поля
        // Они будут заполняться в таблице после добавления, поэтому не отправляем их при создании
        String position = null;
        String note = null;

        // Проверяем, не добавлен ли уже этот компонент
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
                // Добавляем position и note только если они не null и не пустые
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
                        clearSelectedComponent();
                        loadComponents();
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

    @FXML
    private void handleDeleteSelected() {
        ProductComponentItemDto selected = componentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите компонент для удаления", Alert.AlertType.WARNING);
            return;
        }
        deleteComponent(selected);
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

    private void updatePosition(ProductComponentItemDto item, String newPosition) {
        new Thread(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("position", newPosition != null ? newPosition : "");

                ApiClient.put("/components/product/" + productCardId + "/" + item.getComponentId() + "/position",
                        request, new TypeReference<ApiResponse<Void>>() {});

                Platform.runLater(() -> {
                    item.setPosition(newPosition);
                    componentsTable.refresh();
                    statusLabel.setText("Позиция обновлена");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Ошибка", "Не удалось обновить позицию: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                    loadComponents();
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void updateNote(ProductComponentItemDto item, String newNote) {
        new Thread(() -> {
            try {
                Map<String, String> request = new HashMap<>();
                request.put("note", newNote != null ? newNote : "");

                ApiClient.put("/components/product/" + productCardId + "/" + item.getComponentId() + "/note",
                        request, new TypeReference<ApiResponse<Void>>() {});

                Platform.runLater(() -> {
                    item.setNote(newNote);
                    componentsTable.refresh();
                    statusLabel.setText("Примечание обновлено");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Ошибка", "Не удалось обновить примечание: " + e.getMessage(),
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
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить: " + e.getMessage(),
                                Alert.AlertType.ERROR));
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

    /**
     * Создаёт ячейку таблицы с всплывающей подсказкой (Tooltip) без задержки.
     * @param <T> тип данных в ячейке (обычно String)
     * @return настроенная ячейка
     */
    private <T> TableCell<ProductComponentItemDto, T> createTooltipCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    String text = item.toString();
                    setText(text);
                    if (!text.isEmpty()) {
                        Tooltip tooltip = new Tooltip(text);
                        tooltip.setShowDelay(Duration.millis(100));
                        tooltip.setShowDuration(Duration.INDEFINITE);
                        setTooltip(tooltip);
                    }
                }
            }
        };
    }

    public void showLoadingMessage() {
        Platform.runLater(() -> {
            componentsList.clear();
            statusLabel.setText("Загрузка...");
            addButton.setDisable(true);
            selectComponentButton.setDisable(true);
        });
    }

    public void enableControls() {
        Platform.runLater(() -> {
            addButton.setDisable(false);
            selectComponentButton.setDisable(false);
        });
    }

    /**
     * Возвращает список компонентов для внешнего использования
     */
    public ObservableList<ProductComponentItemDto> getItems() {
        return componentsList;
    }

    /**
     * Удаляет компонент из списка
     */
    public void removeItem(ProductComponentItemDto item) {
        if (item != null) {
            componentsList.remove(item);
        }
    }


    public void addItem(ProductComponentItemDto item) {
        if (item != null) {
            componentsList.add(item);
        }
    }

    public void refreshItem(ProductComponentItemDto item) {
        int index = componentsList.indexOf(item);
        if (index >= 0) {
            componentsList.set(index, item);
        } else {
            componentsList.add(item);
        }
        componentsTable.refresh();
    }
}



package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.dto.request.CreateComponentRequest;
import com.fanproduction.gui.dto.response.ComponentClassDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для справочника компонентов.
 */
public class ComponentsCatalogController {

    @FXML
    private ComboBox<String> classFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<ComponentDto> componentsTable;

    @FXML
    private TableColumn<ComponentDto, String> nameColumn;
    @FXML
    private TableColumn<ComponentDto, String> classNameColumn;
    @FXML
    private TableColumn<ComponentDto, String> vendorCodeColumn;
    @FXML
    private TableColumn<ComponentDto, String> unitColumn;
    @FXML
    private TableColumn<ComponentDto, String> quantityPerUnitColumn;
    @FXML
    private TableColumn<ComponentDto, String> descriptionColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button createClassButton;
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    @Setter
    private Stage stage;

    private final ObservableList<ComponentDto> componentList = FXCollections.observableArrayList();
    private final Map<Long, String> classNames = new HashMap<>();
    private final Map<Long, String> unitNames = new HashMap<>();
    private List<ComponentClassDto> allClasses;
    private List<UnitOfMeasureDto> allUnits;

    @FXML
    private void initialize() {
        setupTable();
        loadData();

        // Отключаем кнопки, если ничего не выбрано
        componentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> updateButtonsState(newVal));
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));
        classNameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getClassName()));
        vendorCodeColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getVendorCode() != null ?
                        cellData.getValue().getVendorCode() : ""));
        unitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUnitCode() != null ?
                        cellData.getValue().getUnitCode() : ""));
        quantityPerUnitColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getQuantityPerUnit() != null ?
                        String.valueOf(cellData.getValue().getQuantityPerUnit()) : "1"));
        descriptionColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescription() != null ?
                        cellData.getValue().getDescription() : ""));

        // Двойной клик для редактирования
        componentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEdit();
            }
        });

        componentsTable.setItems(componentList);
    }

    private void updateButtonsState(ComponentDto selected) {
        boolean hasSelection = selected != null;
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    private void loadData() {
        statusLabel.setText("Загрузка...");

        new Thread(() -> {
            try {
                // Загружаем классы
                allClasses = ComponentClient.getAllClasses();
                // Загружаем единицы измерения
                allUnits = ComponentClient.getAllUnits();
                // Загружаем компоненты
                loadComponents();

                Platform.runLater(() -> {
                    // Заполняем фильтр классов
                    classFilterComboBox.getItems().clear();
                    classFilterComboBox.getItems().add("Все классы");
                    for (ComponentClassDto cls : allClasses) {
                        classFilterComboBox.getItems().add(cls.getName());
                        classNames.put(cls.getId(), cls.getName());
                    }
                    classFilterComboBox.setValue("Все классы");
                    classFilterComboBox.valueProperty().addListener(
                            (obs, old, newVal) -> filterComponents());

                    for (UnitOfMeasureDto unit : allUnits) {
                        unitNames.put(unit.getId(), unit.getCode());
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка загрузки: " + e.getMessage());
                    showAlert("Ошибка", "Не удалось загрузить данные: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void loadComponents() throws Exception {
        List<ComponentDto> components = ComponentClient.getAllComponents();
        Platform.runLater(() -> {
            componentList.clear();
            componentList.addAll(components);
            statusLabel.setText("Всего компонентов: " + componentList.size());
        });
    }

    private void filterComponents() {
        String selectedClass = classFilterComboBox.getValue();
        String searchText = searchField.getText().toLowerCase();

        new Thread(() -> {
            try {
                List<ComponentDto> filtered;
                if ("Все классы".equals(selectedClass)) {
                    filtered = ComponentClient.getAllComponents();
                } else {
                    // Находим ID класса по имени
                    Long classId = null;
                    for (ComponentClassDto cls : allClasses) {
                        if (cls.getName().equals(selectedClass)) {
                            classId = cls.getId();
                            break;
                        }
                    }
                    if (classId != null) {
                        filtered = ComponentClient.getComponentsByClass(classId);
                    } else {
                        filtered = ComponentClient.getAllComponents();
                    }
                }

                // Фильтр по поисковому запросу
                if (!searchText.isEmpty()) {
                    filtered = filtered.stream()
                            .filter(c -> c.getName().toLowerCase().contains(searchText) ||
                                    (c.getVendorCode() != null && c.getVendorCode().toLowerCase().contains(searchText)))
                            .collect(java.util.stream.Collectors.toList());
                }

                List<ComponentDto> finalFiltered = filtered;
                Platform.runLater(() -> {
                    componentList.clear();
                    componentList.addAll(finalFiltered);
                    statusLabel.setText("Всего компонентов: " + componentList.size());
                });

            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(),
                        Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleSearch() {
        filterComponents();
    }

    @FXML
    private void handleRefresh() {
        loadData();
    }

    @FXML
    private void handleCreateClass() {
        showCreateClassDialog();
    }

    @FXML
    private void handleCreate() {
        showComponentDialog(null);
    }

    @FXML
    private void handleEdit() {
        ComponentDto selected = componentsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showComponentDialog(selected);
        } else {
            showAlert("Внимание", "Выберите компонент для редактирования", Alert.AlertType.WARNING);
        }
    }

    @FXML
    private void handleDelete() {
        ComponentDto selected = componentsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите компонент для удаления", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление компонента");
        confirm.setContentText("Вы уверены, что хотите удалить компонент \"" + selected.getName() + "\"?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ComponentClient.deleteComponent(selected.getId());
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Компонент удалён", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось удалить: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                    }
                }).start();
            }
        });
    }

    private void showCreateClassDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание класса компонентов");
        dialog.setHeaderText("Создание нового класса компонентов");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Кабельные вводы");
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание (необязательно)");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Название класса:*"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Описание:"), 0, 1);
        grid.add(descriptionField, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Название класса не может быть пустым", Alert.AlertType.ERROR);
                    return;
                }

                new Thread(() -> {
                    try {
                        ComponentClient.createClass(name, descriptionField.getText());
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Класс создан", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать класс: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                    }
                }).start();
            }
        });
    }

    private void showComponentDialog(ComponentDto existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создание компонента" : "Редактирование компонента");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Выбор класса
        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.getItems().addAll(classNames.values());
        classCombo.setPromptText("Выберите класс");

        // Наименование
        TextField nameField = new TextField();
        nameField.setPromptText("Наименование компонента");

        // Артикул
        TextField vendorCodeField = new TextField();
        vendorCodeField.setPromptText("Артикул производителя");

        // Единица измерения
        ComboBox<String> unitCombo = new ComboBox<>();
        for (UnitOfMeasureDto unit : allUnits) {
            unitCombo.getItems().add(unit.getCode() + " - " + unit.getName());
        }
        unitCombo.setPromptText("Выберите единицу измерения");

        // Количество на единицу
        TextField quantityField = new TextField();
        quantityField.setPromptText("Количество на единицу (по умолчанию 1)");
        quantityField.setText("1");

        // Описание
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        // Заполняем существующие данные
        if (existing != null) {
            nameField.setText(existing.getName());
            if (existing.getVendorCode() != null) vendorCodeField.setText(existing.getVendorCode());
            if (existing.getQuantityPerUnit() != null) quantityField.setText(String.valueOf(existing.getQuantityPerUnit()));
            if (existing.getDescription() != null) descriptionField.setText(existing.getDescription());

            // Устанавливаем класс
            String className = existing.getClassName();
            if (className != null) {
                classCombo.setValue(className);
            }

            // Устанавливаем единицу измерения
            String unitDisplay = existing.getUnitCode() + " - " + existing.getUnitName();
            unitCombo.setValue(unitDisplay);
        }

        grid.add(new Label("Класс:*"), 0, 0);
        grid.add(classCombo, 1, 0);
        grid.add(new Label("Наименование:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Артикул:"), 0, 2);
        grid.add(vendorCodeField, 1, 2);
        grid.add(new Label("Единица измерения:*"), 0, 3);
        grid.add(unitCombo, 1, 3);
        grid.add(new Label("Кол-во на ед.:"), 0, 4);
        grid.add(quantityField, 1, 4);
        grid.add(new Label("Описание:"), 0, 5);
        grid.add(descriptionField, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedClass = classCombo.getValue();
                String name = nameField.getText().trim();
                String vendorCode = vendorCodeField.getText().trim();
                String selectedUnit = unitCombo.getValue();
                Double quantity;
                try {
                    quantity = Double.parseDouble(quantityField.getText().trim());
                } catch (NumberFormatException e) {
                    quantity = 1.0;
                }
                String description = descriptionField.getText().trim();

                if (selectedClass == null || selectedClass.isEmpty()) {
                    showAlert("Ошибка", "Выберите класс компонента", Alert.AlertType.ERROR);
                    return;
                }
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите наименование компонента", Alert.AlertType.ERROR);
                    return;
                }
                if (selectedUnit == null || selectedUnit.isEmpty()) {
                    showAlert("Ошибка", "Выберите единицу измерения", Alert.AlertType.ERROR);
                    return;
                }

                // Находим ID класса
                Long classId = null;
                for (ComponentClassDto cls : allClasses) {
                    if (cls.getName().equals(selectedClass)) {
                        classId = cls.getId();
                        break;
                    }
                }

                // Находим ID единицы измерения
                Long unitId = null;
                for (UnitOfMeasureDto unit : allUnits) {
                    String unitDisplay = unit.getCode() + " - " + unit.getName();
                    if (unitDisplay.equals(selectedUnit)) {
                        unitId = unit.getId();
                        break;
                    }
                }

                if (classId == null) {
                    showAlert("Ошибка", "Класс не найден", Alert.AlertType.ERROR);
                    return;
                }
                if (unitId == null) {
                    showAlert("Ошибка", "Единица измерения не найдена", Alert.AlertType.ERROR);
                    return;
                }

                CreateComponentRequest request = new CreateComponentRequest(
                        classId, name, vendorCode, unitId, quantity, description);

                new Thread(() -> {
                    try {
                        if (existing == null) {
                            ComponentClient.createComponent(request);
                        } else {
                            ComponentClient.updateComponent(existing.getId(), request);
                        }
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Компонент " + (existing == null ? "создан" : "обновлён"),
                                    Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось сохранить: " + e.getMessage(),
                                Alert.AlertType.ERROR));
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
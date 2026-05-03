package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ComponentCategoryClient;
import com.fanproduction.gui.client.ComponentClassClient;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.request.CreateComponentRequest;
import com.fanproduction.gui.dto.response.ComponentCategoryDto;
import com.fanproduction.gui.dto.response.ComponentClassDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private TreeView<ComponentCategoryDto> categoryTreeView;

    @FXML
    private TableColumn<ComponentDto, String> nameColumn;
    @FXML
    private TableColumn<ComponentDto, String> classNameColumn;
    @FXML
    private TableColumn<ComponentDto, String> vendorCodeColumn;
    @FXML
    private TableColumn<ComponentDto, String> unitColumn;

    @FXML
    private TableColumn<ComponentDto, String> descriptionColumn;

    @FXML
    private Label statusLabel;

    @FXML
    @SuppressWarnings("unused")
    private Button createClassButton;

    @FXML
    @SuppressWarnings("unused")
    private Button createCategoryButton;

    @FXML
    @SuppressWarnings("unused")
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
    private List<ComponentCategoryDto> allCategories = new ArrayList<>();
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
                // Загружаем единицы измерения
                allUnits = ComponentClient.getAllUnits();

                // Загружаем категории
                allCategories = ComponentCategoryClient.getAllCategories();

                // Загружаем классы
                allClasses = ComponentClient.getAllClasses();

                // Загружаем компоненты
                loadAllComponents();

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
                    buildCategoryTree();
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

    private void loadClasses() {
        new Thread(() -> {
            try {
                allClasses = ComponentClassClient.getAllClasses();
                Platform.runLater(() -> {
                    // Обновляем выпадающий список классов
                    classFilterComboBox.getItems().clear();
                    classFilterComboBox.getItems().add("Все классы");
                    for (ComponentClassDto cls : allClasses) {
                        classFilterComboBox.getItems().add(cls.getName());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить классы: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    private void loadAllComponents() {
        new Thread(() -> {
            try {
                List<ComponentDto> components = ComponentClient.getAllComponents();
                Platform.runLater(() -> {
                    componentList.clear();
                    componentList.addAll(components);
                    statusLabel.setText("Всего компонентов: " + componentList.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка загрузки: " + e.getMessage());
                    showAlert("Ошибка", "Не удалось загрузить компоненты: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
            }
        }).start();
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
    private void handleCreateCategory() {
        showCreateCategoryDialog();
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
        grid.setPadding(new Insets(20));

        // Выбор категории
        ComboBox<String> categoryCombo = new ComboBox<>();
        for (ComponentCategoryDto cat : allCategories) {
            if (cat.getParentId() == null) {
                categoryCombo.getItems().add(cat.getName());
            }
        }
        categoryCombo.setPromptText("Выберите категорию");

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Ступицы с цилиндрической посадкой");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание (необязательно)");
        descriptionField.setPrefRowCount(3);

        // Группированный список единиц измерения
        GroupedComboBox<UnitOfMeasureDto> unitCombo = new GroupedComboBox<>();
        Map<String, List<UnitOfMeasureDto>> groupedUnits = allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
        unitCombo.setGroupedItems(groupedUnits);
        unitCombo.setPromptText("Единица измерения по умолчанию (необязательно)");

        grid.add(new Label("Категория:*"), 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(new Label("Название класса:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Описание:"), 0, 2);
        grid.add(descriptionField, 1, 2);
        grid.add(new Label("Ед. изм. по умолчанию:"), 0, 3);
        grid.add(unitCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedCategory = categoryCombo.getValue();
                String name = nameField.getText().trim();
                String description = descriptionField.getText().trim();
                UnitOfMeasureDto selectedUnit = unitCombo.getValue();

                if (selectedCategory == null || selectedCategory.isEmpty()) {
                    showAlert("Ошибка", "Выберите категорию", Alert.AlertType.ERROR);
                    return;
                }
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите название класса", Alert.AlertType.ERROR);
                    return;
                }

                // Находим ID категории
                Long categoryId = null;
                for (ComponentCategoryDto cat : allCategories) {
                    if (cat.getName().equals(selectedCategory)) {
                        categoryId = cat.getId();
                        break;
                    }
                }

                Long unitId = selectedUnit != null ? selectedUnit.getId() : null;

                final Long finalCategoryId = categoryId;
                final Long finalUnitId = unitId;

                new Thread(() -> {
                    try {
                        ComponentClassClient.createClass(finalCategoryId, name, description, finalUnitId);
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

    private void showCreateCategoryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание категории компонентов");
        dialog.setHeaderText("Создание новой категории");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20));

        // Выбор родительской категории (опционально)
        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("— Корневая категория —");
        // Загружаем существующие категории
        for (ComponentCategoryDto cat : allCategories) {
            parentCombo.getItems().add(cat.getName());
        }
        parentCombo.setValue("— Корневая категория —");

        // Название категории
        TextField nameField = new TextField();
        nameField.setPromptText("Например: Кронштейны");

        // Описание (необязательно)
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание (необязательно)");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Родительская категория:"), 0, 0);
        grid.add(parentCombo, 1, 0);
        grid.add(new Label("Название категории:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Описание:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите название категории", Alert.AlertType.ERROR);
                    return;
                }

                String parentName = parentCombo.getValue();
                Long parentId = null;
                if (!"— Корневая категория —".equals(parentName) && parentName != null) {
                    // Находим ID родительской категории
                    for (ComponentCategoryDto cat : allCategories) {
                        if (cat.getName().equals(parentName)) {
                            parentId = cat.getId();
                            break;
                        }
                    }
                }

                final Long finalParentId = parentId;
                new Thread(() -> {
                    try {
                        ComponentCategoryClient.createCategory(name, finalParentId, descriptionField.getText());
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Категория создана", Alert.AlertType.INFORMATION);
                            loadData(); // перезагружаем категории
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать категорию: " + e.getMessage(),
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

        // ==========================================
        // ГРУППИРОВАННЫЙ СПИСОК ЕДИНИЦ ИЗМЕРЕНИЯ
        // ==========================================
        GroupedComboBox<UnitOfMeasureDto> unitCombo = new GroupedComboBox<>();
        Map<String, List<UnitOfMeasureDto>> groupedUnits = allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
        unitCombo.setGroupedItems(groupedUnits);
        unitCombo.setPromptText("Выберите единицу измерения");

        // Описание
        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        // Заполняем существующие данные
        if (existing != null) {
            nameField.setText(existing.getName());
            if (existing.getVendorCode() != null) vendorCodeField.setText(existing.getVendorCode());
            if (existing.getDescription() != null) descriptionField.setText(existing.getDescription());

            // Устанавливаем класс
            String className = existing.getClassName();
            if (className != null) {
                classCombo.setValue(className);
            }

            // Устанавливаем единицу измерения (ищем объект по ID)
            if (existing.getUnitId() != null) {
                for (UnitOfMeasureDto unit : allUnits) {
                    if (unit.getId().equals(existing.getUnitId())) {
                        unitCombo.setValue(unit);
                        break;
                    }
                }
            }
        }

        grid.add(new Label("Класс:*"), 0, 0);
        grid.add(classCombo, 1, 0);
        grid.add(new Label("Наименование:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Артикул:"), 0, 2);
        grid.add(vendorCodeField, 1, 2);
        grid.add(new Label("Единица измерения:*"), 0, 3);
        grid.add(unitCombo, 1, 3);
        grid.add(new Label("Описание:"), 0, 4);
        grid.add(descriptionField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedClass = classCombo.getValue();
                String name = nameField.getText().trim();
                String vendorCode = vendorCodeField.getText().trim();
                UnitOfMeasureDto selectedUnit = unitCombo.getValue();
                String description = descriptionField.getText().trim();

                if (selectedClass == null || selectedClass.isEmpty()) {
                    showAlert("Ошибка", "Выберите класс компонента", Alert.AlertType.ERROR);
                    return;
                }
                if (name.isEmpty()) {
                    showAlert("Ошибка", "Введите наименование компонента", Alert.AlertType.ERROR);
                    return;
                }
                if (selectedUnit == null) {
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

                if (classId == null) {
                    showAlert("Ошибка", "Класс не найден", Alert.AlertType.ERROR);
                    return;
                }

                Long unitId = selectedUnit.getId();

                CreateComponentRequest request = new CreateComponentRequest(
                        classId, name, vendorCode, unitId, description);

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

    private void buildCategoryTree() {
        // Находим корневые категории (parentId == null)
        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        // Создаём корневой элемент
        TreeItem<ComponentCategoryDto> rootItem = new TreeItem<>();
        rootItem.setValue(null);
        rootItem.setExpanded(true);

        // Добавляем элемент "Все категории"
        TreeItem<ComponentCategoryDto> allItem = new TreeItem<>();
        ComponentCategoryDto allCategory = new ComponentCategoryDto();
        allCategory.setId(null);
        allCategory.setName("Все категории");
        allItem.setValue(allCategory);
        allItem.setExpanded(true);
        rootItem.getChildren().add(allItem);

        // Рекурсивно добавляем категории
        for (ComponentCategoryDto category : rootCategories) {
            TreeItem<ComponentCategoryDto> categoryItem = createCategoryTreeItem(category);
            allItem.getChildren().add(categoryItem);
        }

        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false);

        // Обработчик выбора категории
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        Long selectedCategoryId = newVal.getValue().getId();
                        if (selectedCategoryId == null) {
                            // Выбрано "Все категории"
                            loadAllComponents();
                        } else {
                            filterByCategory(selectedCategoryId);
                        }
                    }
                });

        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false);

        // ==========================================
        // НАСТРОЙКА ОТОБРАЖЕНИЯ ТОЛЬКО НАЗВАНИЯ КАТЕГОРИИ
        // ==========================================
        categoryTreeView.setCellFactory(tv -> new TreeCell<ComponentCategoryDto>() {
            @Override
            protected void updateItem(ComponentCategoryDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());  // ← показываем только название
                }
            }
        });

        // Обработчик выбора категории
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        Long selectedCategoryId = newVal.getValue().getId();
                        if (selectedCategoryId == null) {
                            loadAllComponents();
                        } else {
                            filterByCategory(selectedCategoryId);
                        }
                    }
                });

    }

    private TreeItem<ComponentCategoryDto> createCategoryTreeItem(ComponentCategoryDto category) {
        TreeItem<ComponentCategoryDto> item = new TreeItem<>(category);
        item.setExpanded(true);

        // Находим дочерние категории
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            item.getChildren().add(createCategoryTreeItem(child));
        }

        return item;
    }

    private void filterByCategory(Long categoryId) {
        if (categoryId == null) {
            loadAllComponents();
            return;
        }

        // Находим все ID категорий (включая подкатегории)
        List<Long> categoryIds = getAllCategoryIds(categoryId);
        System.out.println("Selected category ID: " + categoryId);
        System.out.println("All category IDs: " + categoryIds);

        // Находим классы, принадлежащие этим категориям
        List<Long> classIds = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && categoryIds.contains(cls.getCategoryId()))
                .map(ComponentClassDto::getId)
                .collect(Collectors.toList());
        System.out.println("Found class IDs: " + classIds);

        if (classIds.isEmpty()) {
            // Нет классов в этой категории — показываем пустой список
            Platform.runLater(() -> {
                componentList.clear();
                statusLabel.setText("Нет компонентов в выбранной категории");
            });
            return;
        }

        // Фильтруем компоненты по классам
        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> classIds.contains(c.getClassId()))
                        .collect(Collectors.toList());
                System.out.println("Filtered components count: " + filtered.size());
                Platform.runLater(() -> {
                    componentList.clear();
                    componentList.addAll(filtered);
                    statusLabel.setText("Всего компонентов: " + componentList.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка фильтрации: " + e.getMessage());
                    showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);

        // Находим все подкатегории
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(categoryId))
                .collect(Collectors.toList());

        for (ComponentCategoryDto child : children) {
            ids.addAll(getAllCategoryIds(child.getId()));
        }

        return ids;
    }

    private Map<String, List<UnitOfMeasureDto>> getGroupedUnits() {
        return allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
    }
}
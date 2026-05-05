package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ComponentCategoryClient;
import com.fanproduction.gui.client.ComponentClassClient;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.request.CreateComponentRequest;
import com.fanproduction.gui.dto.response.*;
import com.fanproduction.gui.util.TooltipUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public class ComponentsCatalogController {

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
    private TableColumn<ComponentDto, String> descriptionColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Button createCategoryButton;
    @FXML
    private Button createClassButton;
    @FXML
    private Button createButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    @FXML
    private TreeView<CategoryTreeItem> categoryTreeView;

    @Setter
    private Stage stage;
    private final ObservableList<ComponentDto> componentList = FXCollections.observableArrayList();
    private final Map<Long, String> classNames = new HashMap<>();
    private final Map<Long, String> unitNames = new HashMap<>();
    private List<ComponentCategoryDto> allCategories = new ArrayList<>();
    private List<ComponentClassDto> allClasses = new ArrayList<>();
    private List<UnitOfMeasureDto> allUnits = new ArrayList<>();

    // Внутренний класс для хранения элементов дерева
    @Getter
    public static class CategoryTreeItem {
        private final Long id;
        private final String displayName;
        private final String type; // "root", "category", "class"
        private final Long classId;

        public CategoryTreeItem(Long id, String displayName, String type) {
            this(id, displayName, type, null);
        }

        public CategoryTreeItem(Long id, String displayName, String type, Long classId) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
            this.classId = classId;
        }

        public Long getCategoryId() { return id; }
    }

    @FXML
    private void initialize() {
        setupTable();
        loadData();
        componentsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> updateButtonsState(newVal));
    }

    private void setupTable() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        nameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        classNameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClassName()));
        classNameColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        vendorCodeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getVendorCode() != null ? cellData.getValue().getVendorCode() : ""));
        vendorCodeColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        unitColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getUnitCode() != null ? cellData.getValue().getUnitCode() : ""));
        // unitColumn не требует подсказки (короткая)

        descriptionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDescription() != null ? cellData.getValue().getDescription() : ""));
        descriptionColumn.setCellFactory(column -> TooltipUtil.createTooltipCell());

        componentsTable.setItems(componentList);

        // Двойной клик для редактирования
        componentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                ComponentDto selected = componentsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showComponentDialog(selected);
                }
            }
        });
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
                allUnits = ComponentClient.getAllUnits();
                allCategories = ComponentCategoryClient.getAllCategories();
                allClasses = ComponentClassClient.getAllClasses();
                loadAllComponents();

                Platform.runLater(() -> {
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

    private void buildCategoryTree() {
        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        TreeItem<CategoryTreeItem> rootItem = new TreeItem<>();
        rootItem.setValue(null);
        rootItem.setExpanded(true);

        // Добавляем "Все компоненты"
        TreeItem<CategoryTreeItem> allItem = new TreeItem<>(new CategoryTreeItem(null, "Все компоненты", "root"));
        allItem.setExpanded(true);
        rootItem.getChildren().add(allItem);

        for (ComponentCategoryDto category : rootCategories) {
            addCategoryToTree(allItem, category);
        }

        categoryTreeView.setRoot(rootItem);
        categoryTreeView.setShowRoot(false);

        // Настройка отображения
        categoryTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(CategoryTreeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());
                    if ("class".equals(item.getType())) {
                        setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
                    } else if ("category".equals(item.getType())) {
                        setStyle("-fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-font-weight: normal;");
                    }
                }
            }
        });

        // Обработчик выбора
        categoryTreeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        CategoryTreeItem selected = newVal.getValue();
                        if ("class".equals(selected.getType())) {
                            filterByClassId(selected.getClassId());
                        } else if ("category".equals(selected.getType())) {
                            filterByCategory(selected.getCategoryId());
                        } else {
                            loadAllComponents();
                        }
                    }
                });
    }

    private void addCategoryToTree(TreeItem<CategoryTreeItem> parent, ComponentCategoryDto category) {
        TreeItem<CategoryTreeItem> categoryItem = new TreeItem<>(
                new CategoryTreeItem(category.getId(), category.getName(), "category"));
        categoryItem.setExpanded(true);
        parent.getChildren().add(categoryItem);

        // Добавляем классы этой категории
        List<ComponentClassDto> classesInCategory = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(category.getId()))
                .toList();

        for (ComponentClassDto cls : classesInCategory) {
            TreeItem<CategoryTreeItem> classItem = new TreeItem<>(
                    new CategoryTreeItem(cls.getId(), cls.getName(), "class", cls.getId()));
            categoryItem.getChildren().add(classItem);
        }

        // Добавляем дочерние категории
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            addCategoryToTree(categoryItem, child);
        }
    }

    private void filterByClassId(Long classId) {
        if (classId == null) {
            loadAllComponents();
            return;
        }

        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> c.getClassId().equals(classId))
                        .toList();
                Platform.runLater(() -> {
                    componentList.clear();
                    componentList.addAll(filtered);
                    statusLabel.setText("Всего компонентов: " + componentList.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка фильтрации: " + e.getMessage());
                    showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void filterByCategory(Long categoryId) {
        if (categoryId == null) {
            loadAllComponents();
            return;
        }

        List<Long> categoryIds = getAllCategoryIds(categoryId);
        List<Long> classIds = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && categoryIds.contains(cls.getCategoryId()))
                .map(ComponentClassDto::getId)
                .toList();

        if (classIds.isEmpty()) {
            Platform.runLater(() -> {
                componentList.clear();
                statusLabel.setText("Нет компонентов в выбранной категории");
            });
            return;
        }

        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> classIds.contains(c.getClassId()))
                        .toList();
                Platform.runLater(() -> {
                    componentList.clear();
                    componentList.addAll(filtered);
                    statusLabel.setText("Всего компонентов: " + componentList.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка фильтрации: " + e.getMessage());
                    showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(categoryId))
                .toList();
        for (ComponentCategoryDto child : children) {
            ids.addAll(getAllCategoryIds(child.getId()));
        }
        return ids;
    }

    private void performSearch(String searchText) {
        new Thread(() -> {
            try {
                List<ComponentDto> allComponents = ComponentClient.getAllComponents();
                List<ComponentDto> filtered = allComponents.stream()
                        .filter(c -> c.getName().toLowerCase().contains(searchText) ||
                                (c.getVendorCode() != null && c.getVendorCode().toLowerCase().contains(searchText)))
                        .toList();
                Platform.runLater(() -> {
                    componentList.clear();
                    componentList.addAll(filtered);
                    statusLabel.setText("Найдено: " + componentList.size());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка поиска: " + e.getMessage());
                    showAlert("Ошибка", "Ошибка поиска: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }



    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        if (searchText.isEmpty()) {
            loadAllComponents();
        } else {
            performSearch(searchText);
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    @FXML
    private void handleCreateCategory() {
        showCreateCategoryDialog();
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

    private void showCreateCategoryDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание категории компонентов");
        dialog.setHeaderText("Создание новой категории");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("— Корневая категория —");

        // Добавляем категории с отступами
        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (ComponentCategoryDto rootCat : rootCategories) {
            parentCombo.getItems().add(rootCat.getName());
            addChildCategoriesToParentCombo(parentCombo, rootCat, 1);
        }

        parentCombo.setValue("— Корневая категория —");

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Кронштейны");

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
                    String cleanName = parentName.replaceAll("^\\s+", "");
                    for (ComponentCategoryDto cat : allCategories) {
                        if (cat.getName().equals(cleanName)) {
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
                            loadData();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать категорию: " + e.getMessage(),
                                Alert.AlertType.ERROR));
                    }
                }).start();
            }
        });
    }

    private void addChildCategoriesToParentCombo(ComboBox<String> combo, ComponentCategoryDto parent, int depth) {
        String indent = "  " + "  ".repeat(depth);
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parent.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            String display = indent + child.getName();
            combo.getItems().add(display);
            addChildCategoriesToParentCombo(combo, child, depth + 1);
        }
    }

    private void showCreateClassDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание класса компонентов");
        dialog.setHeaderText("Создание нового класса");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Выберите категорию");

        // Добавляем категории с отступами
        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (ComponentCategoryDto rootCat : rootCategories) {
            categoryCombo.getItems().add(rootCat.getName());
            addChildCategoriesToParentCombo(categoryCombo, rootCat, 1);
        }

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Ступицы с цилиндрической посадкой");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание (необязательно)");
        descriptionField.setPrefRowCount(3);

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

                String cleanCategory = selectedCategory.replaceAll("^\\s+", "");
                Long categoryId = null;
                for (ComponentCategoryDto cat : allCategories) {
                    if (cat.getName().equals(cleanCategory)) {
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

    private void showComponentDialog(ComponentDto existing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Создание компонента" : "Редактирование компонента");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Категории
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Выберите категорию");
        categoryCombo.getItems().add("— Все категории —");
        Map<String, Long> categoryIdMap = new HashMap<>();
        buildCategoryCombo(categoryCombo, categoryIdMap);
        categoryCombo.setValue("— Все категории —");

        // Классы
        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.setPromptText("Выберите класс");
        classCombo.setDisable(true);
        Map<String, ComponentClassDto> classMap = new HashMap<>();

        // Предупреждение
        Label warningLabel = new Label();
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");
        warningLabel.setVisible(false);

        // Поля ввода
        TextField nameField = new TextField();
        nameField.setPromptText("Наименование компонента");

        TextField vendorCodeField = new TextField();
        vendorCodeField.setPromptText("Артикул производителя");

        GroupedComboBox<UnitOfMeasureDto> unitCombo = new GroupedComboBox<>();
        Map<String, List<UnitOfMeasureDto>> groupedUnits = allUnits.stream()
                .collect(Collectors.groupingBy(UnitOfMeasureDto::getCategory));
        unitCombo.setGroupedItems(groupedUnits);
        unitCombo.setPromptText("Выберите единицу измерения");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        // Зависимость категория → классы
        categoryCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || "— Все категории —".equals(newVal)) {
                classCombo.setDisable(true);
                classCombo.getItems().clear();
                classCombo.setPromptText("Выберите класс");
                warningLabel.setVisible(false);
            } else {
                Long selectedCategoryId = categoryIdMap.get(newVal);
                if (selectedCategoryId != null) {
                    classMap.clear();
                    List<ComponentClassDto> filteredClasses = allClasses.stream()
                            .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(selectedCategoryId))
                            .toList();

                    classCombo.getItems().clear();
                    for (ComponentClassDto cls : filteredClasses) {
                        classCombo.getItems().add(cls.getName());
                        classMap.put(cls.getName(), cls);
                    }

                    if (filteredClasses.isEmpty()) {
                        classCombo.setDisable(true);
                        classCombo.setPromptText("Нет классов");
                        warningLabel.setText("⚠️ Сначала создайте классы в этой категории");
                        warningLabel.setVisible(true);
                    } else {
                        classCombo.setDisable(false);
                        classCombo.setPromptText("Выберите класс");
                        warningLabel.setVisible(false);
                    }
                }
            }
        });

        // Заполнение при редактировании
        if (existing != null) {
            nameField.setText(existing.getName());
            if (existing.getVendorCode() != null) vendorCodeField.setText(existing.getVendorCode());
            if (existing.getDescription() != null) descriptionField.setText(existing.getDescription());

            if (existing.getClassId() != null) {
                for (ComponentClassDto cls : allClasses) {
                    if (cls.getId().equals(existing.getClassId())) {
                        classCombo.setValue(cls.getName());
                        for (ComponentCategoryDto cat : allCategories) {
                            if (cat.getId().equals(cls.getCategoryId())) {
                                // Находим полное отображаемое имя категории с отступами
                                String fullCategoryName = findCategoryDisplayName(cat);
                                if (fullCategoryName != null) {
                                    categoryCombo.setValue(fullCategoryName);
                                } else {
                                    categoryCombo.setValue(cat.getName());
                                }
                                break;
                            }
                        }
                        break;
                    }
                }
            }

            if (existing.getUnitId() != null) {
                for (UnitOfMeasureDto unit : allUnits) {
                    if (unit.getId().equals(existing.getUnitId())) {
                        unitCombo.setValue(unit);
                        break;
                    }
                }
            }
        }

        // Сборка сетки
        grid.add(new Label("Категория:*"), 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(new Label("Класс:*"), 0, 1);
        grid.add(classCombo, 1, 1);
        grid.add(warningLabel, 1, 2);
        grid.add(new Label("Наименование:*"), 0, 3);
        grid.add(nameField, 1, 3);
        grid.add(new Label("Артикул:"), 0, 4);
        grid.add(vendorCodeField, 1, 4);
        grid.add(new Label("Единица измерения:*"), 0, 5);
        grid.add(unitCombo, 1, 5);
        grid.add(new Label("Описание:"), 0, 6);
        grid.add(descriptionField, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                saveComponent(existing, categoryCombo, classCombo, nameField, vendorCodeField,
                        unitCombo, descriptionField, classMap);
            }
        });
    }

    private String findCategoryDisplayName(ComponentCategoryDto target) {
        // Ищем корневую категорию и строим путь
        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (ComponentCategoryDto root : rootCategories) {
            String result = findInSubtree(root, target);
            if (result != null) {
                return result;
            }
        }
        return target.getName();
    }

    private String findInSubtree(ComponentCategoryDto current, ComponentCategoryDto target) {
        if (current.getId().equals(target.getId())) {
            return current.getName();
        }

        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(current.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            String result = findInSubtree(child, target);
            if (result != null) {
                return "  " + result;
            }
        }
        return null;
    }

    private void buildCategoryCombo(ComboBox<String> combo, Map<String, Long> idMap) {
        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (ComponentCategoryDto rootCat : rootCategories) {
            combo.getItems().add(rootCat.getName());
            idMap.put(rootCat.getName(), rootCat.getId());
            addChildCategoriesToCombo(combo, idMap, rootCat, 1);
        }
    }

    private void addChildCategoriesToCombo(ComboBox<String> combo, Map<String, Long> idMap,
                                           ComponentCategoryDto parent, int depth) {
        String indent = "  " + "  ".repeat(depth);
        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parent.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            String display = indent + child.getName();
            combo.getItems().add(display);
            idMap.put(display, child.getId());
            addChildCategoriesToCombo(combo, idMap, child, depth + 1);
        }
    }

    private void saveComponent(ComponentDto existing, ComboBox<String> categoryCombo,
                               ComboBox<String> classCombo, TextField nameField,
                               TextField vendorCodeField, GroupedComboBox<UnitOfMeasureDto> unitCombo,
                               TextArea descriptionField, Map<String, ComponentClassDto> classMap) {
        String selectedCategory = categoryCombo.getValue();
        String selectedClass = classCombo.getValue();
        String name = nameField.getText().trim();
        String vendorCode = vendorCodeField.getText().trim();
        UnitOfMeasureDto selectedUnit = unitCombo.getValue();
        String description = descriptionField.getText().trim();

        if (selectedCategory == null || "— Все категории —".equals(selectedCategory)) {
            showAlert("Ошибка", "Выберите категорию", Alert.AlertType.ERROR);
            return;
        }
        if (selectedClass == null || selectedClass.isEmpty()) {
            showAlert("Ошибка", "Выберите класс", Alert.AlertType.ERROR);
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

        ComponentClassDto selectedClassDto = classMap.get(selectedClass);
        if (selectedClassDto == null) {
            showAlert("Ошибка", "Класс не найден", Alert.AlertType.ERROR);
            return;
        }

        Long classId = selectedClassDto.getId();
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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
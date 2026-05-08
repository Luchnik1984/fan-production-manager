package com.fanproduction.gui.base;

import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

public abstract class BaseCatalogController<T, C, CL> {

    @Setter
    protected Stage stage;
    protected final ObservableList<T> itemList = FXCollections.observableArrayList();
    protected final Map<Long, String> classNames = new HashMap<>();
    protected final Map<Long, String> unitNames = new HashMap<>();
    protected List<C> allCategories = new ArrayList<>();
    protected List<CL> allClasses = new ArrayList<>();
    protected List<UnitOfMeasureDto> allUnits = new ArrayList<>();

    // Абстрактные методы для загрузки данных
    protected abstract void loadAllItems();
    protected abstract void filterByClassId(Long classId);
    protected abstract void filterByCategoryId(Long categoryId);
    protected abstract void showCreateCategoryDialog();
    protected abstract void showCreateClassDialog();
    protected abstract void showItemDialog(T existing);
    protected abstract void setupTable();
    protected abstract String getItemTypeName();

    // Абстрактные методы для получения полей категорий
    protected abstract Long getCategoryId(C category);
    protected abstract String getCategoryName(C category);
    protected abstract Long getCategoryParentId(C category);

    // Абстрактные методы для получения полей классов
    protected abstract Long getClassId(CL cls);
    protected abstract String getClassName(CL cls);
    protected abstract Long getClassCategoryId(CL cls);

    // Абстрактные методы для получения данных из API
    protected abstract List<C> fetchCategories() throws Exception;
    protected abstract List<CL> fetchClasses() throws Exception;

    // ==========================================
    // ОБЩИЕ МЕТОДЫ ДЛЯ ФИЛЬТРАЦИИ
    // ==========================================

    protected void filterByCategoryIdCommon(Long categoryId) {
        if (categoryId == null) {
            loadAllItems();
            return;
        }
        List<Long> categoryIds = getAllCategoryIds(categoryId);
        List<Long> classIds = allClasses.stream()
                .filter(cls -> getClassCategoryId(cls) != null && categoryIds.contains(getClassCategoryId(cls)))
                .map(this::getClassId)
                .toList();

        if (classIds.isEmpty()) {
            updateItemList(new ArrayList<>());
            return;
        }

        new Thread(() -> {
            try {
                List<T> allItems = fetchAllItems();
                List<T> filtered = allItems.stream()
                        .filter(item -> classIds.contains(getItemClassId(item)))
                        .collect(Collectors.toList());
                updateItemList(filtered);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    protected abstract List<T> fetchAllItems() throws Exception;
    protected abstract Long getItemClassId(T item);

    // ==========================================
    // ОБЩИЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ КЛАССА
    // ==========================================

    protected void showCreateClassDialogCommon() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Создание класса " + getItemTypeName());
        dialog.setHeaderText("Создание нового класса");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.setPromptText("Выберите категорию");
        for (C rootCat : allCategories.stream().filter(c -> getCategoryParentId(c) == null).toList()) {
            categoryCombo.getItems().add(getCategoryName(rootCat));
            addChildCategoriesToParentComboSimple(categoryCombo, rootCat, 1);
        }

        TextField nameField = new TextField();
        nameField.setPromptText("Например: " + getExampleClassName());

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
                for (C cat : allCategories) {
                    if (getCategoryName(cat).equals(cleanCategory)) {
                        categoryId = getCategoryId(cat);
                        break;
                    }
                }
                Long unitId = selectedUnit != null ? selectedUnit.getId() : null;
                final Long finalCategoryId = categoryId;
                final Long finalUnitId = unitId;

                new Thread(() -> {
                    try {
                        createClass(finalCategoryId, name, description, finalUnitId);
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

    // ==========================================
    // ОБЩИЕ МЕТОДЫ ДЛЯ ПОСТРОЕНИЯ КОМБО
    // ==========================================

    protected void addChildCategoriesToParentComboSimple(ComboBox<String> combo, C parent, int depth) {
        String indent = "  " + "  ".repeat(depth);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(getCategoryId(parent)))
                .toList();
        for (C child : children) {
            String display = indent + getCategoryName(child);
            combo.getItems().add(display);
            addChildCategoriesToParentComboSimple(combo, child, depth + 1);
        }
    }

    protected void addChildCategoriesToParentComboWithMap(ComboBox<String> combo, C parent, int depth, Map<String, Long> idMap) {
        String indent = "  " + "  ".repeat(depth);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(getCategoryId(parent)))
                .toList();
        for (C child : children) {
            String display = indent + getCategoryName(child);
            combo.getItems().add(display);
            idMap.put(display, getCategoryId(child));
            addChildCategoriesToParentComboWithMap(combo, child, depth + 1, idMap);
        }
    }

    // ==========================================
    // ОБЩИЕ МЕТОДЫ ДЛЯ ПОСТРОЕНИЯ ДЕРЕВА
    // ==========================================

    protected void buildCategoryTree() {
        TreeView<CategoryTreeItem> treeView = getTreeView();
        List<C> rootCategories = allCategories.stream()
                .filter(c -> getCategoryParentId(c) == null)
                .toList();

        TreeItem<CategoryTreeItem> rootItem = new TreeItem<>();
        rootItem.setValue(null);
        rootItem.setExpanded(true);

        TreeItem<CategoryTreeItem> allItem = new TreeItem<>(
                new CategoryTreeItem(null, "Все " + getItemTypeName(), "root"));
        allItem.setExpanded(true);
        rootItem.getChildren().add(allItem);

        for (C category : rootCategories) {
            addCategoryToTree(allItem, category);
        }

        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);
        setupTreeCellFactory(treeView);

        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        CategoryTreeItem selected = newVal.getValue();
                        if ("class".equals(selected.getType())) {
                            filterByClassId(selected.getClassId());
                        } else if ("category".equals(selected.getType())) {
                            filterByCategoryId(selected.getCategoryId());
                        } else {
                            loadAllItems();
                        }
                    }
                });
    }

    protected void addCategoryToTree(TreeItem<CategoryTreeItem> parent, C category) {
        TreeItem<CategoryTreeItem> categoryItem = new TreeItem<>(
                new CategoryTreeItem(getCategoryId(category), getCategoryName(category), "category"));
        categoryItem.setExpanded(true);
        parent.getChildren().add(categoryItem);

        // Добавляем классы этой категории
        List<CL> classesInCategory = allClasses.stream()
                .filter(cls -> getClassCategoryId(cls) != null && getClassCategoryId(cls).equals(getCategoryId(category)))
                .toList();

        for (CL cls : classesInCategory) {
            TreeItem<CategoryTreeItem> classItem = new TreeItem<>(
                    new CategoryTreeItem(getClassId(cls), getClassName(cls), "class", getClassId(cls)));
            categoryItem.getChildren().add(classItem);
        }

        // Добавляем дочерние категории
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(getCategoryId(category)))
                .toList();

        for (C child : children) {
            addCategoryToTree(categoryItem, child);
        }
    }

    protected void setupTreeCellFactory(TreeView<CategoryTreeItem> treeView) {
        treeView.setCellFactory(tv -> new TreeCell<>() {
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
    }

    // ==========================================
    // ОБЩИЕ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    protected void addChildCategoriesToParentComboForItem(ComboBox<String> combo, C parent, int depth, Map<String, Long> idMap) {
        String indent = "  " + "  ".repeat(depth);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(getCategoryId(parent)))
                .toList();
        for (C child : children) {
            String display = indent + getCategoryName(child);
            combo.getItems().add(display);
            idMap.put(display, getCategoryId(child));
            addChildCategoriesToParentComboForItem(combo, child, depth + 1, idMap);
        }
    }

    protected List<Long> getAllCategoryIds(Long categoryId) {
        List<Long> ids = new ArrayList<>();
        ids.add(categoryId);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(categoryId))
                .toList();
        for (C child : children) {
            ids.addAll(getAllCategoryIds(getCategoryId(child)));
        }
        return ids;
    }

    protected void updateItemList(List<T> items) {
        Platform.runLater(() -> {
            itemList.clear();
            itemList.addAll(items);
            getTableView().setItems(itemList);
            updateStatus("Всего: " + itemList.size());
        });
    }

    protected void updateStatus(String text) {
        Platform.runLater(() -> getStatusLabel().setText(text));
    }

    protected void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void loadData() {
        new Thread(() -> {
            try {
                allUnits = com.fanproduction.gui.client.ComponentClient.getAllUnits();
                allCategories = fetchCategories();
                allClasses = fetchClasses();
                loadAllItems();

                Platform.runLater(() -> {
                    for (UnitOfMeasureDto unit : allUnits) {
                        unitNames.put(unit.getId(), unit.getCode());
                    }
                    buildCategoryTree();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить данные: " + e.getMessage(),
                        Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        }).start();
    }

    // Абстрактные методы для доступа к UI элементам
    protected abstract TreeView<CategoryTreeItem> getTreeView();
    protected abstract Label getStatusLabel();
    protected abstract TableView<T> getTableView();
    protected abstract String getExampleClassName();
    protected abstract void createClass(Long categoryId, String name, String description, Long unitId) throws Exception;

}
package com.fanproduction.gui.base;

import com.fanproduction.gui.component.GroupedComboBox;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Setter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.kordamp.ikonli.fontawesome.FontAwesome;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
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
    protected final Map<String, Long> categoryDisplayToIdMap = new HashMap<>();


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
    protected abstract String getCategoryUpdatePath();
    protected abstract String getCategoryDescription(C category);


    // Абстрактные методы для получения полей классов
    protected abstract Long getClassId(CL cls);
    protected abstract String getClassName(CL cls);
    protected abstract Long getClassCategoryId(CL cls);
    protected abstract String getClassUpdatePath();

    // Абстрактные методы для получения полей элементов
    protected abstract Long getItemId(T item);
    protected abstract String getItemName(T item);
    protected abstract Long getItemClassId(T item);
    protected abstract String getClassDescription(CL cls);

    // Абстрактные методы для получения данных из API
    protected abstract List<C> fetchCategories() throws Exception;
    protected abstract List<CL> fetchClasses() throws Exception;
    protected abstract List<T> fetchAllItems() throws Exception;

    // Абстрактные методы для удаления
    protected abstract void deleteCategoryById(Long id) throws Exception;
    protected abstract void deleteClassById(Long id) throws Exception;
    protected abstract void deleteItemById(Long id) throws Exception;

    // Абстрактные методы для обновления
    protected abstract void updateCategory(Long id, String newName, Long parentId, String description);
    protected abstract void updateClass(Long id, String newName, String description, Long categoryId);

    // Абстрактные методы для экспорта
    protected abstract List<ExportRowDto> getExportData();
    protected abstract String[] getExportHeaders();

    // ==========================================
    // ОБЩИЕ МЕТОДЫ
    // ==========================================

    protected void loadData() {
        new Thread(() -> {
            try {
                allUnits = com.fanproduction.gui.client.ComponentClient.getAllUnits();
                allCategories = fetchCategories();
                allClasses = fetchClasses();

                // загружаем элементы
                List<T> items = fetchAllItems();

                Platform.runLater(() -> {
                    // Обновляем itemList
                    itemList.clear();
                    itemList.addAll(items);
                    getTableView().setItems(itemList);

                    // Обновляем статус
                    for (UnitOfMeasureDto unit : allUnits) {
                        unitNames.put(unit.getId(), unit.getCode());
                    }
                    getStatusLabel().setText("Всего: " + itemList.size());

                    // Перестраиваем дерево
                    refreshTree();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить данные: " + e.getMessage(),
                        Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        }).start();
    }

    protected void refreshTree() {
        categoryDisplayToIdMap.clear();
        buildCategoryTree();
    }

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
        setupContextMenu(treeView);

        treeView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, newVal) -> {
                    if (newVal != null && newVal.getValue() != null) {
                        CategoryTreeItem selected = newVal.getValue();
                        if ("class".equals(selected.getType())) {
                            filterByClassId(selected.getClassId());
                        } else if ("category".equals(selected.getType())) {
                            filterByCategoryId(selected.getCategoryId());
                        } else if ("item".equals(selected.getType())) {
                            filterByItemId(selected.getId());
                        } else {
                            loadAllItems();
                        }
                    }
                });
    }

    protected void filterByItemId(Long id) {
        if (id == null) {
            loadAllItems();
            return;
        }
        new Thread(() -> {
            try {
                List<T> allItems = fetchAllItems();
                List<T> filtered = allItems.stream()
                        .filter(item -> getItemId(item).equals(id))
                        .collect(Collectors.toList());
                updateItemList(filtered);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка фильтрации: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    protected void addCategoryToTree(TreeItem<CategoryTreeItem> parent, C category) {
        TreeItem<CategoryTreeItem> categoryItem = new TreeItem<>(
                new CategoryTreeItem(getCategoryId(category), getCategoryName(category), "category"));
        categoryItem.setExpanded(true);
        categoryItem.setGraphic(createFolderIcon());
        parent.getChildren().add(categoryItem);

        // Добавляем классы этой категории
        List<CL> classesInCategory = allClasses.stream()
                .filter(cls -> getClassCategoryId(cls) != null && getClassCategoryId(cls).equals(getCategoryId(category)))
                .toList();

        for (CL cls : classesInCategory) {
            TreeItem<CategoryTreeItem> classItem = new TreeItem<>(
                    new CategoryTreeItem(getClassId(cls), getClassName(cls), "class", getClassId(cls)));
            classItem.setGraphic(createClassIcon());

            // Добавляем элементы этого класса
            List<T> itemsInClass = itemList.stream()
                    .filter(item -> getItemClassId(item).equals(getClassId(cls)))
                    .toList();

            for (T item : itemsInClass) {
                TreeItem<CategoryTreeItem> itemNode = new TreeItem<>(
                        new CategoryTreeItem(getItemId(item), getItemName(item), "item", getItemClassId(item)));
                itemNode.setGraphic(createFileIcon());
                classItem.getChildren().add(itemNode);
            }

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

                    // Устанавливаем иконку в зависимости от типа
                    if ("category".equals(item.getType())) {
                        setGraphic(createFolderIcon());
                    } else if ("class".equals(item.getType())) {
                        setGraphic(createClassIcon());
                    } else if ("item".equals(item.getType())) {
                        setGraphic(createFileIcon());
                    } else {
                        setGraphic(null);
                    }

                    // Стилизация
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

    protected void setupContextMenu(TreeView<CategoryTreeItem> treeView) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("✏️ Редактировать");
        MenuItem deleteItem = new MenuItem("🗑️ Удалить");

        editItem.setOnAction(e -> {
            CategoryTreeItem selected = treeView.getSelectionModel().getSelectedItem().getValue();
            if (selected != null) {
                if ("category".equals(selected.getType())) {
                    editCategoryById(selected.getCategoryId());
                } else if ("class".equals(selected.getType())) {
                    editClassById(selected.getClassId());
                } else if ("item".equals(selected.getType())) {
                    editItemById(selected.getId());
                }
            }
        });

        deleteItem.setOnAction(e -> {
            CategoryTreeItem selected = treeView.getSelectionModel().getSelectedItem().getValue();
            if (selected != null) {
                if ("category".equals(selected.getType())) {
                    deleteCategory(selected.getCategoryId(), selected.getDisplayName());
                } else if ("class".equals(selected.getType())) {
                    deleteClass(selected.getClassId(), selected.getDisplayName());
                } else if ("item".equals(selected.getType())) {
                    deleteItem(selected.getId(), selected.getDisplayName());
                }
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem);
        treeView.setContextMenu(contextMenu);
    }

    protected void editCategoryById(Long categoryId) {
        for (C cat : allCategories) {
            if (getCategoryId(cat).equals(categoryId)) {
                showEditCategoryDialog(cat);
                break;
            }
        }
    }

    protected void editClassById(Long classId) {
        for (CL cls : allClasses) {
            if (getClassId(cls).equals(classId)) {
                showEditClassDialog(cls);
                break;
            }
        }
    }

    protected void editItemById(Long id) {
        for (T item : itemList) {
            if (getItemId(item).equals(id)) {
                showItemDialog(item);
                break;
            }
        }
    }

    protected void showEditCategoryDialog(C category) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование категории");
        dialog.setHeaderText("Редактирование категории \"" + getCategoryName(category) + "\"");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Выбор родительской категории
        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("— Корневая категория —");
        for (C cat : allCategories) {
            if (!getCategoryId(cat).equals(getCategoryId(category))) {
                String display = getCategoryName(cat);
                parentCombo.getItems().add(display);
            }
        }
        // Устанавливаем текущего родителя
        Long currentParentId = getCategoryParentId(category);
        if (currentParentId == null) {
            parentCombo.setValue("— Корневая категория —");
        } else {
            for (C cat : allCategories) {
                if (getCategoryId(cat).equals(currentParentId)) {
                    parentCombo.setValue(getCategoryName(cat));
                    break;
                }
            }
        }

        // Название категории
        TextField nameField = new TextField(getCategoryName(category));
        nameField.setPromptText("Название категории");

        // Описание
        TextArea descriptionField = new TextArea();
        descriptionField.setText(getCategoryDescription(category) != null ? getCategoryDescription(category) : "");
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        int row = 0;
        grid.add(new Label("Родительская категория:"), 0, row);
        grid.add(parentCombo, 1, row++);
        grid.add(new Label("Название:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String newName = nameField.getText().trim();
                if (newName.isEmpty()) {
                    showAlert("Ошибка", "Название не может быть пустым", Alert.AlertType.ERROR);
                    return;
                }
                String parentName = parentCombo.getValue();
                Long parentId = null;
                if (!"— Корневая категория —".equals(parentName) && parentName != null) {
                    for (C cat : allCategories) {
                        if (getCategoryName(cat).equals(parentName)) {
                            parentId = getCategoryId(cat);
                            break;
                        }
                    }
                }
                updateCategory(getCategoryId(category), newName, parentId, descriptionField.getText());
            }
        });
    }

    protected void showEditClassDialog(CL cls) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Редактирование класса");
        dialog.setHeaderText("Редактирование класса \"" + getClassName(cls) + "\"");
        dialog.initOwner(stage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Выбор категории
        ComboBox<String> categoryCombo = new ComboBox<>();
        for (C cat : allCategories) {
            categoryCombo.getItems().add(getCategoryName(cat));
        }
        // Устанавливаем текущую категорию
        Long currentCategoryId = getClassCategoryId(cls);
        for (C cat : allCategories) {
            if (getCategoryId(cat).equals(currentCategoryId)) {
                categoryCombo.setValue(getCategoryName(cat));
                break;
            }
        }

        // Название класса
        TextField nameField = new TextField(getClassName(cls));
        nameField.setPromptText("Название класса");

        // Описание
        TextArea descriptionField = new TextArea();
        descriptionField.setText(getClassDescription(cls) != null ? getClassDescription(cls) : "");
        descriptionField.setPromptText("Описание");
        descriptionField.setPrefRowCount(3);

        grid.add(new Label("Категория:*"), 0, 0);
        grid.add(categoryCombo, 1, 0);
        grid.add(new Label("Название:*"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Описание:"), 0, 2);
        grid.add(descriptionField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedCategory = categoryCombo.getValue();
                String newName = nameField.getText().trim();
                String newDescription = descriptionField.getText().trim();

                if (selectedCategory == null || selectedCategory.isEmpty()) {
                    showAlert("Ошибка", "Выберите категорию", Alert.AlertType.ERROR);
                    return;
                }
                if (newName.isEmpty()) {
                    showAlert("Ошибка", "Название не может быть пустым", Alert.AlertType.ERROR);
                    return;
                }

                Long categoryId = null;
                for (C cat : allCategories) {
                    if (getCategoryName(cat).equals(selectedCategory)) {
                        categoryId = getCategoryId(cat);
                        break;
                    }
                }
                updateClass(getClassId(cls), newName, newDescription, categoryId);
            }
        });
    }

    protected void deleteCategory(Long id, String name) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление категории \"" + name + "\"");
        confirm.setContentText("Вы уверены?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        deleteCategoryById(id);
                        Platform.runLater(this::loadData);
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        Platform.runLater(() -> {
                            if (msg.contains("дочерние категории")) {
                                showAlert("Ошибка", "Сначала удалите все подкатегории", Alert.AlertType.WARNING);
                            } else if (msg.contains("классы")) {
                                showAlert("Ошибка", "Сначала удалите все классы в категории", Alert.AlertType.WARNING);
                            } else {
                                showAlert("Ошибка", msg, Alert.AlertType.ERROR);
                            }
                        });
                    }
                }).start();
            }
        });
    }


    protected void deleteClass(Long id, String name) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление класса \"" + name + "\"");
        confirm.setContentText("Вы уверены?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        deleteClassById(id);
                        Platform.runLater(this::loadData);
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        Platform.runLater(() -> {
                            if (msg.contains("компоненты")) {
                                showAlert("Ошибка", "Сначала удалите все компоненты в классе", Alert.AlertType.WARNING);
                            } else {
                                showAlert("Ошибка", msg, Alert.AlertType.ERROR);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    protected void deleteItem(Long id, String name) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение удаления");
        confirm.setHeaderText("Удаление \"" + name + "\"");
        confirm.setContentText("Вы уверены, что хотите удалить этот элемент?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        deleteItemById(id);
                        Platform.runLater(() -> {
                            loadData();
                            showAlert("Успешно", "Элемент удалён", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        String errorMessage = e.getMessage();
                        Platform.runLater(() -> showAlert("Невозможно удалить", errorMessage, Alert.AlertType.WARNING));
                    }
                }).start();
            }
        });
    }


    protected void handleExportToExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить список " + getItemTypeName());
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel файлы", "*.xlsx"));
        fileChooser.setInitialFileName(getItemTypeName() + "_" + LocalDate.now() + ".xlsx");

        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            exportToExcel(file);
        }
    }

    protected void exportToExcel(File file) {
        new Thread(() -> {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet(getItemTypeName());

                // Заголовки
                String[] headers = getExportHeaders();
                Row headerRow = sheet.createRow(0);
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerStyle.setFont(headerFont);

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Данные
                List<ExportRowDto> data = getExportData();
                int rowNum = 1;
                for (ExportRowDto rowData : data) {
                    Row row = sheet.createRow(rowNum++);
                    List<String> values = rowData.getValues();
                    for (int i = 0; i < values.size() && i < headers.length; i++) {
                        row.createCell(i).setCellValue(values.get(i));
                    }
                }

                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                try (FileOutputStream fos = new FileOutputStream(file)) {
                    workbook.write(fos);
                }

                Platform.runLater(() -> showAlert("Успешно", "Экспорт завершён", Alert.AlertType.INFORMATION));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка экспорта: " + e.getMessage(), Alert.AlertType.ERROR));
            }
        }).start();
    }

    protected void addChildCategoriesToParentComboSimple(ComboBox<String> combo, C parent, int depth) {
        String indent = "    " + "    ".repeat(depth);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(getCategoryId(parent)))
                .toList();
        for (C child : children) {
            String display = indent + getCategoryName(child);
            combo.getItems().add(display);
            categoryDisplayToIdMap.put(display, getCategoryId(child));
            addChildCategoriesToParentComboSimple(combo, child, depth + 1);
        }
    }

    protected void addChildCategoriesToParentComboWithMap(ComboBox<String> combo, C parent, int depth, Map<String, Long> idMap) {
        String indent = "    " + "    ".repeat(depth);
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

    protected void updateItemList(List<T> items) {
        Platform.runLater(() -> {
            itemList.clear();
            itemList.addAll(items);
            getTableView().setItems(itemList);
            getStatusLabel().setText("Всего: " + itemList.size());
        });
    }

    protected void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected Node createFolderIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FOLDER);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#e6b422"));
        return icon;
    }

    protected Node createClassIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FOLDER_OPEN);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#e6b422"));
        return icon;
    }

    protected Node createFileIcon() {
        FontIcon icon = new FontIcon(FontAwesome.FILE);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#555555"));
        return icon;
    }

    protected void showCreateClassDialogCommon() {
        categoryDisplayToIdMap.clear();

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
            String display = getCategoryName(rootCat);
            categoryCombo.getItems().add(display);
            categoryDisplayToIdMap.put(display, getCategoryId(rootCat));
            addChildCategoriesToParentComboSimple(categoryCombo, rootCat, 1);
        }

        TextField nameField = new TextField();
        nameField.setPromptText("Например: Болты с шестигранной головкой");

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

                Long categoryId = categoryDisplayToIdMap.get(selectedCategory);
                if (categoryId == null) {
                    showAlert("Ошибка", "Категория не найдена", Alert.AlertType.ERROR);
                    return;
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

    protected abstract void createClass(Long categoryId, String name, String description, Long unitId) throws Exception;

    // Абстрактные методы для доступа к UI элементам
    protected abstract TreeView<CategoryTreeItem> getTreeView();
    protected abstract Label getStatusLabel();
    protected abstract TableView<T> getTableView();
}
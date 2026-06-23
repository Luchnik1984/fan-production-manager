package com.fanproduction.gui.base;

import com.fanproduction.core.dto.TechnicalSpec;
import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.component.TechnicalSpecsEditor;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import com.fanproduction.gui.util.ExcelExportUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import lombok.Setter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Абстрактный контроллер для каталогов (компоненты/материалы).
 * Содержит общую логику, но делегирует специфическую работу хелперам.
 *
 * @param <T> тип элемента (компонент или материал)
 * @param <C> тип категории
 * @param <CL> тип класса
 */
public abstract class BaseCatalogController<T, C, CL> {

    @Setter
    protected Stage stage;

    protected final ObservableList<T> itemList = FXCollections.observableArrayList();
    protected final Map<Long, String> unitNames = new HashMap<>();
    protected final Map<String, Long> categoryIdMap = new HashMap<>();
    protected final Map<String, CL> classMap = new HashMap<>();

    protected List<C> allCategories = new ArrayList<>();
    protected List<CL> allClasses = new ArrayList<>();
    protected List<UnitOfMeasureDto> allUnits = new ArrayList<>();

    // Хелперы
    protected CatalogTreeBuilder<C, CL, T> treeBuilder;
    protected CatalogExportHelper exportHelper;
    protected CatalogFilterHelper<C, T> filterHelper;
    protected CatalogDialogHelper<C, CL> dialogHelper;

    // Абстрактные методы для получения данных
    protected abstract List<C> fetchCategories() throws Exception;
    protected abstract List<CL> fetchClasses() throws Exception;
    protected abstract List<T> fetchAllItems() throws Exception;

    // Абстрактные методы для CRUD операций

    protected abstract void deleteCategoryById(Long id) throws Exception;
    protected abstract void deleteClassById(Long id) throws Exception;
    protected abstract void deleteItemById(Long id) throws Exception;

    protected abstract void updateCategory(Long id, String newName, Long parentId, String description);
    protected abstract void updateClass(Long id, String newName, String description, Long categoryId);

    // Абстрактные методы для экспорта
    protected abstract List<ExportRowDto> getExportData();
    protected abstract String[] getExportHeaders();
    protected abstract String getItemTypeName();

    // Абстрактные методы для получения полей (для хелперов)
    protected abstract Long getCategoryId(C category);
    protected abstract String getCategoryName(C category);
    protected abstract Long getCategoryParentId(C category);
    protected abstract String getCategoryDescription(C category);

    protected abstract Long getClassId(CL cls);
    protected abstract String getClassName(CL cls);
    protected abstract Long getClassCategoryId(CL cls);
    protected abstract String getClassDescription(CL cls);

    protected abstract Long getItemId(T item);
    protected abstract String getItemName(T item);
    protected abstract Long getItemClassId(T item);

    // Абстрактные методы для UI доступа
    protected abstract TreeView<CategoryTreeItem> getTreeView();
    protected abstract Label getStatusLabel();
    protected abstract TableView<T> getTableView();

    // Абстрактные методы для создания новых элементов
    protected abstract void setupTable();
    protected abstract void showCreateItemDialog();
    protected abstract void showEditItemDialog(T item);
    protected abstract void createCategory(String name, Long parentId, String description) throws Exception;
    protected abstract void createClass(Long categoryId, String name, String description, Long unitId) throws Exception;

    // ========== ОСНОВНЫЕ МЕТОДЫ ==========

    protected void loadData() {
        new Thread(() -> {
            try {
                allUnits = fetchUnits();
                allCategories = fetchCategories();
                allClasses = fetchClasses();
                List<T> items = fetchAllItems();

                initHelpers();

                Platform.runLater(() -> {
                    itemList.clear();
                    itemList.addAll(items);
                    getTableView().setItems(itemList);

                    for (UnitOfMeasureDto unit : allUnits) {
                        unitNames.put(unit.getId(), unit.getCode());
                    }

                    getStatusLabel().setText("Всего: " + itemList.size());
                    refreshTree();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить данные: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    protected void initHelpers() {
        exportHelper = new CatalogExportHelper(stage);
        filterHelper = new CatalogFilterHelper<>(allCategories, this::getCategoryId, this::getCategoryParentId, this::getItemClassId);
        dialogHelper = new CatalogDialogHelper<>(stage, allCategories, allClasses, allUnits,
                this::getCategoryId, this::getCategoryName, this::getCategoryParentId, this::getCategoryDescription,
                this::getClassId, this::getClassName, this::getClassCategoryId, this::getClassDescription);
    }

    protected void refreshTree() {
        treeBuilder = new CatalogTreeBuilder<>(allCategories, allClasses, itemList,
                this::getCategoryId, this::getCategoryParentId, this::getCategoryName,
                this::getClassId, this::getClassCategoryId, this::getClassName,
                this::getItemId, this::getItemClassId, this::getItemName);

        TreeView<CategoryTreeItem> newTree = treeBuilder.buildTree();

        TreeView<CategoryTreeItem> oldTree = getTreeView();
        if (oldTree != null) {
            TreeItem<CategoryTreeItem> newRoot = newTree.getRoot();
            if (newRoot != null) {
                oldTree.setRoot(newRoot);
                oldTree.setShowRoot(false);
            }

            CatalogTreeBuilder.setupTreeCellFactory(oldTree);
            setupContextMenu(oldTree);
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

    protected List<UnitOfMeasureDto> fetchUnits() throws Exception {
        return new ArrayList<>();
    }

    // КОНТЕКСТНОЕ МЕНЮ

    protected void setupContextMenu(TreeView<CategoryTreeItem> treeView) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Редактировать");
        editItem.setGraphic(IconFactory.createEditIcon());
        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.setGraphic(IconFactory.createDeleteIcon());

        contextMenu.getItems().addAll(editItem, deleteItem);

        // Закрываем меню при клике левой кнопкой мыши в любом месте
        treeView.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                contextMenu.hide();
            }
        });

        treeView.setOnContextMenuRequested(event -> {
            TreeItem<CategoryTreeItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.getValue() == null) {
                contextMenu.hide();
                return;
            }

            CategoryTreeItem item = selectedItem.getValue();
            String type = item.getType();

            editItem.setOnAction(e -> {
                if ("category".equals(type)) {
                    editCategoryById(item.getCategoryId());
                } else if ("class".equals(type)) {
                    editClassById(item.getClassId());
                } else if ("item".equals(type)) {
                    editItemById(item.getId());
                }
                contextMenu.hide();
            });

            deleteItem.setOnAction(e -> {
                if ("category".equals(type)) {
                    deleteCategory(item.getCategoryId(), item.getDisplayName());
                } else if ("class".equals(type)) {
                    deleteClass(item.getClassId(), item.getDisplayName());
                } else if ("item".equals(type)) {
                    deleteItem(item.getId(), item.getDisplayName());
                }
                contextMenu.hide();
            });

            contextMenu.show(treeView, event.getScreenX(), event.getScreenY());
        });
    }

    // РЕДАКТИРОВАНИЕ

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
                showEditItemDialog(item);
                break;
            }
        }
    }

    // ДИАЛОГИ РЕДАКТИРОВАНИЯ (через dialogHelper)

    protected void showEditCategoryDialog(C category) {
        dialogHelper.showEditCategoryDialog(category, result -> new Thread(() -> {
            try {
                updateCategory(getCategoryId(category), result.name(), result.parentId(), result.description());
                Platform.runLater(() -> {
                    showAlert("Успешно", "Категория обновлена", Alert.AlertType.INFORMATION);
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
            }
        }).start());
    }

    protected void showEditClassDialog(CL cls) {
        dialogHelper.showEditClassDialog(cls, result -> new Thread(() -> {
            try {
                updateClass(getClassId(cls), result.name(), result.description(), result.categoryId());
                Platform.runLater(() -> {
                    showAlert("Успешно", "Класс обновлён", Alert.AlertType.INFORMATION);
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
            }
        }).start());
    }

    // УДАЛЕНИЕ С ПРОВЕРКАМИ

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
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Категория удалена", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        Platform.runLater(() -> {
                            if (msg.contains("дочерние категории")) {
                                showAlert("Ошибка", "Сначала удалите все подкатегории", Alert.AlertType.WARNING);
                            } else if (msg.contains("классы")) {
                                showAlert("Ошибка", "Сначала удалите все классы в категории", Alert.AlertType.WARNING);
                            } else {
                                showAlert("Ошибка", msg);
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
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Класс удалён", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        Platform.runLater(() -> {
                            if (msg.contains("элементы") || msg.contains("компоненты")) {
                                showAlert("Ошибка", "Сначала удалите все элементы в классе", Alert.AlertType.WARNING);
                            } else if (msg.contains("используется")) {
                                showAlert("Ошибка", msg);
                            } else {
                                showAlert("Ошибка", msg);
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
        confirm.setContentText("Вы уверены?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        deleteItemById(id);
                        Platform.runLater(() -> {
                            showAlert("Успешно", "Элемент удалён", Alert.AlertType.INFORMATION);
                            loadData();
                        });
                    } catch (Exception e) {
                        String msg = e.getMessage();
                        Platform.runLater(() -> {
                            if (msg.contains("используется")) {
                                showAlert("Ошибка", "Элемент используется в карточках продукции. Сначала удалите связи.", Alert.AlertType.WARNING);
                            } else {
                                showAlert("Ошибка", msg);
                            }
                        });
                    }
                }).start();
            }
        });
    }

    // УТИЛИТЫ

    protected void showAlert(String title, String message) {
        showAlert(title, message, Alert.AlertType.ERROR);
    }

    protected void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @javafx.fxml.FXML
    protected void handleExportToExcel() {
        if (exportHelper != null) {
            exportHelper.exportToExcel(getExportData(), getExportHeaders(), getItemTypeName(), getItemTypeName());
        }
    }

    /**
     * Возвращает данные для экспорта одного элемента
     */
    protected abstract String[][] getExportDataForItem(Object existing, Object formFields);

    @FXML
    protected void handleCreateCategory() {
        dialogHelper.showCategoryDialog(null, result -> new Thread(() -> {
            try {
                createCategory(result.name(), result.parentId(), result.description());
                Platform.runLater(() -> {
                    showAlert("Успешно", "Категория создана");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать категорию: " + e.getMessage()));
            }
        }).start());
    }

    @FXML
    protected void handleCreateClass() {
        dialogHelper.showClassDialog(null, result -> new Thread(() -> {
            try {
                createClass(result.categoryId(), result.name(), result.description(), null);
                Platform.runLater(() -> {
                    showAlert("Успешно", "Класс создан");
                    loadData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось создать класс: " + e.getMessage()));
            }
        }).start());
    }

    /**
     * Преобразует строку в Double, поддерживая как точку, так и запятую в качестве разделителя.
     *
     * @param value строковое представление числа (например "0.87" или "0,87")
     * @return Double значение или null, если строка пустая или некорректная
     */
    protected Double parseDouble(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            String normalized = value.replace(',', '.');
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Создаёт редактор технических характеристик
     */
    protected TechnicalSpecsEditor createTechnicalSpecsEditor() {
        return new TechnicalSpecsEditor(allUnits);
    }

    // БАЗОВЫЕ МЕТОДЫ ДЛЯ КОМБОБОКСОВ
    /**
     * Создаёт ComboBox для выбора категории
     */
    protected ComboBox<String> createCategoryCombo() {
        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().add("— Все категории —");
        categoryIdMap.clear();

        List<C> rootCategories = allCategories.stream()
                .filter(c -> getCategoryParentId(c) == null)
                .toList();

        for (C rootCat : rootCategories) {
            categoryCombo.getItems().add(getCategoryName(rootCat));
            categoryIdMap.put(getCategoryName(rootCat), getCategoryId(rootCat));
            addChildCategoriesToCombo(categoryCombo, rootCat, 1);
        }
        categoryCombo.setValue("— Все категории —");
        return categoryCombo;
    }

    /**
     * Создаёт ComboBox для выбора класса
     */
    protected ComboBox<String> createClassCombo() {
        ComboBox<String> classCombo = new ComboBox<>();
        classCombo.setPromptText("Выберите класс");
        classCombo.setDisable(true);
        return classCombo;
    }

    /**
     * Добавляет дочерние категории в ComboBox с отступами
     */
    protected void addChildCategoriesToCombo(ComboBox<String> combo, C parent, int depth) {
        String indent = "    ".repeat(depth + 1);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId(c) != null && getCategoryParentId(c).equals(getCategoryId(parent)))
                .toList();
        for (C child : children) {
            String display = indent + getCategoryName(child);
            combo.getItems().add(display);
            categoryIdMap.put(display, getCategoryId(child));
            addChildCategoriesToCombo(combo, child, depth + 1);
        }
    }

    /**
     * Настраивает зависимость категория → класс
     */
    protected void setupCategoryClassDependency(ComboBox<String> categoryCombo,
                                                ComboBox<String> classCombo,
                                                Label warningLabel) {
        categoryCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal == null || "— Все категории —".equals(newVal)) {
                clearClassCombo(classCombo, warningLabel);
                return;
            }

            Long selectedCategoryId = categoryIdMap.get(newVal);
            if (selectedCategoryId == null) {
                clearClassCombo(classCombo, warningLabel);
                return;
            }

            updateClassComboForCategory(selectedCategoryId, classCombo, warningLabel);
        });
    }

    /**
     * Очищает ComboBox классов
     */
    protected void clearClassCombo(ComboBox<String> classCombo, Label warningLabel) {
        classCombo.setDisable(true);
        classCombo.getItems().clear();
        classMap.clear();
        warningLabel.setVisible(false);
    }

    /**
     * Обновляет ComboBox классов для выбранной категории
     */
    protected abstract void updateClassComboForCategory(Long categoryId,
                                                        ComboBox<String> classCombo,
                                                        Label warningLabel);

    /**
     * Заполняет TextField, если значение не null
     */
    protected void fillTextField(TextField field, String value) {
        if (value != null) {
            field.setText(value);
        }
    }

    // БАЗОВЫЙ МЕТОД ДЛЯ ОБНОВЛЕНИЯ СОСТОЯНИЯ КНОПОК
    protected void updateButtonsState(boolean hasSelection, Button editButton, Button deleteButton) {
        editButton.setDisable(!hasSelection);
        deleteButton.setDisable(!hasSelection);
    }

    /**
     * Настраивает диалог создания/редактирования с вкладками
     *
     * @param dialog               диалог
     * @param tabPane              панель вкладок
     * @param mainGrid             GridPane с основными полями
     * @param technicalSpecsEditor редактор технических характеристик
     * @param saveAction           действие при сохранении
     * @param exportAction         действие при экспорте (может быть null)
     */
    protected void setupDialogWithTabs(Dialog<ButtonType> dialog,
                                       TabPane tabPane,
                                       GridPane mainGrid,
                                       TechnicalSpecsEditor technicalSpecsEditor,
                                       Runnable saveAction,
                                       Runnable exportAction) {
        // Создаём вкладку "Основные поля"
        Tab mainTab = new Tab("Основные поля");
        mainTab.setClosable(false);
        ScrollPane scrollPane = new ScrollPane(mainGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        mainTab.setContent(scrollPane);
        tabPane.getTabs().add(mainTab);

        // Создаём вкладку "Технические характеристики"
        Tab technicalTab = new Tab("Технические характеристики");
        technicalTab.setClosable(false);
        technicalTab.setContent(technicalSpecsEditor);
        tabPane.getTabs().add(technicalTab);

        // ========== КНОПКИ ==========
        ButtonType saveButtonType = new ButtonType("Сохранить", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType exportButtonType = new ButtonType("📎 Экспорт в Excel", ButtonBar.ButtonData.OTHER);

        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType, exportButtonType);

        // Настраиваем кнопку "Сохранить"
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        if (saveAction != null) {
            saveButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                event.consume();
                saveAction.run();
            });
        }

        // Настраиваем кнопку "Экспорт в Excel"
        Button exportButton = (Button) dialog.getDialogPane().lookupButton(exportButtonType);
        exportButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        if (exportAction != null) {
            exportButton.setOnAction(e -> exportAction.run());
        } else {
            exportButton.setDisable(true);
        }

        dialog.getDialogPane().setContent(tabPane);
    }

    /**
     * Создаёт базовый диалог с настройками
     * @param title заголовок диалога
     * @return настроенный диалог
     */
    protected Dialog<ButtonType> createBaseDialog(String title) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.initOwner(stage);
        dialog.setResizable(true);
        dialog.setWidth(900);
        dialog.setHeight(700);
        return dialog;
    }

    /**
     * Создаёт базовый TabPane для вкладок
     * @return настроенный TabPane
     */
    protected TabPane createBaseTabPane() {
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(500);
        return tabPane;
    }

    /**
     * Создаёт основную вкладку с GridPane
     * @param title заголовок вкладки
     * @param grid основная форма
     * @return настроенная вкладка
     */
    protected Tab createMainTab(String title, GridPane grid) {
        Tab mainTab = new Tab(title);
        mainTab.setClosable(false);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        mainTab.setContent(scrollPane);

        return mainTab;
    }

    /**
     * Экспортирует данные из диалога в Excel
     *
     * @param formFields           поля формы (Object, чтобы подходило для любого типа)
     * @param technicalSpecsEditor редактор технических характеристик
     * @param dialog               диалог
     * @param existing             существующий элемент (или null для нового)
     * @param itemTypeName         название типа ("компонент" или "материал")
     */
    protected void exportFromDialog(Object formFields,
                                    TechnicalSpecsEditor technicalSpecsEditor,
                                    Dialog<ButtonType> dialog,
                                    Object existing,
                                    String itemTypeName) {
        // ========== 1. ПОЛУЧАЕМ ОСНОВНЫЕ ПОЛЯ через существующий метод ==========
        String[][] mainData = getExportDataForItem(existing, formFields);

        // ========== 2. ПОЛУЧАЕМ ТЕХНИЧЕСКИЕ ХАРАКТЕРИСТИКИ ==========
        Map<String, Object> techSpecsMap = technicalSpecsEditor.getTechnicalSpecs();
        List<TechnicalSpec> techSpecs = new ArrayList<>();
        if (techSpecsMap != null && !techSpecsMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : techSpecsMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                String valueStr = "";
                Long unitId = null;
                String unitCode = null;

                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    valueStr = valueMap.get("value") != null ? valueMap.get("value").toString() : "";
                    Object unitIdObj = valueMap.get("unitId");
                    if (unitIdObj instanceof Number) {
                        unitId = ((Number) unitIdObj).longValue();
                    }
                    unitCode = (String) valueMap.get("unitCode");
                } else {
                    valueStr = value != null ? value.toString() : "";
                }
                techSpecs.add(new TechnicalSpec(key, valueStr, unitId, unitCode));
            }
        }

        // ========== 3. СОБИРАЕМ ДАННЫЕ ДЛЯ ПЕРВОГО ЛИСТА ==========
        List<ExcelExportUtil.ExcelRowData> mainSheetData = new ArrayList<>();
        for (String[] row : mainData) {
            if (row.length >= 2) {
                mainSheetData.add(new ExcelExportUtil.ExcelRowData(row[0], row[1]));
            }
        }

        // ========== 4. ПОЛУЧАЕМ ИМЯ ДЛЯ ФАЙЛА ==========
        String name = extractNameFromForm(formFields);
        String fileNamePrefix = (name == null || name.isEmpty()) ? itemTypeName : name;

        // ========== 5. ЭКСПОРТ ==========
        ExcelExportUtil.exportToExcel(
                dialog.getOwner(),
                fileNamePrefix,
                "Основные поля",
                mainSheetData,
                "Технические характеристики",
                techSpecs
        );
    }

    /**
     * Извлекает имя из формы (для разных типов форм)
     */
    private String extractNameFromForm(Object formFields) {
        if (formFields == null) {
            return null;
        }

        // Пытаемся получить name через рефлексию
        try {
            java.lang.reflect.Field nameField = formFields.getClass().getDeclaredField("name");
            nameField.setAccessible(true);
            Object nameObj = nameField.get(formFields);
            if (nameObj instanceof TextField) {
                return ((TextField) nameObj).getText().trim();
            }
            if (nameObj instanceof String) {
                return ((String) nameObj).trim();
            }
        } catch (Exception e) {
            // Игнорируем — возвращаем null
        }
        return null;
    }

}

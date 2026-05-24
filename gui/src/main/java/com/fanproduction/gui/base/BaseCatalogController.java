package com.fanproduction.gui.base;


import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
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
    protected abstract void createClass(Long categoryId, String name, String description, Long unitId) throws Exception;

    // ========== ОСНОВНЫЕ МЕТОДЫ ==========

    protected void loadData() {
        new Thread(() -> {
            try {
                allUnits = fetchUnits();
                allCategories = fetchCategories();
                allClasses = fetchClasses();
                List<T> items = fetchAllItems();

                // Инициализация хелперов
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
                this::getCategoryId,
                this::getCategoryName,
                this::getCategoryParentId,
                this::getCategoryDescription,
                this::getClassId,
                this::getClassName,
                this::getClassCategoryId,
                this::getClassDescription);
    }

    protected void refreshTree() {
        treeBuilder = new CatalogTreeBuilder<>(allCategories, allClasses, itemList,
                this::getCategoryId, this::getCategoryParentId, this::getCategoryName,
                this::getClassId, this::getClassCategoryId, this::getClassName,
                this::getItemId, this::getItemClassId, this::getItemName);

        TreeView<CategoryTreeItem> newTree = treeBuilder.buildTree();
        CatalogTreeBuilder.setupTreeCellFactory(newTree);

        TreeView<CategoryTreeItem> oldTree = getTreeView();
        if (oldTree != null) {
            // Получаем корень из нового дерева и устанавливаем его в старое
            TreeItem<CategoryTreeItem> newRoot = newTree.getRoot();
            if (newRoot != null) {
                oldTree.setRoot(newRoot);
                oldTree.setShowRoot(false);
            }
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
                            loadData();
                            showAlert("Успешно", "Элемент удалён", Alert.AlertType.INFORMATION);
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", e.getMessage()));
                    }
                }).start();
            }
        });
    }

    protected List<UnitOfMeasureDto> fetchUnits() {
        // Базовый метод, может быть переопределён в наследниках
        return new ArrayList<>();
    }

    // ========== ОБРАБОТЧИКИ ==========

    @javafx.fxml.FXML
    protected void handleExportToExcel() {
        if (exportHelper != null) {
            exportHelper.exportToExcel(getExportData(), getExportHeaders(), getItemTypeName(), getItemTypeName());
        }
    }
}
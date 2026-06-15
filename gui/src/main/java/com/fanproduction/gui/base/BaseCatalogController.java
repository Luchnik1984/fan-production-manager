package com.fanproduction.gui.base;


import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

}

package com.fanproduction.gui.base;

import com.fanproduction.gui.component.IconFactory;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.List;
import java.util.function.Function;

/**
 * Построитель дерева категорий для каталогов.
 * Отвечает только за создание и настройку TreeView.
 *
 * @param <C> тип категории
 * @param <CL> тип класса
 * @param <T> тип элемента (компонент/материал)
 */
public class CatalogTreeBuilder<C, CL, T> {

    private final List<C> allCategories;
    private final List<CL> allClasses;
    private final List<T> itemList;

    private final Function<C, Long> getCategoryId;
    private final Function<C, Long> getCategoryParentId;
    private final Function<C, String> getCategoryName;
    private final Function<CL, Long> getClassId;
    private final Function<CL, Long> getClassCategoryId;
    private final Function<CL, String> getClassName;
    private final Function<T, Long> getItemId;
    private final Function<T, Long> getItemClassId;
    private final Function<T, String> getItemName;

    public CatalogTreeBuilder(List<C> allCategories,
                              List<CL> allClasses,
                              List<T> itemList,
                              Function<C, Long> getCategoryId,
                              Function<C, Long> getCategoryParentId,
                              Function<C, String> getCategoryName,
                              Function<CL, Long> getClassId,
                              Function<CL, Long> getClassCategoryId,
                              Function<CL, String> getClassName,
                              Function<T, Long> getItemId,
                              Function<T, Long> getItemClassId,
                              Function<T, String> getItemName) {
        this.allCategories = allCategories;
        this.allClasses = allClasses;
        this.itemList = itemList;
        this.getCategoryId = getCategoryId;
        this.getCategoryParentId = getCategoryParentId;
        this.getCategoryName = getCategoryName;
        this.getClassId = getClassId;
        this.getClassCategoryId = getClassCategoryId;
        this.getClassName = getClassName;
        this.getItemId = getItemId;
        this.getItemClassId = getItemClassId;
        this.getItemName = getItemName;
    }

    /**
     * Строит дерево категорий
     */
    public TreeView<CategoryTreeItem> buildTree() {
        TreeView<CategoryTreeItem> treeView = new TreeView<>();

        TreeItem<CategoryTreeItem> rootItem = new TreeItem<>();
        rootItem.setValue(null);
        rootItem.setExpanded(true);

        // Корневой элемент "Все"
        TreeItem<CategoryTreeItem> allItem = new TreeItem<>(
                new CategoryTreeItem(null, "Все", "root"));
        allItem.setExpanded(true);
        rootItem.getChildren().add(allItem);

        // Добавляем корневые категории
        List<C> rootCategories = allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) == null)
                .toList();

        for (C category : rootCategories) {
            addCategoryToTree(allItem, category);
        }

        treeView.setRoot(rootItem);
        treeView.setShowRoot(false);

        return treeView;
    }

    /**
     * Рекурсивно добавляет категорию и её дочерние элементы в дерево
     */
    private void addCategoryToTree(TreeItem<CategoryTreeItem> parent, C category) {
        Long categoryId = getCategoryId.apply(category);
        String categoryName = getCategoryName.apply(category);

        TreeItem<CategoryTreeItem> categoryItem = new TreeItem<>(
                new CategoryTreeItem(categoryId, categoryName, "category"));
        categoryItem.setExpanded(true);
        categoryItem.setGraphic(IconFactory.createFolderIcon());
        parent.getChildren().add(categoryItem);

        // Добавляем классы этой категории
        List<CL> classesInCategory = allClasses.stream()
                .filter(cls -> getClassCategoryId.apply(cls) != null
                        && getClassCategoryId.apply(cls).equals(categoryId))
                .toList();

        for (CL cls : classesInCategory) {
            addClassToTree(categoryItem, cls);
        }

        // Добавляем дочерние категории
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) != null
                        && getCategoryParentId.apply(c).equals(categoryId))
                .toList();

        for (C child : children) {
            addCategoryToTree(categoryItem, child);
        }
    }

    /**
     * Добавляет класс и его элементы в дерево
     */
    private void addClassToTree(TreeItem<CategoryTreeItem> parent, CL cls) {
        Long classId = getClassId.apply(cls);
        String className = getClassName.apply(cls);

        TreeItem<CategoryTreeItem> classItem = new TreeItem<>(
                new CategoryTreeItem(classId, className, "class", classId));
        classItem.setGraphic(IconFactory.createClassIcon());

        // Добавляем элементы этого класса
        List<T> itemsInClass = itemList.stream()
                .filter(item -> getItemClassId.apply(item).equals(classId))
                .toList();

        for (T item : itemsInClass) {
            TreeItem<CategoryTreeItem> itemNode = new TreeItem<>(
                    new CategoryTreeItem(getItemId.apply(item), getItemName.apply(item), "item", classId));
            itemNode.setGraphic(IconFactory.createFileIcon());
            classItem.getChildren().add(itemNode);
        }

        parent.getChildren().add(classItem);
    }

    /**
     * Настраивает CellFactory для дерева (иконки и стили)
     */
    public static void setupTreeCellFactory(TreeView<CategoryTreeItem> treeView) {
        treeView.setCellFactory(tv -> new javafx.scene.control.TreeCell<>() {
            @Override
            protected void updateItem(CategoryTreeItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item.getDisplayName());

                    if ("category".equals(item.getType())) {
                        setGraphic(IconFactory.createFolderIcon());
                        setStyle("-fx-font-weight: bold;");
                    } else if ("class".equals(item.getType())) {
                        setGraphic(IconFactory.createClassIcon());
                        setStyle("-fx-font-style: italic; -fx-text-fill: #555;");
                    } else if ("item".equals(item.getType())) {
                        setGraphic(IconFactory.createFileIcon());
                        setStyle("-fx-font-weight: normal;");
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
    }

    /**
     * Настраивает контекстное меню для дерева (правой кнопкой мыши)
     * @param treeView дерево, для которого настраивается меню
     * @param onEditCategory действие при редактировании категории
     * @param onEditClass действие при редактировании класса
     * @param onEditItem действие при редактировании элемента
     * @param onDeleteCategory действие при удалении категории
     * @param onDeleteClass действие при удалении класса
     * @param onDeleteItem действие при удалении элемента
     */
    public static void setupContextMenu(TreeView<CategoryTreeItem> treeView,
                                        Runnable onEditCategory,
                                        Runnable onEditClass,
                                        Runnable onEditItem,
                                        Runnable onDeleteCategory,
                                        Runnable onDeleteClass,
                                        Runnable onDeleteItem) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Редактировать");
        editItem.setGraphic(IconFactory.createEditIcon());
        MenuItem deleteItem = new MenuItem("Удалить");
        deleteItem.setGraphic(IconFactory.createDeleteIcon());

        contextMenu.getItems().addAll(editItem, deleteItem);

        treeView.setContextMenu(contextMenu);

        treeView.setOnContextMenuRequested(event -> {
            TreeItem<CategoryTreeItem> selectedItem = treeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null || selectedItem.getValue() == null) {
                contextMenu.hide();
                return;
            }

            CategoryTreeItem item = selectedItem.getValue();
            String type = item.getType();

            // Настраиваем действия в зависимости от типа выбранного элемента
            editItem.setOnAction(e -> {
                if ("category".equals(type) && onEditCategory != null) {
                    onEditCategory.run();
                } else if ("class".equals(type) && onEditClass != null) {
                    onEditClass.run();
                } else if ("item".equals(type) && onEditItem != null) {
                    onEditItem.run();
                }
            });

            deleteItem.setOnAction(e -> {
                if ("category".equals(type) && onDeleteCategory != null) {
                    onDeleteCategory.run();
                } else if ("class".equals(type) && onDeleteClass != null) {
                    onDeleteClass.run();
                } else if ("item".equals(type) && onDeleteItem != null) {
                    onDeleteItem.run();
                }
            });

            contextMenu.show(treeView, event.getScreenX(), event.getScreenY());
        });
    }
}

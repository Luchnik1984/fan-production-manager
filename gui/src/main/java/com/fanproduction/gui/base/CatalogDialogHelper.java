package com.fanproduction.gui.base;

import com.fanproduction.gui.dto.response.UnitOfMeasureDto;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Хелпер для создания диалогов в каталогах.
 * Полностью сохраняет логику оригинального BaseCatalogController.
 */
public class CatalogDialogHelper<C, CL> {

    private final Stage ownerStage;
    private final List<C> allCategories;
    private final List<CL> allClasses;
    private final List<UnitOfMeasureDto> allUnits;

    private final Function<C, Long> getCategoryId;
    private final Function<C, String> getCategoryName;
    private final Function<C, Long> getCategoryParentId;
    private final Function<C, String> getCategoryDescription;
    private final Function<CL, Long> getClassId;
    private final Function<CL, String> getClassName;
    private final Function<CL, Long> getClassCategoryId;
    private final Function<CL, String> getClassDescription;

    public CatalogDialogHelper(Stage ownerStage,
                               List<C> allCategories,
                               List<CL> allClasses,
                               List<UnitOfMeasureDto> allUnits,
                               Function<C, Long> getCategoryId,
                               Function<C, String> getCategoryName,
                               Function<C, Long> getCategoryParentId,
                               Function<C, String> getCategoryDescription,
                               Function<CL, Long> getClassId,
                               Function<CL, String> getClassName,
                               Function<CL, Long> getClassCategoryId,
                               Function<CL, String> getClassDescription) {
        this.ownerStage = ownerStage;
        this.allCategories = allCategories;
        this.allClasses = allClasses;
        this.allUnits = allUnits;
        this.getCategoryId = getCategoryId;
        this.getCategoryName = getCategoryName;
        this.getCategoryParentId = getCategoryParentId;
        this.getCategoryDescription = getCategoryDescription;
        this.getClassId = getClassId;
        this.getClassName = getClassName;
        this.getClassCategoryId = getClassCategoryId;
        this.getClassDescription = getClassDescription;
    }

    // ==========================================
    // ДИАЛОГ КАТЕГОРИИ
    // ==========================================

    /**
     * Показывает диалог создания/редактирования категории
     */
    public void showCategoryDialog(C existingCategory,
                                   Consumer<CategoryDialogResult> onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingCategory == null ? "Создание категории" : "Редактирование категории");
        dialog.setHeaderText(existingCategory == null ? "Создание новой категории" : "Редактирование категории \"" + getCategoryName.apply(existingCategory) + "\"");
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // ========== ВЫБОР РОДИТЕЛЬСКОЙ КАТЕГОРИИ ==========
        ComboBox<String> parentCombo = new ComboBox<>();
        parentCombo.getItems().add("— Корневая категория —");

        for (C rootCat : allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) == null).toList()) {
            if (existingCategory != null && getCategoryId.apply(rootCat).equals(getCategoryId.apply(existingCategory))) {
                continue;
            }
            parentCombo.getItems().add(getCategoryName.apply(rootCat));
            addChildCategories(parentCombo, rootCat, 1, existingCategory);
        }

        // Устанавливаем текущего родителя
        if (existingCategory != null) {
            Long currentParentId = getCategoryParentId.apply(existingCategory);
            if (currentParentId == null) {
                parentCombo.setValue("— Корневая категория —");
            } else {
                for (C cat : allCategories) {
                    if (getCategoryId.apply(cat).equals(currentParentId)) {
                        parentCombo.setValue(getCategoryName.apply(cat));
                        break;
                    }
                }
            }
        } else {
            parentCombo.setValue("— Корневая категория —");
        }

        // ========== НАЗВАНИЕ ==========
        TextField nameField = new TextField();
        if (existingCategory != null) {
            nameField.setText(getCategoryName.apply(existingCategory));
        }
        nameField.setPromptText("Название категории");

        // ========== ОПИСАНИЕ ==========
        TextArea descriptionField = new TextArea();
        if (existingCategory != null) {
            String existingDescription = getCategoryDescription.apply(existingCategory);
            if (existingDescription != null) {
                descriptionField.setText(existingDescription);
            }
        }
        descriptionField.setPromptText("Описание (необязательно)");
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
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showError("Введите название категории");
                    return;
                }

                String parentName = parentCombo.getValue();
                final Long finalParentId;
                if (!"— Корневая категория —".equals(parentName) && parentName != null) {
                    String cleanName = parentName.replaceAll("^\\s+", "");
                    finalParentId = findCategoryIdByName(cleanName);
                } else {
                    finalParentId = null;
                }

                onSave.accept(new CategoryDialogResult(name, finalParentId, descriptionField.getText()));
            }
        });
    }

    private void addChildCategories(ComboBox<String> combo, C parent, int depth, C excludeCategory) {
        String indent = "    ".repeat(depth);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) != null
                        && getCategoryParentId.apply(c).equals(getCategoryId.apply(parent)))
                .toList();

        for (C child : children) {
            if (excludeCategory != null && getCategoryId.apply(child).equals(getCategoryId.apply(excludeCategory))) {
                continue;
            }
            String display = indent + getCategoryName.apply(child);
            combo.getItems().add(display);
            addChildCategories(combo, child, depth + 1, excludeCategory);
        }
    }

    // ==========================================
    // ДИАЛОГ КЛАССА (без выбора единицы измерения)
    // ==========================================

    /**
     * Показывает диалог создания/редактирования класса
     */
    public void showClassDialog(CL existingClass,
                                Consumer<ClassDialogResult> onSave) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(existingClass == null ? "Создание класса" : "Редактирование класса");
        dialog.setHeaderText(existingClass == null ? "Создание нового класса" : "Редактирование класса \"" + getClassName.apply(existingClass) + "\"");
        dialog.initOwner(ownerStage);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // ========== ВЫБОР КАТЕГОРИИ ==========
        ComboBox<String> categoryCombo = new ComboBox<>();
        Map<String, Long> categoryIdMap = new java.util.HashMap<>();

        for (C rootCat : allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) == null).toList()) {
            categoryCombo.getItems().add(getCategoryName.apply(rootCat));
            categoryIdMap.put(getCategoryName.apply(rootCat), getCategoryId.apply(rootCat));
            addChildCategoriesToCombo(categoryCombo, categoryIdMap, rootCat, 1);
        }

        if (existingClass != null) {
            Long currentCategoryId = getClassCategoryId.apply(existingClass);
            for (C cat : allCategories) {
                if (getCategoryId.apply(cat).equals(currentCategoryId)) {
                    categoryCombo.setValue(getCategoryName.apply(cat));
                    break;
                }
            }
        }

        // ========== НАЗВАНИЕ ==========
        TextField nameField = new TextField();
        if (existingClass != null) {
            nameField.setText(getClassName.apply(existingClass));
        }
        nameField.setPromptText("Название класса");

        // ========== ОПИСАНИЕ ==========
        TextArea descriptionField = new TextArea();
        if (existingClass != null) {
            String existingDescription = getClassDescription.apply(existingClass);
            if (existingDescription != null) {
                descriptionField.setText(existingDescription);
            }
        }
        descriptionField.setPromptText("Описание (необязательно)");
        descriptionField.setPrefRowCount(3);

        int row = 0;
        grid.add(new Label("Категория:*"), 0, row);
        grid.add(categoryCombo, 1, row++);
        grid.add(new Label("Название:*"), 0, row);
        grid.add(nameField, 1, row++);
        grid.add(new Label("Описание:"), 0, row);
        grid.add(descriptionField, 1, row++);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String selectedCategory = categoryCombo.getValue();
                String name = nameField.getText().trim();

                if (selectedCategory == null) {
                    showError("Выберите категорию");
                    return;
                }
                if (name.isEmpty()) {
                    showError("Введите название класса");
                    return;
                }

                final Long finalCategoryId = categoryIdMap.get(selectedCategory);

                onSave.accept(new ClassDialogResult(finalCategoryId, name, descriptionField.getText()));
            }
        });
    }

    private void addChildCategoriesToCombo(ComboBox<String> combo, Map<String, Long> idMap, C parent, int depth) {
        String indent = "    ".repeat(depth);
        List<C> children = allCategories.stream()
                .filter(c -> getCategoryParentId.apply(c) != null
                        && getCategoryParentId.apply(c).equals(getCategoryId.apply(parent)))
                .toList();

        for (C child : children) {
            String display = indent + getCategoryName.apply(child);
            combo.getItems().add(display);
            idMap.put(display, getCategoryId.apply(child));
            addChildCategoriesToCombo(combo, idMap, child, depth + 1);
        }
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    private Long findCategoryIdByName(String name) {
        String cleanName = name.replaceAll("^\\s+", "");
        for (C cat : allCategories) {
            if (getCategoryName.apply(cat).equals(cleanName)) {
                return getCategoryId.apply(cat);
            }
        }
        return null;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==========================================
    // RECORD DTO
    // ==========================================

    public record CategoryDialogResult(String name, Long parentId, String description) {}
    public record ClassDialogResult(Long categoryId, String name, String description) {}
}

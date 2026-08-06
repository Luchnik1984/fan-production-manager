package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.MaterialCategoryDto;
import com.fanproduction.gui.dto.response.MaterialClassDto;
import com.fanproduction.gui.dto.response.MaterialDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MaterialTreeBuilder {

    private final List<MaterialCategoryDto> allCategories;
    private final List<MaterialClassDto> allClasses;
    private final Map<Long, List<MaterialDto>> materialsByClassId;

    public MaterialTreeBuilder(List<MaterialCategoryDto> allCategories,
                               List<MaterialClassDto> allClasses,
                               List<MaterialDto> allMaterials) {
        this.allCategories = allCategories;
        this.allClasses = allClasses;
        this.materialsByClassId = allMaterials.stream()
                .collect(Collectors.groupingBy(MaterialDto::getClassId));
    }

    public TreeItem<Object> buildTree() {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        TreeItem<Object> allItem = new TreeItem<>("Все материалы");
        allItem.setExpanded(true);
        root.getChildren().add(allItem);

        List<MaterialCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (MaterialCategoryDto category : rootCategories) {
            TreeItem<Object> categoryItem = buildCategoryTreeItem(category);
            allItem.getChildren().add(categoryItem);
        }

        return root;
    }

    private TreeItem<Object> buildCategoryTreeItem(MaterialCategoryDto category) {
        // Храним сам DTO (Displayable позаботится об отображении)
        TreeItem<Object> categoryItem = new TreeItem<>(category);
        categoryItem.setExpanded(true);
        categoryItem.setGraphic(IconFactory.createFolderIcon());

        List<MaterialClassDto> classesInCategory = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(category.getId()))
                .toList();

        for (MaterialClassDto cls : classesInCategory) {
            TreeItem<Object> classItem = new TreeItem<>(cls);
            classItem.setExpanded(true);
            classItem.setGraphic(IconFactory.createClassIcon());

            List<MaterialDto> materials = materialsByClassId.getOrDefault(cls.getId(), List.of());
            for (MaterialDto mat : materials) {
                TreeItem<Object> materialItem = new TreeItem<>(mat);
                materialItem.setGraphic(IconFactory.createFileIcon());
                classItem.getChildren().add(materialItem);
            }

            categoryItem.getChildren().add(classItem);
        }

        List<MaterialCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .toList();

        for (MaterialCategoryDto child : children) {
            categoryItem.getChildren().add(buildCategoryTreeItem(child));
        }

        return categoryItem;
    }
}

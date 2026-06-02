package com.fanproduction.gui.builder;

import com.fanproduction.gui.component.IconFactory;
import com.fanproduction.gui.dto.response.ComponentCategoryDto;
import com.fanproduction.gui.dto.response.ComponentClassDto;
import com.fanproduction.gui.dto.response.ComponentDto;
import javafx.scene.control.TreeItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComponentTreeBuilder {

    private final List<ComponentCategoryDto> allCategories;
    private final List<ComponentClassDto> allClasses;
    private final Map<Long, List<ComponentDto>> componentsByClassId;

    public ComponentTreeBuilder(List<ComponentCategoryDto> allCategories,
                                List<ComponentClassDto> allClasses,
                                List<ComponentDto> allComponents) {
        this.allCategories = allCategories;
        this.allClasses = allClasses;
        this.componentsByClassId = allComponents.stream()
                .collect(Collectors.groupingBy(ComponentDto::getClassId));
    }

    public TreeItem<Object> buildTree() {
        TreeItem<Object> root = new TreeItem<>();
        root.setValue(null);
        root.setExpanded(true);

        TreeItem<Object> allItem = new TreeItem<>("Все компоненты");
        allItem.setExpanded(true);
        root.getChildren().add(allItem);

        List<ComponentCategoryDto> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .toList();

        for (ComponentCategoryDto category : rootCategories) {
            TreeItem<Object> categoryItem = buildCategoryTreeItem(category);
            allItem.getChildren().add(categoryItem);
        }

        return root;
    }

    private TreeItem<Object> buildCategoryTreeItem(ComponentCategoryDto category) {
        // Храним сам DTO (Displayable позаботится об отображении)
        TreeItem<Object> categoryItem = new TreeItem<>(category);
        categoryItem.setExpanded(true);
        categoryItem.setGraphic(IconFactory.createFolderIcon());

        List<ComponentClassDto> classesInCategory = allClasses.stream()
                .filter(cls -> cls.getCategoryId() != null && cls.getCategoryId().equals(category.getId()))
                .toList();

        for (ComponentClassDto cls : classesInCategory) {
            TreeItem<Object> classItem = new TreeItem<>(cls);
            classItem.setExpanded(true);
            classItem.setGraphic(IconFactory.createClassIcon());

            List<ComponentDto> components = componentsByClassId.getOrDefault(cls.getId(), List.of());
            for (ComponentDto comp : components) {
                TreeItem<Object> compItem = new TreeItem<>(comp);
                compItem.setGraphic(IconFactory.createFileIcon());
                classItem.getChildren().add(compItem);
            }

            categoryItem.getChildren().add(classItem);
        }

        List<ComponentCategoryDto> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(category.getId()))
                .toList();

        for (ComponentCategoryDto child : children) {
            categoryItem.getChildren().add(buildCategoryTreeItem(child));
        }

        return categoryItem;
    }
}

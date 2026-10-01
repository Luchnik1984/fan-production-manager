package com.fanproduction.services;

import com.fanproduction.core.entity.component.ComponentCategoryEntity;
import com.fanproduction.core.entity.component.ComponentClassEntity;
import com.fanproduction.core.entity.component.ComponentEntity;
import com.fanproduction.core.entity.component.ProductComponentEntity;
import com.fanproduction.core.entity.dictionary.UnitOfMeasureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления компонентами и комплектующими.
 */
public interface ComponentService {

    // ========== Component Category ==========
    List<ComponentCategoryEntity> getAllCategories();
    Optional<ComponentCategoryEntity> getCategoryById(Long id);
    ComponentCategoryEntity createCategory(String name, Long parentId, String description, String createdBy);
    ComponentCategoryEntity updateCategory(Long id, String name, Integer sortOrder);
    void deleteCategory(Long id);
    boolean isCategoryInUse(Long id);
    ComponentCategoryEntity updateCategory(Long id, String name, Long parentId, String description);

    // ========== Component Class ==========
    List<ComponentClassEntity> getAllComponentClasses();
    List<ComponentClassEntity> getClassesByCategory(Long categoryId);
    Optional<ComponentClassEntity> getComponentClassById(Long id);
    Optional<ComponentClassEntity> getComponentClassByName(String name);
    ComponentClassEntity createComponentClass(Long categoryId, String name, String description, String createdBy, Long unitId);
    ComponentClassEntity updateComponentClass(Long id, String name, String description);
    void deleteComponentClass(Long id);
    boolean isComponentClassInUse(Long id);
    ComponentClassEntity updateClass(Long id, String name, Long categoryId, String description);
    void deleteClass(Long id);

    // ========== Component ==========
    List<ComponentEntity> getAllComponents();
    Page<ComponentEntity> getComponentsByClass(Long classId, Pageable pageable);
    Optional<ComponentEntity> getComponentById(Long id);
    ComponentEntity createComponent(ComponentEntity component);
    ComponentEntity updateComponent(Long id, ComponentEntity component);
    void deleteComponent(Long id);
    List<ComponentEntity> searchComponents(String query);
    List<ComponentEntity> getComponentsByClassAndName(Long classId, String search);

    // ========== Unit of Measure ==========
    List<UnitOfMeasureEntity> getAllUnits();
    Optional<UnitOfMeasureEntity> getUnitById(Long id);
    Optional<UnitOfMeasureEntity> getUnitByCode(String code);
    UnitOfMeasureEntity getDefaultUnit();

    // ========== Product Component ==========
    List<ProductComponentEntity> getComponentsByProductCard(Long productCardId);
    ProductComponentEntity addComponentToProduct(Long productCardId, Long componentId, Double quantity, String position, String note);
    void updateComponentQuantity(Long productCardId, Long componentId, Double quantity);
    void updateComponentPosition(Long productCardId, Long componentId, String position);
    void removeComponentFromProduct(Long productCardId, Long componentId);
    void removeAllComponentsFromProduct(Long productCardId);
    void updateComponentNote(Long productCardId, Long componentId, String note);

    boolean hasChildrenCategories(Long id);
    boolean hasClassesInCategory(Long id);
    boolean hasComponentsInClass(Long id);
    boolean isClassUsedInProducts(Long id);
    boolean isComponentUsedInProducts(Long id);
}

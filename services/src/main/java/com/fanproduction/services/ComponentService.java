package com.fanproduction.services;

import com.fanproduction.core.entity.ComponentClassEntity;
import com.fanproduction.core.entity.ComponentEntity;
import com.fanproduction.core.entity.ProductComponentEntity;
import com.fanproduction.core.entity.UnitOfMeasureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления компонентами и комплектующими.
 */
public interface ComponentService {

    // ========== Component Class ==========
    List<ComponentClassEntity> getAllComponentClasses();
    Optional<ComponentClassEntity> getComponentClassById(Long id);
    Optional<ComponentClassEntity> getComponentClassByName(String name);
    ComponentClassEntity createComponentClass(String name, String description, String createdBy);
    ComponentClassEntity updateComponentClass(Long id, String name, String description);
    void deleteComponentClass(Long id);
    boolean isComponentClassInUse(Long id);  // есть ли компоненты в этом классе

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
    ProductComponentEntity addComponentToProduct(Long productCardId, Long componentId, Double quantity, String note);
    void updateComponentQuantity(Long productCardId, Long componentId, Double quantity);
    void removeComponentFromProduct(Long productCardId, Long componentId);
    void removeAllComponentsFromProduct(Long productCardId);
}

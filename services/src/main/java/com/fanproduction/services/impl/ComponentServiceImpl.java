package com.fanproduction.services.impl;

import com.fanproduction.core.entity.component.ComponentCategoryEntity;
import com.fanproduction.core.entity.component.ComponentClassEntity;
import com.fanproduction.core.entity.component.ComponentEntity;
import com.fanproduction.core.entity.component.ProductComponentEntity;
import com.fanproduction.core.entity.dictionary.UnitOfMeasureEntity;
import com.fanproduction.repositories.component.ComponentCategoryRepository;
import com.fanproduction.repositories.component.ComponentClassRepository;
import com.fanproduction.repositories.component.ComponentRepository;
import com.fanproduction.repositories.component.ProductComponentRepository;
import com.fanproduction.repositories.dictionary.UnitOfMeasureRepository;
import com.fanproduction.services.ComponentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ComponentServiceImpl implements ComponentService {

    private final ComponentClassRepository componentClassRepository;
    private final ComponentRepository componentRepository;
    private final ProductComponentRepository productComponentRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final ComponentCategoryRepository componentCategoryRepository;

    // ========== Component Category ==========

    @Override
    public List<ComponentCategoryEntity> getAllCategories() {
        return componentCategoryRepository.findAllByOrderByPathAsc();
    }

    @Override
    @Transactional
    public ComponentCategoryEntity createCategory(String name, Long parentId, String description, String createdBy) {
        // Определяем уровень и путь
        int level = 1;
        String path = "/" + name + "/";
        Integer sortOrder = 0;

        if (parentId != null) {
            ComponentCategoryEntity parent = componentCategoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена"));
            level = parent.getLevel() + 1;
            path = parent.getPath() + name + "/";
            sortOrder = componentCategoryRepository.findByParentIdOrderBySortOrderAsc(parentId).size();
        }

        ComponentCategoryEntity entity = new ComponentCategoryEntity();
        entity.setName(name);
        entity.setParentId(parentId);
        entity.setLevel(level);
        entity.setPath(path);
        entity.setSortOrder(sortOrder);
        entity.setDescription(description);
        entity.setCreatedBy(createdBy);

        return componentCategoryRepository.save(entity);
    }

    @Override
    @Transactional
    public ComponentCategoryEntity updateCategory(Long id, String name, Integer sortOrder) {
        ComponentCategoryEntity entity = componentCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        if (name != null && !name.equals(entity.getName())) {
            // Обновляем путь
            String oldPath = entity.getPath();
            String newPath = oldPath.substring(0, oldPath.lastIndexOf(entity.getName() + "/")) + name + "/";
            entity.setPath(newPath);
            entity.setName(name);
        }
        if (sortOrder != null) {
            entity.setSortOrder(sortOrder);
        }

        return componentCategoryRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        long childrenCount = componentCategoryRepository.countByParentId(id);
        if (childrenCount > 0) {
            System.out.println("Throwing: есть дочерние категории");
            throw new IllegalStateException("У категории есть дочерние категории. Удалите их сначала.");
        }
        long classesCount = componentClassRepository.countByCategoryId(id);
        if (classesCount > 0) {
            throw new IllegalStateException("В категории есть классы. Удалите их сначала.");
        }
        componentCategoryRepository.deleteById(id);
    }

    @Override
    public boolean isCategoryInUse(Long id) {
        return componentClassRepository.countByCategoryId(id) > 0;
    }

    @Override
    public Optional<ComponentCategoryEntity> getCategoryById(Long id) {
        return componentCategoryRepository.findById(id);
    }

    // ========== Component Class ==========

    @Override
    public List<ComponentClassEntity> getAllComponentClasses() {
        return componentClassRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<ComponentClassEntity> getClassesByCategory(Long categoryId) {
        return componentClassRepository.findByCategoryId(categoryId);
    }

    @Override
    public Optional<ComponentClassEntity> getComponentClassById(Long id) {
        return componentClassRepository.findById(id);
    }

    @Override
    public Optional<ComponentClassEntity> getComponentClassByName(String name) {
        return componentClassRepository.findByName(name);
    }

    @Override
    @Transactional
    public ComponentClassEntity createComponentClass(Long categoryId, String name, String description, String createdBy, Long unitId) {
        if (componentClassRepository.existsByName(name)) {
            throw new IllegalArgumentException("Класс компонента с таким именем уже существует: " + name);
        }

        // Проверяем существование категории
        componentCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));

        ComponentClassEntity entity = new ComponentClassEntity();
        entity.setCategoryId(categoryId);
        entity.setName(name);
        entity.setDescription(description);
        entity.setUnitId(unitId);
        entity.setCreatedBy(createdBy);
        return componentClassRepository.save(entity);
    }

    @Override
    @Transactional
    public ComponentClassEntity updateComponentClass(Long id, String name, String description) {
        ComponentClassEntity entity = componentClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Класс компонента не найден: " + id));

        if (name != null && !name.equals(entity.getName())) {
            if (componentClassRepository.existsByName(name)) {
                throw new IllegalArgumentException("Класс компонента с таким именем уже существует: " + name);
            }
            entity.setName(name);
        }
        if (description != null) {
            entity.setDescription(description);
        }
        return componentClassRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteComponentClass(Long id) {
        // Проверяем наличие компонентов
        long componentsCount = componentRepository.countByClassId(id);
        if (componentsCount > 0) {
            throw new IllegalStateException("В классе есть компоненты. Удалите их сначала.");
        }
        componentClassRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        // Вызываем deleteComponentClass, чтобы не дублировать логику
        deleteComponentClass(id);
    }

    @Override
    public boolean isComponentClassInUse(Long id) {
        return componentRepository.countByClassId(id) > 0;
    }

    // ========== Component ==========

    @Override
    public List<ComponentEntity> getAllComponents() {
        return componentRepository.findAll();
    }

    @Override
    public Page<ComponentEntity> getComponentsByClass(Long classId, Pageable pageable) {
        return componentRepository.findByClassId(classId, pageable);
    }

    @Override
    public Optional<ComponentEntity> getComponentById(Long id) {
        return componentRepository.findById(id);
    }

    @Override
    @Transactional
    public ComponentEntity createComponent(ComponentEntity component) {
        // Проверяем существование класса
        componentClassRepository.findById(component.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Класс компонента не найден: " + component.getClassId()));

        // Проверяем существование единицы измерения
        unitOfMeasureRepository.findById(component.getUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Единица измерения не найдена: " + component.getUnitId()));

        // Проверяем уникальность имени в рамках класса
        Optional<ComponentEntity> existing = componentRepository.findByClassIdAndName(component.getClassId(), component.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Компонент с таким именем уже существует в этом классе");
        }

        return componentRepository.save(component);
    }

    @Override
    @Transactional
    public ComponentEntity updateComponent(Long id, ComponentEntity updated) {
        ComponentEntity existing = componentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Компонент не найден: " + id));

        //  Обновление ClassId
        if (updated.getClassId() != null && !updated.getClassId().equals(existing.getClassId())) {
            // Проверяем, существует ли новый класс
            componentClassRepository.findById(updated.getClassId())
                    .orElseThrow(() -> new IllegalArgumentException("Класс не найден: " + updated.getClassId()));
            // Проверяем уникальность имени в новом классе
            Optional<ComponentEntity> duplicate = componentRepository.findByClassIdAndName(updated.getClassId(), existing.getName());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new IllegalArgumentException("Компонент с таким именем уже существует в этом классе");
            }
            existing.setClassId(updated.getClassId());
        }

        if (updated.getName() != null && !updated.getName().equals(existing.getName())) {
            // Проверяем уникальность нового имени в текущем классе
            Optional<ComponentEntity> duplicate = componentRepository.findByClassIdAndName(existing.getClassId(), updated.getName());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new IllegalArgumentException("Компонент с таким именем уже существует в этом классе");
            }
            existing.setName(updated.getName());
        }
        if (updated.getVendorCode() != null) {
            existing.setVendorCode(updated.getVendorCode());
        }
        if (updated.getUnitId() != null) {
            unitOfMeasureRepository.findById(updated.getUnitId())
                    .orElseThrow(() -> new IllegalArgumentException("Единица измерения не найдена"));
            existing.setUnitId(updated.getUnitId());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getTechnicalSpecs() != null) {
            existing.setTechnicalSpecs(updated.getTechnicalSpecs());
        }
        if (updated.getWeightKg() != null) {
            existing.setWeightKg(updated.getWeightKg());
        }
        if (updated.getMaterial() != null) {
            existing.setMaterial(updated.getMaterial());
        }

        return componentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteComponent(Long id) {
        // Проверяем, используется ли компонент в продукции
        if (isComponentUsedInProducts(id)) {
            throw new IllegalStateException("Невозможно удалить компонент: он используется в карточках продукции. Сначала удалите связи.");
        }
        componentRepository.deleteById(id);
    }

    @Override
    public List<ComponentEntity> searchComponents(String query) {
        return componentRepository.findByNameContainingIgnoreCase(query);
    }

    @Override
    public List<ComponentEntity> getComponentsByClassAndName(Long classId, String search) {
        return componentRepository.searchByClassAndName(classId, search);
    }

    // ========== Unit of Measure ==========

    @Override
    public List<UnitOfMeasureEntity> getAllUnits() {
        return unitOfMeasureRepository.findAllByOrderByCodeAsc();
    }

    @Override
    public Optional<UnitOfMeasureEntity> getUnitById(Long id) {
        return unitOfMeasureRepository.findById(id);
    }

    @Override
    public Optional<UnitOfMeasureEntity> getUnitByCode(String code) {
        return unitOfMeasureRepository.findByCode(code);
    }

    @Override
    public UnitOfMeasureEntity getDefaultUnit() {
        return unitOfMeasureRepository.findByIsDefaultTrue()
                .orElseThrow(() -> new IllegalStateException("Единица измерения по умолчанию не найдена"));
    }

    // ========== Product Component ==========

    @Override
    public List<ProductComponentEntity> getComponentsByProductCard(Long productCardId) {
        return productComponentRepository.findByProductCardId(productCardId);
    }

    @Override
    @Transactional
    public ProductComponentEntity addComponentToProduct(Long productCardId, Long componentId, Double quantity, String position, String note) {
        // Проверяем, не существует ли уже связь
        Optional<ProductComponentEntity> existing = productComponentRepository
                .findByProductCardIdAndComponentId(productCardId, componentId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Компонент уже добавлен к этому изделию. Используйте обновление количества.");
        }

        ProductComponentEntity entity = new ProductComponentEntity();
        entity.setProductCardId(productCardId);
        entity.setComponentId(componentId);
        entity.setQuantity(quantity != null ? quantity : 1.0);
        entity.setPosition(position);
        entity.setNote(note);
        return productComponentRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateComponentQuantity(Long productCardId, Long componentId, Double quantity) {
        ProductComponentEntity entity = productComponentRepository
                .findByProductCardIdAndComponentId(productCardId, componentId)
                .orElseThrow(() -> new IllegalArgumentException("Связь не найдена"));
        entity.setQuantity(quantity);
        productComponentRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateComponentPosition(Long productCardId, Long componentId, String position) {
        ProductComponentEntity entity = productComponentRepository
                .findByProductCardIdAndComponentId(productCardId, componentId)
                .orElseThrow(() -> new IllegalArgumentException("Связь не найдена"));
        entity.setPosition(position);
        productComponentRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateComponentNote(Long productCardId, Long componentId, String note) {
        ProductComponentEntity entity = productComponentRepository
                .findByProductCardIdAndComponentId(productCardId, componentId)
                .orElseThrow(() -> new IllegalArgumentException("Связь не найдена"));
        entity.setNote(note);
        productComponentRepository.save(entity);
    }

    @Override
    @Transactional
    public void removeComponentFromProduct(Long productCardId, Long componentId) {
        productComponentRepository.deleteByProductCardIdAndComponentId(productCardId, componentId);
    }

    @Override
    @Transactional
    public void removeAllComponentsFromProduct(Long productCardId) {
        productComponentRepository.deleteByProductCardId(productCardId);
    }

    @Override
    @Transactional
    public ComponentCategoryEntity updateCategory(Long id, String name, Long parentId, String description) {
        ComponentCategoryEntity entity = componentCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        if (name != null && !name.isEmpty()) {
            entity.setName(name);
        }
        // Обновляем родителя
        if (parentId == null) {
            // Перемещаем в корневую категорию
            entity.setParentId(null);
            entity.setLevel(1);
            entity.setPath("/" + entity.getName() + "/");
        } else {
            ComponentCategoryEntity parent = componentCategoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена"));
            entity.setParentId(parentId);
            entity.setLevel(parent.getLevel() + 1);
            entity.setPath(parent.getPath() + entity.getName() + "/");
        }
        if (description != null) {
            entity.setDescription(description);
        }
        return componentCategoryRepository.save(entity);
    }

    @Override
    @Transactional
    public ComponentClassEntity updateClass(Long id, String name, Long categoryId, String description) {
        ComponentClassEntity entity = componentClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Класс не найден"));

        if (name != null && !name.isEmpty()) {
            entity.setName(name);
        }
        if (categoryId != null) {
            entity.setCategoryId(categoryId);
        }
        if (description != null) {
            entity.setDescription(description);
        }
        return componentClassRepository.save(entity);
    }

    @Override
    public boolean hasChildrenCategories(Long id) {
        return componentCategoryRepository.countByParentId(id) > 0;
    }

    @Override
    public boolean hasClassesInCategory(Long id) {
        return componentClassRepository.countByCategoryId(id) > 0;
    }

    @Override
    public boolean hasComponentsInClass(Long id) {
        return componentRepository.countByClassId(id) > 0;
    }

    @Override
    public boolean isClassUsedInProducts(Long id) {
        // Находим все компоненты этого класса
        List<ComponentEntity> components = componentRepository.findByClassId(id);
        for (ComponentEntity component : components) {
            if (isComponentUsedInProducts(component.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isComponentUsedInProducts(Long id) {
        return !productComponentRepository.findByComponentId(id).isEmpty();
    }

}
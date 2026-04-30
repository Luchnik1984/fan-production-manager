package com.fanproduction.services.impl;

import com.fanproduction.core.entity.ComponentClassEntity;
import com.fanproduction.core.entity.ComponentEntity;
import com.fanproduction.core.entity.ProductComponentEntity;
import com.fanproduction.core.entity.UnitOfMeasureEntity;
import com.fanproduction.repositories.ComponentClassRepository;
import com.fanproduction.repositories.ComponentRepository;
import com.fanproduction.repositories.ProductComponentRepository;
import com.fanproduction.repositories.UnitOfMeasureRepository;
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

    // ========== Component Class ==========

    @Override
    public List<ComponentClassEntity> getAllComponentClasses() {
        return componentClassRepository.findAllByOrderByNameAsc();
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
    public ComponentClassEntity createComponentClass(String name, String description, String createdBy) {
        if (componentClassRepository.existsByName(name)) {
            throw new IllegalArgumentException("Класс компонента с таким именем уже существует: " + name);
        }

        ComponentClassEntity entity = new ComponentClassEntity();
        entity.setName(name);
        entity.setDescription(description);
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
        if (isComponentClassInUse(id)) {
            throw new IllegalStateException("Невозможно удалить класс, так как существуют компоненты этого класса");
        }
        componentClassRepository.deleteById(id);
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

        if (updated.getName() != null && !updated.getName().equals(existing.getName())) {
            // Проверяем уникальность нового имени
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
        if (updated.getQuantityPerUnit() != null) {
            existing.setQuantityPerUnit(updated.getQuantityPerUnit());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }

        return componentRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteComponent(Long id) {
        // Проверяем, не используется ли компонент в продукции
        List<ProductComponentEntity> usages = productComponentRepository.findByComponentId(id);
        if (!usages.isEmpty()) {
            throw new IllegalStateException("Невозможно удалить компонент, так как он используется в " + usages.size() + " карточках продукции");
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
    public ProductComponentEntity addComponentToProduct(Long productCardId, Long componentId, Double quantity, String note) {
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
    public void removeComponentFromProduct(Long productCardId, Long componentId) {
        productComponentRepository.deleteByProductCardIdAndComponentId(productCardId, componentId);
    }

    @Override
    @Transactional
    public void removeAllComponentsFromProduct(Long productCardId) {
        productComponentRepository.deleteByProductCardId(productCardId);
    }
}
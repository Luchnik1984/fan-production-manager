package com.fanproduction.services.impl;

import com.fanproduction.core.entity.material.MaterialCategoryEntity;
import com.fanproduction.core.entity.material.MaterialClassEntity;
import com.fanproduction.core.entity.material.MaterialEntity;
import com.fanproduction.core.entity.material.ProductMaterialRequirementEntity;
import com.fanproduction.core.entity.dictionary.UnitOfMeasureEntity;
import com.fanproduction.repositories.material.MaterialCategoryRepository;
import com.fanproduction.repositories.material.MaterialClassRepository;
import com.fanproduction.repositories.material.MaterialRepository;
import com.fanproduction.repositories.material.ProductMaterialRequirementRepository;
import com.fanproduction.repositories.dictionary.UnitOfMeasureRepository;
import com.fanproduction.services.MaterialService;
import com.fanproduction.services.base.BaseValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MaterialServiceImpl extends BaseValidationService implements MaterialService {

    private final MaterialCategoryRepository materialCategoryRepository;
    private final MaterialClassRepository materialClassRepository;
    private final MaterialRepository materialRepository;
    private final ProductMaterialRequirementRepository productMaterialRequirementRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    // ========== Material Category ==========

    @Override
    public List<MaterialCategoryEntity> getAllCategories() {
        return materialCategoryRepository.findAllByOrderByPathAsc();
    }

    @Override
    public List<MaterialCategoryEntity> getRootCategories() {
        return materialCategoryRepository.findByParentIdIsNullOrderBySortOrderAsc();
    }

    @Override
    public List<MaterialCategoryEntity> getChildCategories(Long parentId) {
        return materialCategoryRepository.findByParentIdOrderBySortOrderAsc(parentId);
    }

    @Override
    public Optional<MaterialCategoryEntity> getCategoryById(Long id) {
        return materialCategoryRepository.findById(id);
    }

    @Override
    public Optional<MaterialCategoryEntity> getCategoryByName(String name) {
        return materialCategoryRepository.findByName(name);
    }

    @Override
    @Transactional
    public MaterialCategoryEntity createCategory(String name, Long parentId, String createdBy) {
        // Определяем уровень и путь
        int level = 1;
        String path = "/" + name + "/";
        Integer sortOrder = 0;

        if (parentId != null) {
            MaterialCategoryEntity parent = materialCategoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена"));
            level = parent.getLevel() + 1;
            path = parent.getPath() + name + "/";
            sortOrder = materialCategoryRepository.findByParentIdOrderBySortOrderAsc(parentId).size();
        }

        MaterialCategoryEntity entity = new MaterialCategoryEntity();
        entity.setName(name);
        entity.setParentId(parentId);
        entity.setLevel(level);
        entity.setPath(path);
        entity.setSortOrder(sortOrder);
        entity.setCreatedBy(createdBy);

        return materialCategoryRepository.save(entity);
    }



    @Override
    @Transactional
    public void deleteCategory(Long id) {
        // Проверяем наличие дочерних категорий
        long childrenCount = materialCategoryRepository.countByParentId(id);
        if (childrenCount > 0) {
            throw new IllegalStateException("У категории есть дочерние категории. Удалите их сначала.");
        }
        // Проверяем наличие классов
        long classesCount = materialClassRepository.countByCategoryId(id);
        if (classesCount > 0) {
            throw new IllegalStateException("В категории есть классы. Удалите их сначала.");
        }
        materialCategoryRepository.deleteById(id);
    }

    @Override
    public boolean isCategoryInUse(Long id) {
        return materialClassRepository.countByCategoryId(id) > 0;
    }

    // ========== Material Class ==========

    @Override
    public List<MaterialClassEntity> getAllClasses() {
        return materialClassRepository.findAllByOrderByNameAsc();
    }

    @Override
    public List<MaterialClassEntity> getClassesByCategory(Long categoryId) {
        return materialClassRepository.findByCategoryId(categoryId);
    }

    @Override
    public Optional<MaterialClassEntity> getClassById(Long id) {
        return materialClassRepository.findById(id);
    }

    @Override
    public Optional<MaterialClassEntity> getClassByName(String name) {
        return materialClassRepository.findByName(name);
    }

    @Override
    @Transactional
    public MaterialClassEntity createClass(Long categoryId, String name, String description, String createdBy, Long unitId) {
        if (materialClassRepository.existsByName(name)) {
            throw new IllegalArgumentException("Класс с таким именем уже существует");
        }

        // Проверяем существование категории
        materialCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена: " + categoryId));

        MaterialClassEntity entity = new MaterialClassEntity();
        entity.setCategoryId(categoryId);
        entity.setName(name);
        entity.setDescription(description);
        entity.setUnitId(unitId);
        entity.setCreatedBy(createdBy);
        return materialClassRepository.save(entity);
    }

    @Override
    @Transactional
    public MaterialClassEntity updateClass(Long id, String name, String description, Long unitId) {
        MaterialClassEntity entity = materialClassRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Класс не найден"));

        if (name != null && !name.equals(entity.getName())) {
            if (materialClassRepository.existsByName(name)) {
                throw new IllegalArgumentException("Класс с таким именем уже существует");
            }
            entity.setName(name);
        }
        if (description != null) {
            entity.setDescription(description);
        }
        if (unitId != null) {
            unitOfMeasureRepository.findById(unitId)
                    .orElseThrow(() -> new IllegalArgumentException("Единица измерения не найдена"));
            entity.setUnitId(unitId);
        }

        return materialClassRepository.save(entity);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        // Проверяем наличие компонентов
        long componentsCount = materialRepository.countByClassId(id);
        if (componentsCount > 0) {
            throw new IllegalStateException("В классе есть компоненты. Удалите их сначала.");
        }
        materialClassRepository.deleteById(id);

    }

    @Override
    public boolean isClassInUse(Long id) {
        return materialRepository.countByClassId(id) > 0;
    }

    // ========== Material ==========

    @Override
    public List<MaterialEntity> getAllMaterials() {
        return materialRepository.findAll();
    }

    @Override
    public Page<MaterialEntity> getMaterialsByClass(Long classId, Pageable pageable) {
        return materialRepository.findByClassId(classId, pageable);
    }

    @Override
    public Optional<MaterialEntity> getMaterialById(Long id) {
        return materialRepository.findById(id);
    }

    @Override
    public Optional<MaterialEntity> getMaterialByClassAndName(Long classId, String name) {
        return materialRepository.findByClassIdAndName(classId, name);
    }

    @Override
    @Transactional
    public MaterialEntity createMaterial(MaterialEntity material) {
        // Проверка существования класса
        materialClassRepository.findById(material.getClassId())
                .orElseThrow(() -> new IllegalArgumentException("Класс материала не найден"));

        // Проверка единицы измерения
        unitOfMeasureRepository.findById(material.getUnitId())
                .orElseThrow(() -> new IllegalArgumentException("Единица измерения не найдена"));

        // Проверка уникальности
        checkUnique(() -> materialRepository.findByClassIdAndNameAndStandardAndSpecification(
                        material.getClassId(), material.getName(), material.getStandard(), material.getSpecification()),
                "Материал с такими параметрами уже существует");

        return materialRepository.save(material);
    }

    @Override
    @Transactional
    public MaterialEntity updateMaterial(Long id, MaterialEntity updated) {
        MaterialEntity existing = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Материал не найден"));

        // Проверка уникальности при изменении ключевых полей
        if (!existing.getClassId().equals(updated.getClassId()) ||
                !existing.getName().equals(updated.getName()) ||
                !Objects.equals(existing.getStandard(), updated.getStandard()) ||
                !Objects.equals(existing.getSpecification(), updated.getSpecification())) {

            checkUniqueOnUpdate(
                    () -> materialRepository.findByClassIdAndNameAndStandardAndSpecification(
                            updated.getClassId(),
                            updated.getName(),
                            updated.getStandard(),
                            updated.getSpecification()
                    ),
                    id,
                    "Материал с такими параметрами уже существует"
            );
        }

        // Обновление полей
        if (updated.getClassId() != null) {
            existing.setClassId(updated.getClassId());
        }
        if (updated.getName() != null) {
            existing.setName(updated.getName());
        }
        if (updated.getStandard() != null) {
            existing.setStandard(updated.getStandard());
        }
        if (updated.getSpecification() != null) {
            existing.setSpecification(updated.getSpecification());
        }
        if (updated.getMaterialType() != null) {
            existing.setMaterialType(updated.getMaterialType());
        }
        if (updated.getUnitId() != null) {
            existing.setUnitId(updated.getUnitId());
        }
        if (updated.getDensity() != null) {
            existing.setDensity(updated.getDensity());
        }
        if (updated.getVendorCode() != null) {
            existing.setVendorCode(updated.getVendorCode());
        }
        if (updated.getMinOrder() != null) {
            existing.setMinOrder(updated.getMinOrder());
        }
        if (updated.getDescription() != null) {
            existing.setDescription(updated.getDescription());
        }
        if (updated.getTechnicalSpecs() != null) {
            existing.setTechnicalSpecs(updated.getTechnicalSpecs());
        }

        return materialRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteMaterial(Long id) {
        // Проверяем, используется ли материал в продукции
        if (isMaterialUsedInProducts(id)) {
            throw new IllegalStateException("Невозможно удалить материал: он используется в карточках продукции. Сначала удалите связи.");
        }
        materialRepository.deleteById(id);
    }


    @Override
    public List<MaterialEntity> searchMaterials(String query) {
        return materialRepository.findByNameContainingIgnoreCase(query);
    }

    @Override
    public List<MaterialEntity> getMaterialsByClassAndName(Long classId, String search) {
        return materialRepository.searchByClassAndName(classId, search);
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

    // ========== Product Material Requirement ==========

    @Override
    public List<ProductMaterialRequirementEntity> getMaterialsByProductCard(Long productCardId) {
        return productMaterialRequirementRepository.findByProductCardId(productCardId);
    }

    @Override
    @Transactional
    public ProductMaterialRequirementEntity addMaterialToProduct(Long productCardId, Long materialId, Double quantityPerUnit, String note) {
        Optional<ProductMaterialRequirementEntity> existing = productMaterialRequirementRepository
                .findByProductCardIdAndMaterialId(productCardId, materialId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Материал уже добавлен к этому изделию");
        }

        ProductMaterialRequirementEntity entity = new ProductMaterialRequirementEntity();
        entity.setProductCardId(productCardId);
        entity.setMaterialId(materialId);
        entity.setQuantityPerUnit(quantityPerUnit != null ? quantityPerUnit : 1.0);
        entity.setNote(note);
        return productMaterialRequirementRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateMaterialQuantity(Long productCardId, Long materialId, Double quantityPerUnit) {
        ProductMaterialRequirementEntity entity = productMaterialRequirementRepository
                .findByProductCardIdAndMaterialId(productCardId, materialId)
                .orElseThrow(() -> new IllegalArgumentException("Связь не найдена"));
        entity.setQuantityPerUnit(quantityPerUnit);
        productMaterialRequirementRepository.save(entity);
    }

    @Override
    @Transactional
    public void updateMaterialNote(Long productCardId, Long materialId, String note) {
        ProductMaterialRequirementEntity entity = productMaterialRequirementRepository
                .findByProductCardIdAndMaterialId(productCardId, materialId)
                .orElseThrow(() -> new IllegalArgumentException("Связь не найдена"));
        entity.setNote(note);
        productMaterialRequirementRepository.save(entity);
    }

    @Override
    @Transactional
    public void removeMaterialFromProduct(Long productCardId, Long materialId) {
        productMaterialRequirementRepository.deleteByProductCardIdAndMaterialId(productCardId, materialId);
    }

    @Override
    @Transactional
    public void removeAllMaterialsFromProduct(Long productCardId) {
        productMaterialRequirementRepository.deleteByProductCardId(productCardId);
    }

    @Override
    @Transactional
    public MaterialCategoryEntity updateCategory(Long id, String name, Long parentId, String description) {
        MaterialCategoryEntity entity = materialCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория не найдена"));

        if (name != null && !name.isEmpty()) {
            entity.setName(name);
        }
        if (description != null) {
            entity.setDescription(description);
        }

        // Обновляем родителя
        if (parentId == null) {
            // Перемещаем в корневую категорию
            entity.setParentId(null);
            entity.setLevel(1);
            entity.setPath("/" + entity.getName() + "/");
        } else {
            MaterialCategoryEntity parent = materialCategoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("Родительская категория не найдена"));
            entity.setParentId(parentId);
            entity.setLevel(parent.getLevel() + 1);
            entity.setPath(parent.getPath() + entity.getName() + "/");
        }

        return materialCategoryRepository.save(entity);
    }

    @Override
    @Transactional
    public MaterialClassEntity updateClass(Long id, String name, Long categoryId, String description) {
        MaterialClassEntity entity = materialClassRepository.findById(id)
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
        return materialClassRepository.save(entity);

    }

    @Override
    public boolean hasChildrenCategories(Long categoryId) {
        List<MaterialCategoryEntity> children = materialCategoryRepository.findByParentIdOrderBySortOrderAsc(categoryId);
        return !children.isEmpty();
    }

    @Override
    public boolean hasClassesInCategory(Long categoryId) {
        List<MaterialClassEntity> classes = materialClassRepository.findByCategoryId(categoryId);
        return !classes.isEmpty();
    }

    @Override
    public boolean hasMaterialsInClass(Long classId) {
        List<MaterialEntity> components = materialRepository.findByClassId(classId);
        return !components.isEmpty();
    }

    @Override
    public boolean isClassUsedInProducts(Long classId) {
        // Находим все материалы этого класса
        List<MaterialEntity> materials = materialRepository.findByClassId(classId);
        for (MaterialEntity material : materials) {
            if (isMaterialUsedInProducts(material.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isMaterialUsedInProducts(Long id) {
        return !productMaterialRequirementRepository.findByMaterialId(id).isEmpty();
    }
}


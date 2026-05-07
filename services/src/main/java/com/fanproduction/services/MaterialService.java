package com.fanproduction.services;

import com.fanproduction.core.entity.material.MaterialCategoryEntity;
import com.fanproduction.core.entity.material.MaterialClassEntity;
import com.fanproduction.core.entity.material.MaterialEntity;
import com.fanproduction.core.entity.material.ProductMaterialRequirementEntity;
import com.fanproduction.core.entity.dictionary.UnitOfMeasureEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MaterialService {

    // ========== Material Category ==========
    List<MaterialCategoryEntity> getAllCategories();
    List<MaterialCategoryEntity> getRootCategories();
    List<MaterialCategoryEntity> getChildCategories(Long parentId);
    Optional<MaterialCategoryEntity> getCategoryById(Long id);
    Optional<MaterialCategoryEntity> getCategoryByName(String name);
    MaterialCategoryEntity createCategory(String name, Long parentId, String createdBy);
    MaterialCategoryEntity updateCategory(Long id, String name, Integer sortOrder);
    void deleteCategory(Long id);
    boolean isCategoryInUse(Long id);

    // ========== Material Class ==========
    List<MaterialClassEntity> getAllClasses();
    List<MaterialClassEntity> getClassesByCategory(Long categoryId);
    Optional<MaterialClassEntity> getClassById(Long id);
    Optional<MaterialClassEntity> getClassByName(String name);
    MaterialClassEntity createClass(Long categoryId, String name, String description, Long unitId, String createdBy);
    MaterialClassEntity updateClass(Long id, String name, String description, Long unitId);
    void deleteClass(Long id);
    boolean isClassInUse(Long id);

    // ========== Material ==========
    List<MaterialEntity> getAllMaterials();
    Page<MaterialEntity> getMaterialsByClass(Long classId, Pageable pageable);
    Optional<MaterialEntity> getMaterialById(Long id);
    Optional<MaterialEntity> getMaterialByClassAndName(Long classId, String name);
    MaterialEntity createMaterial(MaterialEntity material);
    MaterialEntity updateMaterial(Long id, MaterialEntity material);
    void deleteMaterial(Long id);
    List<MaterialEntity> searchMaterials(String query);
    List<MaterialEntity> getMaterialsByClassAndName(Long classId, String search);

    // ========== Unit of Measure ==========
    List<UnitOfMeasureEntity> getAllUnits();
    Optional<UnitOfMeasureEntity> getUnitById(Long id);
    Optional<UnitOfMeasureEntity> getUnitByCode(String code);

    // ========== Product Material Requirement ==========
    List<ProductMaterialRequirementEntity> getMaterialsByProductCard(Long productCardId);
    ProductMaterialRequirementEntity addMaterialToProduct(Long productCardId, Long materialId, Double quantityPerUnit, String note);
    void updateMaterialQuantity(Long productCardId, Long materialId, Double quantityPerUnit);
    void removeMaterialFromProduct(Long productCardId, Long materialId);
    void removeAllMaterialsFromProduct(Long productCardId);
    void updateMaterialNote(Long productCardId, Long materialId, String note);

}

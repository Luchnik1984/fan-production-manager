package com.fanproduction.api.controller;

import com.fanproduction.api.dto.response.*;
import com.fanproduction.core.entity.material.MaterialCategoryEntity;
import com.fanproduction.core.entity.material.MaterialClassEntity;
import com.fanproduction.core.entity.material.MaterialEntity;
import com.fanproduction.core.entity.material.ProductMaterialRequirementEntity;
import com.fanproduction.core.entity.dictionary.UnitOfMeasureEntity;
import com.fanproduction.services.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    private String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "system";
    }

    // ========== Unit of Measure ==========

    @GetMapping("/units")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<UnitOfMeasureDto>> getAllUnits() {
        List<UnitOfMeasureDto> units = materialService.getAllUnits().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ApiResponse.success(units);
    }

    // ========== Categories ==========

    @GetMapping("/categories")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MaterialCategoryDto>> getAllCategories() {
        List<MaterialCategoryDto> categories = materialService.getAllCategories().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
        return ApiResponse.success(categories);
    }

    @GetMapping("/categories/root")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MaterialCategoryDto>> getRootCategories() {
        List<MaterialCategoryDto> categories = materialService.getRootCategories().stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
        return ApiResponse.success(categories);
    }

    @GetMapping("/categories/{id}/children")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MaterialCategoryDto>> getChildCategories(@PathVariable Long id) {
        List<MaterialCategoryDto> children = materialService.getChildCategories(id).stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
        return ApiResponse.success(children);
    }

    @PostMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<MaterialCategoryDto> createCategory(@RequestBody Map<String, Object> request) {
        String name = (String) request.get("name");
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;

        if (name == null || name.trim().isEmpty()) {
            return ApiResponse.error("Название категории обязательно");
        }
            MaterialCategoryEntity entity = materialService.createCategory(name.trim(), parentId, getCurrentUser());
            return ApiResponse.success(toCategoryDto(entity));
    }

    @DeleteMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteCategory(@PathVariable Long id) {
            materialService.deleteCategory(id);
            return ApiResponse.success(null);
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<MaterialCategoryDto> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
            String name = (String) request.get("name");
            Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
            String description = (String) request.get("description");

            MaterialCategoryEntity entity = materialService.updateCategory(id, name, parentId, description);
            return ApiResponse.success(toCategoryDto(entity));
    }

    // ========== Classes ==========

    @GetMapping("/classes")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MaterialClassDto>> getAllClasses(
            @RequestParam(required = false) Long categoryId) {
        List<MaterialClassEntity> classes;
        if (categoryId != null) {
            classes = materialService.getClassesByCategory(categoryId);
        } else {
            classes = materialService.getAllClasses();
        }

        List<MaterialClassDto> dtos = classes.stream()
                .map(this::toClassDto)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @PostMapping("/classes")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<MaterialClassDto> createClass(@RequestBody Map<String, Object> request) {
        Long categoryId = ((Number) request.get("categoryId")).longValue();
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        Long unitId = request.get("unitId") != null ? ((Number) request.get("unitId")).longValue() : null;

        MaterialClassEntity entity = materialService.createClass(categoryId, name, description, getCurrentUser(), unitId);
        return ApiResponse.success(toClassDto(entity));
    }

    @DeleteMapping("/classes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteClass(@PathVariable Long id) {
            materialService.deleteClass(id);
            return ApiResponse.success(null);
    }

    @PutMapping("/classes/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<MaterialClassDto> updateClass(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
            String name = (String) request.get("name");
            Long categoryId = ((Number) request.get("categoryId")).longValue();
            String description = (String) request.get("description");

            MaterialClassEntity entity = materialService.updateClass(id, name, categoryId, description);
            return ApiResponse.success(toClassDto(entity));
    }

    // ========== Materials ==========

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MaterialDto>> getAllMaterials(
            @RequestParam(required = false) Long classId) {
        List<MaterialEntity> materials;
        if (classId != null) {
            materials = materialService.getMaterialsByClass(classId, null).getContent();
        } else {
            materials = materialService.getAllMaterials();
        }

        List<MaterialDto> dtos = materials.stream()
                .map(this::toMaterialDto)
                .collect(Collectors.toList());
        return ApiResponse.success(dtos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<MaterialDto> getMaterial(@PathVariable Long id) {
        return materialService.getMaterialById(id)
                .map(this::toMaterialDto)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("Материал не найден"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<MaterialDto> createMaterial(@RequestBody MaterialDto dto) {
        try {
            MaterialEntity entity = new MaterialEntity();
            entity.setClassId(dto.getClassId());
            entity.setName(dto.getName());
            entity.setStandard(dto.getStandard());
            entity.setSpecification(dto.getSpecification());
            entity.setMaterialType(dto.getMaterialType());
            entity.setUnitId(dto.getUnitId());
            entity.setDensity(dto.getDensity());
            entity.setVendorCode(dto.getVendorCode());
            entity.setMinOrder(dto.getMinOrder());
            entity.setDescription(dto.getDescription());
            entity.setTechnicalSpecs(dto.getTechnicalSpecs());
            entity.setCreatedBy(getCurrentUser());

            MaterialEntity saved = materialService.createMaterial(entity);
            return ApiResponse.success(toMaterialDto(saved));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<MaterialDto> updateMaterial(@PathVariable Long id, @RequestBody MaterialDto dto) {

            MaterialEntity entity = new MaterialEntity();
            entity.setClassId(dto.getClassId());
            entity.setName(dto.getName());
            entity.setStandard(dto.getStandard());
            entity.setSpecification(dto.getSpecification());
            entity.setMaterialType(dto.getMaterialType());
            entity.setUnitId(dto.getUnitId());
            entity.setDensity(dto.getDensity());
            entity.setVendorCode(dto.getVendorCode());
            entity.setMinOrder(dto.getMinOrder());
            entity.setDescription(dto.getDescription());
            entity.setTechnicalSpecs(dto.getTechnicalSpecs());

            MaterialEntity updated = materialService.updateMaterial(id, entity);
            return ApiResponse.success(toMaterialDto(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> deleteMaterial(@PathVariable Long id) {
            materialService.deleteMaterial(id);
            return ApiResponse.success("Материал удалён", null);
    }

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<MaterialDto>> searchMaterials(@RequestParam String query) {
        List<MaterialDto> results = materialService.searchMaterials(query).stream()
                .map(this::toMaterialDto)
                .collect(Collectors.toList());
        return ApiResponse.success(results);
    }

    // ========== Product Material Requirements ==========

    @GetMapping("/product/{productCardId}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProductMaterialRequirementDto>> getProductMaterials(@PathVariable Long productCardId) {
        List<ProductMaterialRequirementDto> result = materialService.getMaterialsByProductCard(productCardId).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
        return ApiResponse.success(result);
    }

    @PostMapping("/product/{productCardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductMaterialRequirementDto> addMaterialToProduct(
            @PathVariable Long productCardId,
            @RequestBody Map<String, Object> request) {
            Long materialId = ((Number) request.get("materialId")).longValue();
            Double quantity = request.get("quantityPerUnit") != null ? ((Number) request.get("quantityPerUnit")).doubleValue() : 1.0;
            String note = (String) request.get("note");

            ProductMaterialRequirementEntity entity = materialService.addMaterialToProduct(
                    productCardId, materialId, quantity, note);
            return ApiResponse.success(toProductDto(entity));
    }

    @PutMapping("/product/{productCardId}/{materialId}/quantity")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> updateMaterialQuantity(
            @PathVariable Long productCardId,
            @PathVariable Long materialId,
            @RequestBody Map<String, Double> request) {
            Double quantity = request.get("quantityPerUnit");
            if (quantity == null) {
                return ApiResponse.error("Количество не указано");
            }
            materialService.updateMaterialQuantity(productCardId, materialId, quantity);
            return ApiResponse.success("Количество обновлено", null);
    }

    @PutMapping("/product/{productCardId}/{materialId}/note")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> updateMaterialNote(
            @PathVariable Long productCardId,
            @PathVariable Long materialId,
            @RequestBody Map<String, String> request) {
            String note = request.get("note");
            materialService.updateMaterialNote(productCardId, materialId, note);
            return ApiResponse.success("Примечание обновлено", null);
    }

    @DeleteMapping("/product/{productCardId}/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> removeMaterialFromProduct(
            @PathVariable Long productCardId,
            @PathVariable Long materialId) {
            materialService.removeMaterialFromProduct(productCardId, materialId);
            return ApiResponse.success("Материал удалён", null);
    }

    // ========== Mappers ==========

    private UnitOfMeasureDto toDto(UnitOfMeasureEntity entity) {
        return UnitOfMeasureDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .symbol(entity.getSymbol())
                .category(entity.getCategory())
                .isDefault(entity.getIsDefault())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private MaterialCategoryDto toCategoryDto(MaterialCategoryEntity entity) {
        return MaterialCategoryDto.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .level(entity.getLevel())
                .path(entity.getPath())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy())
                .build();
    }

    private MaterialClassDto toClassDto(MaterialClassEntity entity) {
        MaterialClassDto.MaterialClassDtoBuilder builder = MaterialClassDto.builder()
                .id(entity.getId())
                .categoryId(entity.getCategoryId())
                .name(entity.getName())
                .description(entity.getDescription())
                .unitId(entity.getUnitId())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy());

        materialService.getCategoryById(entity.getCategoryId())
                .ifPresent(cat -> builder.categoryName(cat.getName()));
        if (entity.getUnitId() != null) {
            materialService.getUnitById(entity.getUnitId())
                    .ifPresent(unit -> {
                        builder.unitCode(unit.getCode());
                        builder.unitName(unit.getName());
                    });
        }

        return builder.build();
    }

    private MaterialDto toMaterialDto(MaterialEntity entity) {
        MaterialDto.MaterialDtoBuilder builder = MaterialDto.builder()
                .id(entity.getId())
                .classId(entity.getClassId())
                .name(entity.getName())
                .standard(entity.getStandard())
                .specification(entity.getSpecification())
                .materialType(entity.getMaterialType())
                .unitId(entity.getUnitId())
                .density(entity.getDensity())
                .vendorCode(entity.getVendorCode())
                .minOrder(entity.getMinOrder())
                .description(entity.getDescription())
                .technicalSpecs(entity.getTechnicalSpecs())
                .createdAt(entity.getCreatedAt())
                .createdBy(entity.getCreatedBy());

        materialService.getClassById(entity.getClassId())
                .ifPresent(cls -> builder.className(cls.getName()));
        materialService.getUnitById(entity.getUnitId())
                .ifPresent(unit -> {
                    builder.unitCode(unit.getCode());
                    builder.unitName(unit.getName());
                });

        return builder.build();
    }

    private ProductMaterialRequirementDto toProductDto(ProductMaterialRequirementEntity entity) {
        ProductMaterialRequirementDto.ProductMaterialRequirementDtoBuilder builder = ProductMaterialRequirementDto.builder()
                .id(entity.getId())
                .productCardId(entity.getProductCardId())
                .materialId(entity.getMaterialId())
                .quantityPerUnit(entity.getQuantityPerUnit())
                .note(entity.getNote());

        materialService.getMaterialById(entity.getMaterialId()).ifPresent(mat -> {
            builder.materialName(mat.getName());
            materialService.getClassById(mat.getClassId())
                    .ifPresent(cls -> builder.materialClass(cls.getName()));
            materialService.getUnitById(mat.getUnitId())
                    .ifPresent(unit -> builder.unitCode(unit.getCode()));
        });

        return builder.build();
    }
}

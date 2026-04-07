package com.fanproduction.api.controller;

import com.fanproduction.api.dto.ApiResponse;
import com.fanproduction.core.entity.ProductMaterialRequirementEntity;
import com.fanproduction.repositories.ProductMaterialRequirementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products/{productId}/materials")
@RequiredArgsConstructor
public class ProductMaterialController {

    private final ProductMaterialRequirementRepository materialRequirementRepository;

    /**
     * Получить все материалы для карточки продукции
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProductMaterialRequirementEntity>> getMaterials(@PathVariable Long productId) {
        List<ProductMaterialRequirementEntity> materials =
                materialRequirementRepository.findByProductCardId(productId);
        return ApiResponse.success(materials);
    }

    /**
     * Добавить материал к карточке продукции
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<ProductMaterialRequirementEntity> addMaterial(
            @PathVariable Long productId,
            @RequestBody ProductMaterialRequirementEntity requirement) {

        requirement.setProductCardId(productId);
        ProductMaterialRequirementEntity saved = materialRequirementRepository.save(requirement);
        return ApiResponse.success(saved);
    }

    /**
     * Удалить материал из карточки продукции
     */
    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENGINEER')")
    public ApiResponse<Void> removeMaterial(
            @PathVariable Long productId,
            @PathVariable Long materialId) {

        materialRequirementRepository.deleteByProductCardIdAndMaterialId(productId, materialId);
        return ApiResponse.success("Материал удалён", null);
    }
}

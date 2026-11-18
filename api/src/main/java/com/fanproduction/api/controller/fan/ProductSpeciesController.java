package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateProductSpeciesRequest;
import com.fanproduction.api.dto.request.fan.UpdateProductSpeciesRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.ProductSpeciesDto;
import com.fanproduction.template.service.ProductHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления видами вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/product-species")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductSpeciesController extends BaseController {

    private final ProductHierarchyService productHierarchyService;

    /**
     * Виды по типу.
     * <p>
     * {@code typeId} — обязательный query-параметр.
     */
    @GetMapping
    public ApiResponse<List<ProductSpeciesDto>> getSpeciesByType(@RequestParam Long typeId) {
        return ApiResponse.success(productHierarchyService.getSpeciesByType(typeId));
    }

    /**
     * Вид по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductSpeciesDto> getSpeciesById(@PathVariable Long id) {
        return ApiResponse.success(productHierarchyService.getSpeciesById(id));
    }

    /**
     * Создать вид.
     */
    @PostMapping
    public ApiResponse<ProductSpeciesDto> createSpecies(
            @Valid @RequestBody CreateProductSpeciesRequest request) {
        ProductSpeciesDto created = productHierarchyService.createSpecies(
                request.typeId(),
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Вид создан", created);
    }

    /**
     * Обновить вид.
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductSpeciesDto> updateSpecies(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductSpeciesRequest request) {
        ProductSpeciesDto updated = productHierarchyService.updateSpecies(
                id,
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Вид обновлён", updated);
    }

    /**
     * Удалить вид.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSpecies(@PathVariable Long id) {
        productHierarchyService.deleteSpecies(id, getCurrentUser());
        return ApiResponse.success("Вид удалён", null);
    }
}

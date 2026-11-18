package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateProductSeriesRequest;
import com.fanproduction.api.dto.request.fan.UpdateProductSeriesRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.ProductSeriesDto;
import com.fanproduction.template.service.ProductHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления сериями вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/product-series")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductSeriesController extends BaseController {

    private final ProductHierarchyService productHierarchyService;

    /**
     * Серии по виду.
     * <p>
     * {@code speciesId} — обязательный query-параметр.
     */
    @GetMapping
    public ApiResponse<List<ProductSeriesDto>> getSeriesBySpecies(@RequestParam Long speciesId) {
        return ApiResponse.success(productHierarchyService.getSeriesBySpecies(speciesId));
    }

    /**
     * Серия по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductSeriesDto> getSeriesById(@PathVariable Long id) {
        return ApiResponse.success(productHierarchyService.getSeriesById(id));
    }

    /**
     * Создать серию.
     */
    @PostMapping
    public ApiResponse<ProductSeriesDto> createSeries(
            @Valid @RequestBody CreateProductSeriesRequest request) {
        ProductSeriesDto created = productHierarchyService.createSeries(
                request.speciesId(),
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Серия создана", created);
    }

    /**
     * Обновить серию.
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductSeriesDto> updateSeries(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductSeriesRequest request) {
        ProductSeriesDto updated = productHierarchyService.updateSeries(
                id,
                request.name(),
                getCurrentUser()
        );
        return ApiResponse.success("Серия обновлена", updated);
    }

    /**
     * Удалить серию.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteSeries(@PathVariable Long id) {
        productHierarchyService.deleteSeries(id, getCurrentUser());
        return ApiResponse.success("Серия удалена", null);
    }
}

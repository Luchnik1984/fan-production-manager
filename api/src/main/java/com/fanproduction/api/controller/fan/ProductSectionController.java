package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateProductSectionRequest;
import com.fanproduction.api.dto.request.fan.MoveProductSectionRequest;
import com.fanproduction.api.dto.request.fan.UpdateProductSectionRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.ProductSectionDto;
import com.fanproduction.template.service.ProductHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер разделов продукции (верхний уровень иерархии).
 * <p>
 * Чтение доступно всем аутентифицированным пользователям — дерево разделов
 * используется в конструкторе шаблонов. Изменения — только ADMIN.
 */
@RestController
@RequestMapping("/api/product-sections")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProductSectionController extends BaseController {

    private final ProductHierarchyService productHierarchyService;

    /**
     * Дерево разделов вместе с типами продукции.
     */
    @GetMapping("/tree")
    public ApiResponse<List<ProductSectionDto>> getTree() {
        return ApiResponse.success(productHierarchyService.getSectionTree());
    }

    /**
     * Плоский список разделов — для выпадающих списков.
     */
    @GetMapping
    public ApiResponse<List<ProductSectionDto>> getAll() {
        return ApiResponse.success(productHierarchyService.getAllSections());
    }

    /**
     * Раздел по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductSectionDto> getById(@PathVariable Long id) {
        return ApiResponse.success(productHierarchyService.getSectionById(id));
    }

    /**
     * Создать раздел.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductSectionDto> create(@Valid @RequestBody CreateProductSectionRequest request) {
        ProductSectionDto created = productHierarchyService.createSection(
                request.parentId(),
                request.name(),
                request.sortOrder(),
                getCurrentUser()
        );
        return ApiResponse.success("Раздел создан", created);
    }

    /**
     * Обновить раздел.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductSectionDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductSectionRequest request) {
        ProductSectionDto updated = productHierarchyService.updateSection(
                id,
                request.name(),
                request.sortOrder(),
                getCurrentUser()
        );
        return ApiResponse.success("Раздел обновлён", updated);
    }

    /**
     * Переместить раздел (drag-and-drop в дереве).
     * <p>
     * Перемещение внутрь собственного подраздела запрещено — это создало бы цикл.
     */
    @PutMapping("/{id}/move")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> move(
            @PathVariable Long id,
            @RequestBody MoveProductSectionRequest request) {
        productHierarchyService.moveSection(id, request.parentId(), getCurrentUser());
        return ApiResponse.success("Раздел перемещён", null);
    }

    /**
     * Удалить раздел.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        productHierarchyService.deleteSection(id, getCurrentUser());
        return ApiResponse.success("Раздел удалён", null);
    }
}

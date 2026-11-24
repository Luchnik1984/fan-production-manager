package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateProductTypeRequest;
import com.fanproduction.api.dto.request.fan.UpdateProductTypeRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.ProductTypeDto;
import com.fanproduction.template.dto.ProductTypeShortDto;
import com.fanproduction.template.service.ProductHierarchyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления типами вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/product-types")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductTypeController extends BaseController {

    private final ProductHierarchyService productHierarchyService;

    /**
     * Все типы (без вложенных видов и серий).
     */
    @GetMapping
    public ApiResponse<List<ProductTypeDto>> getAllTypes() {
        return ApiResponse.success(productHierarchyService.getAllTypes());
    }

    /**
     * Дерево: Тип → Вид → Серия.
     */
    @GetMapping("/tree")
    public ApiResponse<List<ProductTypeDto>> getTree() {
        return ApiResponse.success(productHierarchyService.getTree());
    }

    /**
     * Тип по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductTypeDto> getTypeById(@PathVariable Long id) {
        return ApiResponse.success(productHierarchyService.getTypeById(id));
    }

    /**
     * Краткий список типов для ComboBox (без вложенных видов).
     * <p>
     * Доступен всем аутентифицированным пользователям — используется
     * в ComboBox фильтрации каталога.
     */
    @GetMapping("/short-list")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<ProductTypeShortDto>> getShortList() {
        return ApiResponse.success(productHierarchyService.getShortList());
    }

    /**
     * Создать тип.
     */
    @PostMapping
    public ApiResponse<ProductTypeDto> createType(@Valid @RequestBody CreateProductTypeRequest request) {
        ProductTypeDto created = productHierarchyService.createType(
                request.name(),
                request.designation(),
                request.sectionId(),
                getCurrentUser()
        );
        return ApiResponse.success("Тип создан", created);
    }

    /**
     * Обновить тип.
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductTypeDto> updateType(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductTypeRequest request) {
        ProductTypeDto updated = productHierarchyService.updateType(
                id,
                request.name(),
                request.designation(),
                getCurrentUser()
        );
        return ApiResponse.success("Тип обновлён", updated);
    }

    /**
     * Переместить тип в другой раздел (drag-and-drop в дереве).
     * <p>
     * {@code sectionId} не задан — тип убирается из разделов.
     */
    @PutMapping("/{id}/section")
    public ApiResponse<ProductTypeDto> moveToSection(
            @PathVariable Long id,
            @RequestParam(required = false) Long sectionId) {
        ProductTypeDto moved = productHierarchyService.moveTypeToSection(id, sectionId, getCurrentUser());
        return ApiResponse.success("Тип перемещён", moved);
    }

    /**
     * Назначить типу роль продукции (US10.7.2).
     * <p>
     * Без {@code cardRoleId} роль снимается — тип перестаёт участвовать
     * в селекторах комплектации.
     */
    @PutMapping("/{id}/role")
    public ApiResponse<ProductTypeDto> setRole(
            @PathVariable Long id,
            @RequestParam(required = false) Long cardRoleId) {
        ProductTypeDto updated = productHierarchyService.setTypeRole(id, cardRoleId, getCurrentUser());
        return ApiResponse.success("Роль типа обновлена", updated);
    }

    /**
     * Удалить тип.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        productHierarchyService.deleteType(id, getCurrentUser());
        return ApiResponse.success("Тип удалён", null);
    }
}

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

    private final ProductHierarchyService fanHierarchyService;

    /**
     * Все типы (без вложенных видов и серий).
     */
    @GetMapping
    public ApiResponse<List<ProductTypeDto>> getAllTypes() {
        return ApiResponse.success(fanHierarchyService.getAllTypes());
    }

    /**
     * Дерево: Тип → Вид → Серия.
     */
    @GetMapping("/tree")
    public ApiResponse<List<ProductTypeDto>> getTree() {
        return ApiResponse.success(fanHierarchyService.getTree());
    }

    /**
     * Тип по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductTypeDto> getTypeById(@PathVariable Long id) {
        return ApiResponse.success(fanHierarchyService.getTypeById(id));
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
        return ApiResponse.success(fanHierarchyService.getShortList());
    }

    /**
     * Создать тип.
     */
    @PostMapping
    public ApiResponse<ProductTypeDto> createType(@Valid @RequestBody CreateProductTypeRequest request) {
        ProductTypeDto created = fanHierarchyService.createType(
                request.name(),
                request.designation(),
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
        ProductTypeDto updated = fanHierarchyService.updateType(
                id,
                request.name(),
                request.designation(),
                getCurrentUser()
        );
        return ApiResponse.success("Тип обновлён", updated);
    }

    /**
     * Удалить тип.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteType(@PathVariable Long id) {
        fanHierarchyService.deleteType(id, getCurrentUser());
        return ApiResponse.success("Тип удалён", null);
    }
}

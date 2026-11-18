package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CloneProductTemplateRequest;
import com.fanproduction.api.dto.request.fan.CreateProductTemplateRequest;
import com.fanproduction.api.dto.request.fan.CreateProductTemplateVersionRequest;
import com.fanproduction.api.dto.request.fan.UpdateProductTemplateRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.ProductTemplateDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.service.ProductTemplateService;
import com.fanproduction.template.service.TemplateCloneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления шаблонами вентиляторов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/product-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ProductTemplateController extends BaseController {

    private final ProductTemplateService productTemplateService;
    private final TemplateCloneService templateCloneService;

    // ==========================================================
    // TEMPLATE — CRUD
    // ==========================================================

    /**
     * Все шаблоны (без версий).
     */
    @GetMapping
    public ApiResponse<List<ProductTemplateDto>> getAllTemplates() {
        return ApiResponse.success(productTemplateService.getAllTemplates());
    }

    /**
     * Шаблон по ID (без версий).
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductTemplateDto> getTemplateById(@PathVariable Long id) {
        return ApiResponse.success(productTemplateService.getTemplateById(id));
    }

    /**
     * Шаблон по ID со всеми версиями.
     */
    @GetMapping("/{id}/with-versions")
    public ApiResponse<ProductTemplateDto> getTemplateWithVersions(@PathVariable Long id) {
        return ApiResponse.success(productTemplateService.getTemplateWithVersions(id));
    }

    /**
     * Шаблон по ID серии.
     */
    @GetMapping("/by-series/{seriesId}")
    public ApiResponse<ProductTemplateDto> getTemplateBySeries(@PathVariable Long seriesId) {
        return ApiResponse.success(productTemplateService.getTemplateBySeries(seriesId));
    }

    /**
     * Создать шаблон (логический + первая DRAFT-версия).
     */
    @PostMapping
    public ApiResponse<ProductTemplateDto> createTemplate(
            @Valid @RequestBody CreateProductTemplateRequest request) {
        ProductTemplateDto created = productTemplateService.createTemplate(
                request.seriesId(),
                request.name(),
                request.description(),
                request.fields(),
                request.markingRule(),
                getCurrentUser()
        );
        return ApiResponse.success("Шаблон создан", created);
    }

    /**
     * Обновить метаданные шаблона (name, description).
     */
    @PutMapping("/{id}")
    public ApiResponse<ProductTemplateDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductTemplateRequest request) {
        ProductTemplateDto updated = productTemplateService.updateTemplate(
                id,
                request.name(),
                request.description(),
                getCurrentUser()
        );
        return ApiResponse.success("Шаблон обновлён", updated);
    }

    /**
     * Удалить шаблон вместе со всеми версиями.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long id) {
        productTemplateService.deleteTemplate(id, getCurrentUser());
        return ApiResponse.success("Шаблон удалён", null);
    }

    // ==========================================================
    // TEMPLATE — CLONE
    // ==========================================================

    /**
     * Клонировать шаблон для другой серии.
     */
    @PostMapping("/{id}/clone")
    public ApiResponse<ProductTemplateDto> cloneTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CloneProductTemplateRequest request) {
        ProductTemplateDto cloned = templateCloneService.cloneTemplate(
                id,
                request.targetSeriesId(),
                request.newName(),
                getCurrentUser()
        );
        return ApiResponse.success("Шаблон клонирован", cloned);
    }

    // ==========================================================
    // VERSIONS — READ
    // ==========================================================

    /**
     * Все версии шаблона (от новых к старым).
     */
    @GetMapping("/{id}/versions")
    public ApiResponse<List<ProductTemplateVersionDto>> getVersions(@PathVariable Long id) {
        return ApiResponse.success(productTemplateService.getVersionsByTemplate(id));
    }

    /**
     * Текущая PUBLISHED-версия.
     */
    @GetMapping("/{id}/versions/published")
    public ApiResponse<ProductTemplateVersionDto> getPublishedVersion(@PathVariable Long id) {
        return ApiResponse.success(productTemplateService.getPublishedVersion(id));
    }

    /**
     * Версии, доступные для создания карточек (PUBLISHED + LEGACY).
     */
    @GetMapping("/{id}/versions/available")
    public ApiResponse<List<ProductTemplateVersionDto>> getAvailableVersions(@PathVariable Long id) {
        return ApiResponse.success(productTemplateService.getAvailableVersions(id));
    }

    /**
     * DRAFT-версия (если есть).
     */
    @GetMapping("/{id}/versions/draft")
    public ApiResponse<ProductTemplateVersionDto> getDraftVersion(@PathVariable Long id) {
        return ApiResponse.success(productTemplateService.getDraftVersion(id));
    }

    // ==========================================================
    // VERSIONS — CREATE
    // ==========================================================

    /**
     * Создать новую DRAFT-версию.
     * <p>
     * Запрещено, если у шаблона уже есть DRAFT.
     */
    @PostMapping("/{id}/versions")
    public ApiResponse<ProductTemplateVersionDto> createNewVersion(
            @PathVariable Long id,
            @RequestBody(required = false) CreateProductTemplateVersionRequest request) {
        List<com.fanproduction.template.model.FieldDefinition> fields =
                request != null ? request.fields() : null;
        com.fanproduction.template.model.MarkingRule rule =
                request != null ? request.markingRule() : null;

        ProductTemplateVersionDto created = productTemplateService.createNewVersion(
                id,
                fields,
                rule,
                getCurrentUser()
        );
        return ApiResponse.success("Версия создана", created);
    }
}

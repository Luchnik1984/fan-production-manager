package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CloneFanTemplateRequest;
import com.fanproduction.api.dto.request.fan.CreateFanTemplateRequest;
import com.fanproduction.api.dto.request.fan.CreateFanTemplateVersionRequest;
import com.fanproduction.api.dto.request.fan.UpdateFanTemplateRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.FanTemplateDto;
import com.fanproduction.template.dto.FanTemplateVersionDto;
import com.fanproduction.template.service.FanTemplateService;
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
@RequestMapping("/api/fan-templates")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FanTemplateController extends BaseController {

    private final FanTemplateService fanTemplateService;
    private final TemplateCloneService templateCloneService;

    // ==========================================================
    // TEMPLATE — CRUD
    // ==========================================================

    /**
     * Все шаблоны (без версий).
     */
    @GetMapping
    public ApiResponse<List<FanTemplateDto>> getAllTemplates() {
        return ApiResponse.success(fanTemplateService.getAllTemplates());
    }

    /**
     * Шаблон по ID (без версий).
     */
    @GetMapping("/{id}")
    public ApiResponse<FanTemplateDto> getTemplateById(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getTemplateById(id));
    }

    /**
     * Шаблон по ID со всеми версиями.
     */
    @GetMapping("/{id}/with-versions")
    public ApiResponse<FanTemplateDto> getTemplateWithVersions(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getTemplateWithVersions(id));
    }

    /**
     * Шаблон по ID серии.
     */
    @GetMapping("/by-series/{seriesId}")
    public ApiResponse<FanTemplateDto> getTemplateBySeries(@PathVariable Long seriesId) {
        return ApiResponse.success(fanTemplateService.getTemplateBySeries(seriesId));
    }

    /**
     * Создать шаблон (логический + первая DRAFT-версия).
     */
    @PostMapping
    public ApiResponse<FanTemplateDto> createTemplate(
            @Valid @RequestBody CreateFanTemplateRequest request) {
        FanTemplateDto created = fanTemplateService.createTemplate(
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
    public ApiResponse<FanTemplateDto> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFanTemplateRequest request) {
        FanTemplateDto updated = fanTemplateService.updateTemplate(
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
        fanTemplateService.deleteTemplate(id, getCurrentUser());
        return ApiResponse.success("Шаблон удалён", null);
    }

    // ==========================================================
    // TEMPLATE — CLONE
    // ==========================================================

    /**
     * Клонировать шаблон для другой серии.
     */
    @PostMapping("/{id}/clone")
    public ApiResponse<FanTemplateDto> cloneTemplate(
            @PathVariable Long id,
            @Valid @RequestBody CloneFanTemplateRequest request) {
        FanTemplateDto cloned = templateCloneService.cloneTemplate(
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
    public ApiResponse<List<FanTemplateVersionDto>> getVersions(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getVersionsByTemplate(id));
    }

    /**
     * Текущая PUBLISHED-версия.
     */
    @GetMapping("/{id}/versions/published")
    public ApiResponse<FanTemplateVersionDto> getPublishedVersion(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getPublishedVersion(id));
    }

    /**
     * Версии, доступные для создания карточек (PUBLISHED + LEGACY).
     */
    @GetMapping("/{id}/versions/available")
    public ApiResponse<List<FanTemplateVersionDto>> getAvailableVersions(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getAvailableVersions(id));
    }

    /**
     * DRAFT-версия (если есть).
     */
    @GetMapping("/{id}/versions/draft")
    public ApiResponse<FanTemplateVersionDto> getDraftVersion(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getDraftVersion(id));
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
    public ApiResponse<FanTemplateVersionDto> createNewVersion(
            @PathVariable Long id,
            @RequestBody(required = false) CreateFanTemplateVersionRequest request) {
        List<com.fanproduction.template.model.FieldDefinition> fields =
                request != null ? request.fields() : null;
        com.fanproduction.template.model.MarkingRule rule =
                request != null ? request.markingRule() : null;

        FanTemplateVersionDto created = fanTemplateService.createNewVersion(
                id,
                fields,
                rule,
                getCurrentUser()
        );
        return ApiResponse.success("Версия создана", created);
    }
}

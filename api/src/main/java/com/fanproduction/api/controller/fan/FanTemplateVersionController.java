package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.UpdateFanTemplateVersionRequest;
import com.fanproduction.api.dto.request.fan.PreviewMarkingRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.DynamicFormMetadataDto;
import com.fanproduction.template.dto.FanTemplateVersionDto;
import com.fanproduction.template.service.DynamicFormBuilder;
import com.fanproduction.template.service.FanTemplateService;
import com.fanproduction.template.service.MarkingPreviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления версиями шаблонов.
 * <p>
 * Доступ: только ADMIN.
 */
@RestController
@RequestMapping("/api/fan-template-versions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FanTemplateVersionController extends BaseController {

    private final FanTemplateService fanTemplateService;
    private final DynamicFormBuilder dynamicFormBuilder;
    private final MarkingPreviewService markingPreviewService;

    // ==========================================================
    // READ
    // ==========================================================

    /**
     * Версия по ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<FanTemplateVersionDto> getVersionById(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.getVersionById(id));
    }

    /**
     * Метаданные формы по версии шаблона.
     * <p>
     * Используется GUI для динамической отрисовки формы карточки.
     */
    @GetMapping("/{id}/form")
    public ApiResponse<DynamicFormMetadataDto> getFormMetadata(@PathVariable Long id) {
        return ApiResponse.success(dynamicFormBuilder.buildFormMetadata(id));
    }

    /**
     * Предпросмотр полной маркировки FAN-карточки по значениям полей.
     * <p>
     * Не сохраняет карточку. Используется GUI для real-time обновления
     * поля «Полная маркировка».
     *
     * @param id      ID версии шаблона
     * @param request значения полей формы
     * @return вычисленная маркировка (пустая строка, если не удалось)
     */
    @PostMapping("/{id}/preview")
    public ApiResponse<String> previewMarking(
            @PathVariable Long id,
            @RequestBody PreviewMarkingRequest request) {
        String marking = markingPreviewService.preview(
                id,
                request != null ? request.values() : null);
        return ApiResponse.success(marking);
    }

    /**
     * Предварительная валидация версии.
     * <p>
     * Возвращает список ошибок (пустой, если всё корректно).
     */
    @GetMapping("/{id}/validate")
    public ApiResponse<List<String>> validateVersion(@PathVariable Long id) {
        return ApiResponse.success(fanTemplateService.validateVersion(id));
    }

    // ==========================================================
    // UPDATE DRAFT
    // ==========================================================

    /**
     * Обновить содержимое DRAFT-версии.
     * <p>
     * Запрещено для версий в других статусах.
     */
    @PutMapping("/{id}/draft")
    public ApiResponse<FanTemplateVersionDto> updateDraft(
            @PathVariable Long id,
            @RequestBody UpdateFanTemplateVersionRequest request) {
        FanTemplateVersionDto updated = fanTemplateService.updateDraftVersion(
                id,
                request.fields(),
                request.markingRule(),
                getCurrentUser()
        );
        return ApiResponse.success("Черновик обновлён", updated);
    }

    // ==========================================================
    // LIFECYCLE
    // ==========================================================

    /**
     * Опубликовать DRAFT-версию.
     * <p>
     * Предыдущая PUBLISHED автоматически становится LEGACY.
     */
    @PostMapping("/{id}/publish")
    public ApiResponse<FanTemplateVersionDto> publish(@PathVariable Long id) {
        FanTemplateVersionDto published = fanTemplateService.publishVersion(
                id, getCurrentUser());
        return ApiResponse.success("Версия опубликована", published);
    }

    /**
     * Пометить LEGACY-версию как DEPRECATED.
     */
    @PostMapping("/{id}/deprecate")
    public ApiResponse<FanTemplateVersionDto> deprecate(@PathVariable Long id) {
        FanTemplateVersionDto deprecated = fanTemplateService.deprecateVersion(
                id, getCurrentUser());
        return ApiResponse.success("Версия помечена как DEPRECATED", deprecated);
    }

    /**
     * Архивировать DEPRECATED-версию.
     */
    @PostMapping("/{id}/archive")
    public ApiResponse<FanTemplateVersionDto> archive(@PathVariable Long id) {
        FanTemplateVersionDto archived = fanTemplateService.archiveVersion(
                id, getCurrentUser());
        return ApiResponse.success("Версия заархивирована", archived);
    }

    /**
     * Удалить DRAFT или ARCHIVED версию.
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVersion(@PathVariable Long id) {
        fanTemplateService.deleteVersion(id, getCurrentUser());
        return ApiResponse.success("Версия удалена", null);
    }
}

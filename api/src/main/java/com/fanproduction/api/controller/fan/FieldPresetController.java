package com.fanproduction.api.controller.fan;

import com.fanproduction.api.controller.BaseController;
import com.fanproduction.api.dto.request.fan.CreateFieldPresetRequest;
import com.fanproduction.api.dto.request.fan.UpdateFieldPresetRequest;
import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.dto.FieldPresetDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.service.FieldPresetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер пресетов полей шаблона (US10.6).
 * <p>
 * Чтение — всем аутентифицированным (пресеты используются в конструкторе),
 * изменение — только ADMIN.
 */
@RestController
@RequestMapping("/api/field-presets")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FieldPresetController extends BaseController {

    private final FieldPresetService fieldPresetService;

    /**
     * Список пресетов.
     */
    @GetMapping
    public ApiResponse<List<FieldPresetDto>> getAll() {
        return ApiResponse.success(fieldPresetService.getAll());
    }

    /**
     * Пресет вместе с набором полей.
     */
    @GetMapping("/{id}")
    public ApiResponse<FieldPresetDto> getById(@PathVariable Long id) {
        return ApiResponse.success(fieldPresetService.getById(id));
    }

    /**
     * Создать пресет.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FieldPresetDto> create(@Valid @RequestBody CreateFieldPresetRequest request) {
        FieldPresetDto created = fieldPresetService.create(
                request.name(), request.description(), request.fields(),
                request.markingRule(), getCurrentUser());
        return ApiResponse.success("Пресет создан", created);
    }

    /**
     * Изменить пользовательский пресет.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FieldPresetDto> update(@PathVariable Long id,
                                              @Valid @RequestBody UpdateFieldPresetRequest request) {
        FieldPresetDto updated = fieldPresetService.update(
                id, request.name(), request.description(), request.fields(),
                request.markingRule(), getCurrentUser());
        return ApiResponse.success("Пресет обновлён", updated);
    }

    /**
     * Удалить пользовательский пресет.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        fieldPresetService.delete(id, getCurrentUser());
        return ApiResponse.success("Пресет удалён", null);
    }

    /**
     * Применить пресет к DRAFT-версии шаблона.
     * <p>
     * Добавляются только недостающие поля; существующие не меняются.
     */
    @PostMapping("/{id}/apply-to-version/{versionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ProductTemplateVersionDto> applyToVersion(
            @PathVariable Long id,
            @PathVariable Long versionId,
            @RequestParam(defaultValue = "false") boolean replace) {
        ProductTemplateVersionDto version = fieldPresetService.applyToVersion(
                versionId, id, replace, getCurrentUser());
        return ApiResponse.success(replace ? "Поля заменены пресетом" : "Пресет применён", version);
    }
}

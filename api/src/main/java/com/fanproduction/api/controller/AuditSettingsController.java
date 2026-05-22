package com.fanproduction.api.controller;

import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.core.dto.AuditSettingsDto;
import com.fanproduction.services.AuditSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit/settings")
@RequiredArgsConstructor
public class AuditSettingsController extends BaseController {

    private final AuditSettingsService auditSettingsService;

    /**
     * Получить текущие настройки аудита (только ADMIN)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AuditSettingsDto> getSettings() {
        AuditSettingsDto settings = auditSettingsService.getSettings();
        return ApiResponse.success(settings);
    }

    /**
     * Обновить настройки аудита (только ADMIN)
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AuditSettingsDto> updateSettings(@Valid @RequestBody AuditSettingsDto dto) {
        String updatedBy = getCurrentUser();
        AuditSettingsDto updated = auditSettingsService.updateSettings(dto, updatedBy);
        return ApiResponse.success("Настройки аудита обновлены", updated);
    }
}

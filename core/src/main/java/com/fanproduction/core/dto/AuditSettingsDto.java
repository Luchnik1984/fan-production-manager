package com.fanproduction.core.dto;

import java.time.LocalDateTime;

/**
 * DTO для настроек аудита.
 * Используется для передачи между API и GUI.
 */
public record AuditSettingsDto(
        Long id,

        // Настройки очистки
        Integer retentionDays,
        Integer maxRecords,
        String cleanupCron,

        // Фильтрация действий
        Boolean logLoginSuccess,
        Boolean logLoginFailed,
        Boolean logLogout,
        Boolean logRegister,
        Boolean logUserManagement,
        Boolean logCardCreate,
        Boolean logCardUpdate,
        Boolean logCardDelete,
        Boolean logDocumentGeneration,
        Boolean logProfileChanges,

        // Служебные поля
        LocalDateTime updatedAt,
        String updatedBy
) {}

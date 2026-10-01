package com.fanproduction.services;

import com.fanproduction.core.dto.AuditSettingsDto;

public interface AuditSettingsService {

    /**
     * Получить текущие настройки аудита
     */
    AuditSettingsDto getSettings();

    /**
     * Обновить настройки аудита
     * @param dto новые настройки
     * @param updatedBy email пользователя, который обновил
     * @return обновлённые настройки
     */
    AuditSettingsDto updateSettings(AuditSettingsDto dto, String updatedBy);
}

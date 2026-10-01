package com.fanproduction.services.impl;

import com.fanproduction.core.dto.AuditSettingsDto;
import com.fanproduction.core.entity.settings.AuditSettingsEntity;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.repositories.settings.AuditSettingsRepository;
import com.fanproduction.services.AuditService;
import com.fanproduction.services.AuditSettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditSettingsServiceImpl implements AuditSettingsService {

    private final AuditSettingsRepository auditSettingsRepository;
    private final AuditService auditService;

    @Override
    public AuditSettingsDto getSettings() {
        AuditSettingsEntity entity = getSettingsEntity();
        return toDto(entity);
    }

    @Override
    @Transactional
    public AuditSettingsDto updateSettings(AuditSettingsDto dto, String updatedBy) {
        AuditSettingsEntity entity = getSettingsEntity();

        // Валидация (используем методы record — без get)
        validateRetentionDays(dto.retentionDays());
        validateMaxRecords(dto.maxRecords());
        validateCron(dto.cleanupCron());

        // Обновляем поля
        entity.setRetentionDays(dto.retentionDays());
        entity.setMaxRecords(dto.maxRecords());
        entity.setCleanupCron(dto.cleanupCron());

        entity.setLogLoginSuccess(dto.logLoginSuccess());
        entity.setLogLoginFailed(dto.logLoginFailed());
        entity.setLogLogout(dto.logLogout());
        entity.setLogRegister(dto.logRegister());
        entity.setLogUserManagement(dto.logUserManagement());
        entity.setLogCardCreate(dto.logCardCreate());
        entity.setLogCardUpdate(dto.logCardUpdate());
        entity.setLogCardDelete(dto.logCardDelete());
        entity.setLogDocumentGeneration(dto.logDocumentGeneration());
        entity.setLogProfileChanges(dto.logProfileChanges());

        entity.setUpdatedBy(updatedBy);

        AuditSettingsEntity saved = auditSettingsRepository.save(entity);

        // Логируем изменение настроек (всегда, независимо от фильтрации)
        auditService.log(AuditAction.UPDATE_AUDIT_SETTINGS,
                String.format("Пользователь %s изменил настройки аудита: retentionDays=%d, maxRecords=%d, cleanupCron=%s",
                        updatedBy, dto.retentionDays(), dto.maxRecords(), dto.cleanupCron()));

        log.info("Настройки аудита обновлены пользователем: {}", updatedBy);

        return toDto(saved);
    }

    private AuditSettingsEntity getSettingsEntity() {
        AuditSettingsEntity entity = auditSettingsRepository.findFirst();
        if (entity == null) {
            throw new IllegalStateException("Настройки аудита не найдены. Проверьте миграцию V29.");
        }
        return entity;
    }

    private void validateRetentionDays(Integer days) {
        if (days == null || days < 0) {
            throw new IllegalArgumentException("Срок хранения должен быть >= 0");
        }
    }

    private void validateMaxRecords(Integer records) {
        if (records == null || records < 0) {
            throw new IllegalArgumentException("Максимальное количество записей должно быть >= 0");
        }
    }

    private void validateCron(String cron) {
        if (cron == null || cron.trim().isEmpty()) {
            throw new IllegalArgumentException("Cron выражение не может быть пустым");
        }
        String[] parts = cron.trim().split(" ");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Cron выражение должно содержать 6 частей: секунды минуты часы день месяц день_недели");
        }
    }

    private AuditSettingsDto toDto(AuditSettingsEntity entity) {
        return new AuditSettingsDto(
                entity.getId(),
                entity.getRetentionDays(),
                entity.getMaxRecords(),
                entity.getCleanupCron(),
                entity.getLogLoginSuccess(),
                entity.getLogLoginFailed(),
                entity.getLogLogout(),
                entity.getLogRegister(),
                entity.getLogUserManagement(),
                entity.getLogCardCreate(),
                entity.getLogCardUpdate(),
                entity.getLogCardDelete(),
                entity.getLogDocumentGeneration(),
                entity.getLogProfileChanges(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}

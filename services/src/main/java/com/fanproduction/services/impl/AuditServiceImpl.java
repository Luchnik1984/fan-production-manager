package com.fanproduction.services.impl;

import com.fanproduction.core.dto.AuditLogDto;
import com.fanproduction.core.entity.settings.AuditSettingsEntity;
import com.fanproduction.core.entity.user.AuditLog;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.settings.AuditSettingsRepository;
import com.fanproduction.repositories.user.AuditLogRepository;
import com.fanproduction.services.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditSettingsRepository auditSettingsRepository;
    private final CurrentUserProvider currentUserProvider;

    @Override
    @Transactional
    public void log(AuditAction action, String details) {
        String username = currentUserProvider.getCurrentUserEmail();
        if (username == null) {
            username = "SYSTEM";
        }
        log(username, action, details);
    }

    @Override
    @Transactional
    public void log(String username, AuditAction action, String details) {
        // Проверяем, нужно ли логировать это действие
        if (!shouldLog(action)) {
            log.debug("Пропуск логирования действия {} (отключено в настройках)", action);
            return;
        }

        AuditLog logEntry = new AuditLog();
        logEntry.setUsername(username);
        logEntry.setAction(action.name());
        logEntry.setDetails(details);
        logEntry.setIpAddress(getIpAddress());
        auditLogRepository.save(logEntry);
    }

    /**
     * Проверяет, нужно ли логировать данное действие
     */
    private boolean shouldLog(AuditAction action) {
        AuditSettingsEntity settings = getSettings();

        return switch (action) {
            case LOGIN_SUCCESS -> settings.getLogLoginSuccess();
            case LOGIN_FAILED -> settings.getLogLoginFailed();
            case LOGOUT -> settings.getLogLogout();
            case REGISTER -> settings.getLogRegister();
            case APPROVE_USER, REJECT_USER, BLOCK_USER, UNBLOCK_USER -> settings.getLogUserManagement();
            case CREATE_CARD -> settings.getLogCardCreate();
            case UPDATE_CARD -> settings.getLogCardUpdate();
            case DELETE_CARD -> settings.getLogCardDelete();
            case GENERATE_TZ, GENERATE_PASSPORT, GENERATE_PLATE -> settings.getLogDocumentGeneration();
            case UPDATE_PROFILE, CHANGE_PASSWORD -> settings.getLogProfileChanges();
            default -> true; // остальные действия логируем всегда
        };
    }

    /**
     * Плановая очистка с использованием настроек из БД
     */
    @Scheduled(fixedDelay = 3600000) // проверяем настройки каждый час
    @Transactional
    public void scheduledCleanup() {
        AuditSettingsEntity settings = getSettings();

        int deletedByTime = 0;
        int deletedByCount = 0;

        // Очистка по времени
        if (settings.getRetentionDays() > 0) {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(settings.getRetentionDays());
            deletedByTime = auditLogRepository.deleteByTimestampBefore(cutoff);
        }

        // Очистка по количеству
        if (settings.getMaxRecords() > 0) {
            long total = auditLogRepository.count();
            if (total > settings.getMaxRecords()) {
                long toDelete = total - settings.getMaxRecords();
                deletedByCount = auditLogRepository.deleteOldest(toDelete);
            }
        }

        if (deletedByTime > 0 || deletedByCount > 0) {
            log.info("Очистка аудита: удалено по сроку {} записей, по лимиту {} записей",
                    deletedByTime, deletedByCount);
        }
    }

    private AuditSettingsEntity getSettings() {
        AuditSettingsEntity settings = auditSettingsRepository.findFirst();
        if (settings == null) {
            log.warn("Настройки аудита не найдены, используются значения по умолчанию");
            // Возвращаем значения по умолчанию
            AuditSettingsEntity defaultSettings = new AuditSettingsEntity();
            defaultSettings.setRetentionDays(90);
            defaultSettings.setMaxRecords(50000);
            defaultSettings.setCleanupCron("0 0 2 * * *");
            defaultSettings.setLogLoginSuccess(true);
            defaultSettings.setLogLoginFailed(true);
            defaultSettings.setLogLogout(true);
            defaultSettings.setLogRegister(true);
            defaultSettings.setLogUserManagement(true);
            defaultSettings.setLogCardCreate(true);
            defaultSettings.setLogCardUpdate(true);
            defaultSettings.setLogCardDelete(true);
            defaultSettings.setLogDocumentGeneration(true);
            defaultSettings.setLogProfileChanges(true);
            return defaultSettings;
        }
        return settings;
    }

    @Override
    public Page<AuditLogDto> getLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    public Page<AuditLogDto> getLogsByUser(String username, Pageable pageable) {
        return auditLogRepository.findByUsername(username, pageable).map(this::toDto);
    }

    @Override
    public Page<AuditLogDto> getLogsByAction(AuditAction action, Pageable pageable) {
        return auditLogRepository.findByAction(action.name(), pageable).map(this::toDto);
    }

    @Override
    public int deleteOlderThan(int days) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return auditLogRepository.deleteByTimestampBefore(cutoff);
    }

    @Override
    public int keepOnlyLast(int count) {
        long total = auditLogRepository.count();
        if (total <= count) {
            return 0;
        }
        long toDelete = total - count;
        return auditLogRepository.deleteOldest(toDelete);
    }

    @Override
    public int getTotalCount() {
        return (int) auditLogRepository.count();
    }

    private AuditLogDto toDto(AuditLog logEntry) {
        return new AuditLogDto(
                logEntry.getId(),
                logEntry.getUsername(),
                AuditAction.valueOf(logEntry.getAction()),
                logEntry.getDetails(),
                logEntry.getTimestamp(),
                logEntry.getIpAddress()
        );
    }

    private String getIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}

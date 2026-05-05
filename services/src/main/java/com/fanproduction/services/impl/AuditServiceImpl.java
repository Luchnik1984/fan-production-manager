package com.fanproduction.services.impl;

import com.fanproduction.core.dto.AuditLogDto;
import com.fanproduction.core.entity.user.AuditLog;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.security.CurrentUserProvider;
import com.fanproduction.repositories.user.AuditLogRepository;
import com.fanproduction.services.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private CurrentUserProvider currentUserProvider;

    @Value("${audit.retention.days:90}")
    private int retentionDays;

    @Value("${audit.retention.max-records:50000}")
    private int maxRecords;

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
        AuditLog log = new AuditLog();
        log.setUsername(username);
        log.setAction(action.name());
        log.setDetails(details);
        log.setIpAddress(getIpAddress());
        auditLogRepository.save(log);

        // Проверяем размер журнала после каждой записи
        // Вызовы идут ВНУТРИ транзакции, поэтому отдельные @Transactional не нужны
        checkAndCleanup();
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

    /**
     * Проверяет и очищает старые записи.
     * Вызывается из транзакционного метода log(), поэтому выполняется в той же транзакции.
     */
    private void checkAndCleanup() {
        // Проверяем по времени
        if (retentionDays > 0) {
            deleteOlderThan(retentionDays);
        }

        // Проверяем по количеству записей
        if (maxRecords > 0) {
            keepOnlyLast(maxRecords);
        }
    }

    private AuditLogDto toDto(AuditLog log) {
        return new AuditLogDto(
                log.getId(),
                log.getUsername(),
                AuditAction.valueOf(log.getAction()),
                log.getDetails(),
                log.getTimestamp(),
                log.getIpAddress()
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

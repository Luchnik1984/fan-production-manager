package com.fanproduction.services;

import com.fanproduction.core.dto.AuditLogDto;
import com.fanproduction.core.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {

    void log(AuditAction action, String details);

    void log(String username, AuditAction action, String details);

    Page<AuditLogDto> getLogs(Pageable pageable);

    Page<AuditLogDto> getLogsByUser(String username, Pageable pageable);

    Page<AuditLogDto> getLogsByAction(AuditAction action, Pageable pageable);

    int deleteOlderThan(int days);

    int keepOnlyLast(int count);

    int getTotalCount();
}


package com.fanproduction.core.dto;

import com.fanproduction.core.enums.AuditAction;

import java.time.LocalDateTime;

public record AuditLogDto(Long id,
                          String username,
                          AuditAction action,
                          String details,
                          LocalDateTime timestamp,
                          String ipAddress
) {}

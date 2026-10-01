package com.fanproduction.gui.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogDto {
    private Long id;
    private String username;
    private String action;
    private String details;
    private LocalDateTime timestamp;
    private String ipAddress;
}

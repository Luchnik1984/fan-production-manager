package com.fanproduction.api.controller;

import com.fanproduction.api.dto.ApiResponse;
import com.fanproduction.core.dto.AuditLogDto;
import com.fanproduction.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<AuditLogDto>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());

        Page<AuditLogDto> result;
        if (username != null && !username.isEmpty()) {
            result = auditService.getLogsByUser(username, pageable);
        } else if (action != null && !action.isEmpty()) {
            try {
                com.fanproduction.core.enums.AuditAction auditAction =
                        com.fanproduction.core.enums.AuditAction.valueOf(action);
                result = auditService.getLogsByAction(auditAction, pageable);
            } catch (IllegalArgumentException e) {
                return ApiResponse.error("Invalid action: " + action);
            }
        } else {
            result = auditService.getLogs(pageable);
        }

        return ApiResponse.success(result);
    }

    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Long> getTotalCount() {
        return ApiResponse.success((long) auditService.getTotalCount());
    }
}
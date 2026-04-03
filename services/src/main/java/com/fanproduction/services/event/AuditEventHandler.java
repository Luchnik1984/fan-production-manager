package com.fanproduction.services.event;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.services.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Асинхронный обработчик событий аудита.
 * Разрывает циклическую зависимость между UserService и AuditService.
 */
@Component
@RequiredArgsConstructor
public class AuditEventHandler {

    private final AuditService auditService;

    @Async
    @EventListener
    public void handleAuditEvent(AuditEvent event) {
        auditService.log(event.getUsername(), event.getAction(), event.getDetails());
    }
}
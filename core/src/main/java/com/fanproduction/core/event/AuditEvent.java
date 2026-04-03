package com.fanproduction.core.event;

import com.fanproduction.core.enums.AuditAction;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Событие аудита. Публикуется при выполнении действий, которые нужно залогировать.
 * Обрабатывается асинхронно в AuditEventHandler.
 */
@Getter
public class AuditEvent extends ApplicationEvent {

    private final String username;
    private final AuditAction action;
    private final String details;

    public AuditEvent(Object source, String username, AuditAction action, String details) {
        super(source);
        this.username = username;
        this.action = action;
        this.details = details;
    }
}

package com.fanproduction.services.event;

import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.services.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventHandler {

    private final AuditService auditService;

    /**
     * Асинхронный обработчик событий аудита.
     * <p>
     * Важно: @Transactional с propagation = REQUIRES_NEW гарантирует,
     * что аудит сохраняется в отдельной транзакции.
     * Это необходимо потому, что метод выполняется асинхронно в другом потоке,
     * и транзакция из вызывающего метода не передаётся.
     */
    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAuditEvent(AuditEvent event) {
        try {
            auditService.log(event.getUsername(), event.getAction(), event.getDetails());
        } catch (Exception e) {
            // Логируем ошибку, но не бросаем исключение,
            // чтобы не прерывать основной поток выполнения
            log.error("Ошибка при сохранении события аудита: {}", e.getMessage(), e);
        }
    }
}
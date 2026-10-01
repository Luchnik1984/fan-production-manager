package com.fanproduction.core.entity.settings;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Настройки журнала аудита.
 * Хранит параметры очистки и фильтрации логов.
 * В таблице всегда только одна строка (singleton).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_settings")
public class AuditSettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Сколько дней хранить записи (0 = без ограничения по времени)
     */
    @Column(name = "retention_days", nullable = false)
    private Integer retentionDays = 90;

    /**
     * Максимальное количество записей (0 = без ограничения по количеству)
     */
    @Column(name = "max_records", nullable = false)
    private Integer maxRecords = 50000;

    /**
     * Cron расписание для плановой очистки
     * Пример: "0 0 2 * * *" - каждый день в 2 часа ночи
     */
    @Column(name = "cleanup_cron", nullable = false, length = 50)
    private String cleanupCron = "0 0 2 * * *";

    // ========== ФИЛЬТРАЦИЯ ДЕЙСТВИЙ ДЛЯ ЛОГИРОВАНИЯ ==========

    @Column(name = "log_login_success", nullable = false)
    private Boolean logLoginSuccess = true;

    @Column(name = "log_login_failed", nullable = false)
    private Boolean logLoginFailed = true;

    @Column(name = "log_logout", nullable = false)
    private Boolean logLogout = true;

    @Column(name = "log_register", nullable = false)
    private Boolean logRegister = true;

    @Column(name = "log_user_management", nullable = false)
    private Boolean logUserManagement = true;

    @Column(name = "log_card_create", nullable = false)
    private Boolean logCardCreate = true;

    @Column(name = "log_card_update", nullable = false)
    private Boolean logCardUpdate = true;

    @Column(name = "log_card_delete", nullable = false)
    private Boolean logCardDelete = true;

    @Column(name = "log_document_generation", nullable = false)
    private Boolean logDocumentGeneration = true;

    @Column(name = "log_profile_changes", nullable = false)
    private Boolean logProfileChanges = true;

    // ========== СЛУЖЕБНЫЕ ПОЛЯ ==========

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

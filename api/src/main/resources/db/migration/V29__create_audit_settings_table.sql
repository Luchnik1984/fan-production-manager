-- V29__create_audit_settings_table.sql
-- Создание таблицы настроек аудита

CREATE TABLE IF NOT EXISTS audit_settings (
                                              id BIGSERIAL PRIMARY KEY,

    -- Настройки очистки
                                              retention_days INTEGER NOT NULL DEFAULT 90,
                                              max_records INTEGER NOT NULL DEFAULT 50000,
                                              cleanup_cron VARCHAR(50) NOT NULL DEFAULT '0 0 2 * * *',

    -- Фильтрация действий для логирования
    log_login_success BOOLEAN NOT NULL DEFAULT TRUE,
    log_login_failed BOOLEAN NOT NULL DEFAULT TRUE,
    log_logout BOOLEAN NOT NULL DEFAULT TRUE,
    log_register BOOLEAN NOT NULL DEFAULT TRUE,
    log_user_management BOOLEAN NOT NULL DEFAULT TRUE,
    log_card_create BOOLEAN NOT NULL DEFAULT TRUE,
    log_card_update BOOLEAN NOT NULL DEFAULT TRUE,
    log_card_delete BOOLEAN NOT NULL DEFAULT TRUE,
    log_document_generation BOOLEAN NOT NULL DEFAULT TRUE,
    log_profile_changes BOOLEAN NOT NULL DEFAULT TRUE,

    -- Служебные поля
    updated_at TIMESTAMP NOT NULL,
    updated_by VARCHAR(100)
    );

-- Вставляем начальную запись (только одна строка в таблице)
INSERT INTO audit_settings (
    retention_days,
    max_records,
    cleanup_cron,
    log_login_success,
    log_login_failed,
    log_logout,
    log_register,
    log_user_management,
    log_card_create,
    log_card_update,
    log_card_delete,
    log_document_generation,
    log_profile_changes,
    updated_at,
    updated_by
) VALUES (
             90,
             50000,
             '0 0 2 * * *',
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             TRUE,
             NOW(),
             'system'
         );

-- Комментарии к таблице и колонкам
COMMENT ON TABLE audit_settings IS 'Настройки журнала аудита';
COMMENT ON COLUMN audit_settings.retention_days IS 'Сколько дней хранить записи (0 - без ограничения)';
COMMENT ON COLUMN audit_settings.max_records IS 'Максимальное количество записей (0 - без ограничения)';
COMMENT ON COLUMN audit_settings.cleanup_cron IS 'Cron расписание для плановой очистки';
COMMENT ON COLUMN audit_settings.log_login_success IS 'Логировать успешные входы';
COMMENT ON COLUMN audit_settings.log_login_failed IS 'Логировать неудачные попытки входа';
COMMENT ON COLUMN audit_settings.log_logout IS 'Логировать выход из системы';
COMMENT ON COLUMN audit_settings.log_register IS 'Логировать регистрацию пользователей';
COMMENT ON COLUMN audit_settings.log_user_management IS 'Логировать модерацию пользователей';
COMMENT ON COLUMN audit_settings.log_card_create IS 'Логировать создание карточек продукции';
COMMENT ON COLUMN audit_settings.log_card_update IS 'Логировать изменение карточек продукции';
COMMENT ON COLUMN audit_settings.log_card_delete IS 'Логировать удаление карточек продукции';
COMMENT ON COLUMN audit_settings.log_document_generation IS 'Логировать генерацию документов';
COMMENT ON COLUMN audit_settings.log_profile_changes IS 'Логировать изменения профиля и пароля';
-- V3__create_audit_log_table.sql
-- Создание таблицы аудита действий пользователей

CREATE TABLE IF NOT EXISTS audit_log (
                                         id BIGSERIAL PRIMARY KEY,
                                         username VARCHAR(100) NOT NULL,
    action VARCHAR(50) NOT NULL,
    details VARCHAR(500),
    timestamp TIMESTAMP NOT NULL,
    ip_address VARCHAR(45)
    );

-- Создаём индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_audit_log_username ON audit_log(username);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);
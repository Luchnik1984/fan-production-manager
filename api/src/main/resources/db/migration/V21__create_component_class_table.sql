-- V21__create_component_class_table.sql
-- Создание таблицы классов компонентов

CREATE TABLE IF NOT EXISTS component_class (
                                               id BIGSERIAL PRIMARY KEY,
                                               name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

-- Индекс для быстрого поиска по имени
CREATE INDEX IF NOT EXISTS idx_component_class_name ON component_class(name);
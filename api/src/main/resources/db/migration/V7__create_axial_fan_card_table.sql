-- V7__create_axial_fan_card_table.sql
-- Создание таблицы для осевых вентиляторов

CREATE TABLE IF NOT EXISTS axial_fan_card (
    id BIGSERIAL PRIMARY KEY,
    series_name VARCHAR(100),
    position VARCHAR(10),
    blade_count INTEGER,
    blade_slots INTEGER,
    blade_shape VARCHAR(50),
    blade_angle INTEGER,
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_axial_fan_series ON axial_fan_card(series_name);
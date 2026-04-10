-- V9__create_radial_fan_card_table.sql
-- Создание таблицы для радиальных вентиляторов

CREATE TABLE IF NOT EXISTS radial_fan_card (
                                               id BIGSERIAL PRIMARY KEY,
                                               series_name VARCHAR(100),
    front_disk_mod VARCHAR(10),
    blade_mod VARCHAR(50),
    blade_count INTEGER,
    housing_angle INTEGER,
    rotation_direction VARCHAR(10),
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_radial_fan_series ON radial_fan_card(series_name);

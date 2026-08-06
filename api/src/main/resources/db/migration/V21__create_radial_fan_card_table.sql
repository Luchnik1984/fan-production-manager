-- V21__create_radial_fan_card_table.sql
-- Создание таблицы для радиальных вентиляторов

CREATE TABLE IF NOT EXISTS radial_fan_card (
                                               id BIGSERIAL PRIMARY KEY,
                                               series_name VARCHAR(100),
    radial_wheel_id BIGINT,
    housing_angle INTEGER,
    rotation_direction VARCHAR(10),
    cable_spec VARCHAR(100),
    fan_class VARCHAR(20),
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_radial_fan_series ON radial_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_radial_fan_class ON radial_fan_card(fan_class);
CREATE INDEX IF NOT EXISTS idx_radial_fan_housing ON radial_fan_card(housing_angle);
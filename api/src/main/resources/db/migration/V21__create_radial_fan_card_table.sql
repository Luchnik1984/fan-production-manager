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
    radial_wheel_full_marking VARCHAR(200),
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_radial_fan_series ON radial_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_radial_fan_wheel ON radial_fan_card(radial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_radial_fan_class ON radial_fan_card(fan_class);
CREATE INDEX IF NOT EXISTS idx_radial_fan_housing ON radial_fan_card(housing_angle);
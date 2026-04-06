-- V9__create_duct_fan_card_table.sql
-- Создание таблицы для канальных вентиляторов

CREATE TABLE IF NOT EXISTS duct_fan_card (
                                             id BIGSERIAL PRIMARY KEY,
                                             series_name VARCHAR(100),
    housing_type VARCHAR(20),
    noise_level INTEGER,
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_duct_fan_series ON duct_fan_card(series_name);

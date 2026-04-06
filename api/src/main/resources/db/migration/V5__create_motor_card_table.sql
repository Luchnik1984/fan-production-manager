-- V5__create_motor_card_table.sql
-- Создание таблицы для электродвигателей

CREATE TABLE IF NOT EXISTS motor_card (
                                          id BIGSERIAL PRIMARY KEY,
                                          motor_type VARCHAR(20),
    poles INTEGER,
    power_kw DOUBLE PRECISION,
    rated_speed_rpm INTEGER,
    actual_speed_rpm INTEGER,
    shaft_size INTEGER,
    mounting_type VARCHAR(20),
    climate_type VARCHAR(10),
    voltage INTEGER,
    operation_mode VARCHAR(10),
    weight_kg DOUBLE PRECISION,
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

-- Индекс для быстрого поиска по типу двигателя
CREATE INDEX IF NOT EXISTS idx_motor_card_type ON motor_card(motor_type);
CREATE INDEX IF NOT EXISTS idx_motor_card_power ON motor_card(power_kw);
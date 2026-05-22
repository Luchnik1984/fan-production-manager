-- V5__create_motor_card_table.sql
-- Создание таблицы для электродвигателей (объединённая с полями из V18)

CREATE TABLE IF NOT EXISTS motor_card (
                                          id BIGSERIAL PRIMARY KEY,

    -- Основные характеристики
                                          series VARCHAR(50),
    motor_type VARCHAR(20),
    poles INTEGER,
    power_kw DOUBLE PRECISION,
    rated_speed_rpm INTEGER,
    actual_speed_rpm INTEGER,
    shaft_size INTEGER,
    mounting_type VARCHAR(50),
    climate_type VARCHAR(10),
    voltage INTEGER,
    operation_mode VARCHAR(10),
    weight_kg DOUBLE PRECISION,

    -- Специальные исполнения
    general_purpose BOOLEAN DEFAULT TRUE,
    fireproof BOOLEAN DEFAULT FALSE,
    fireproof_marking VARCHAR(50),
    max_temperature INTEGER,
    explosion_proof BOOLEAN DEFAULT FALSE,
    explosion_marking VARCHAR(100),
    full_marking VARCHAR(200),

    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_motor_card_type ON motor_card(motor_type);
CREATE INDEX IF NOT EXISTS idx_motor_card_power ON motor_card(power_kw);
CREATE INDEX IF NOT EXISTS idx_motor_card_series ON motor_card(series);
CREATE INDEX IF NOT EXISTS idx_motor_card_poles ON motor_card(poles);
CREATE INDEX IF NOT EXISTS idx_motor_card_voltage ON motor_card(voltage);
CREATE UNIQUE INDEX IF NOT EXISTS idx_motor_card_full_marking ON motor_card(full_marking);
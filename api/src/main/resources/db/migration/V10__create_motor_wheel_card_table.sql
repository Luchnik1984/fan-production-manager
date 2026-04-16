-- V10__create_motor_wheel_card_table.sql
-- Создание таблицы для мотор-колёс

CREATE TABLE IF NOT EXISTS motor_wheel_card (
                                                id BIGSERIAL PRIMARY KEY,
                                                manufacturer VARCHAR(100),
    blade_type VARCHAR(30),
    size INTEGER,
    poles INTEGER,
    voltage_code VARCHAR(10),
    power_kw DOUBLE PRECISION,
    rated_speed_rpm INTEGER,
    actual_speed_rpm INTEGER,
    voltage INTEGER,
    weight_kg DOUBLE PRECISION,
    full_marking VARCHAR(500),
    motor_code VARCHAR(50),
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_motor_wheel_size ON motor_wheel_card(size);
CREATE INDEX IF NOT EXISTS idx_motor_wheel_poles ON motor_wheel_card(poles);
CREATE INDEX IF NOT EXISTS idx_motor_wheel_blade_type ON motor_wheel_card(blade_type)

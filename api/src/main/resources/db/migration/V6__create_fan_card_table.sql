-- V6__create_fan_card_table.sql
-- Создание таблицы для общих полей вентиляторов

CREATE TABLE IF NOT EXISTS fan_card (
                                        id BIGSERIAL PRIMARY KEY,
                                        size DOUBLE PRECISION,
                                        execution VARCHAR(50),
    trim_coefficient DOUBLE PRECISION,
    climate_type VARCHAR(10),
    motor_id BIGINT,
    hub_type VARCHAR(50),
    wheel_formula VARCHAR(100),
    wheel_diameter DOUBLE PRECISION,
    cable_spec VARCHAR(100),
    has_ha BOOLEAN DEFAULT FALSE,
    has_ca BOOLEAN DEFAULT FALSE,
    certificate_number VARCHAR(100),
    fan_class VARCHAR(20),
    fan_type VARCHAR(30),
    fan_subtype VARCHAR(30),
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE,
    FOREIGN KEY (motor_id) REFERENCES motor_card(id)
    );

-- Индексы
CREATE INDEX IF NOT EXISTS idx_fan_card_size ON fan_card(size);
CREATE INDEX IF NOT EXISTS idx_fan_card_motor_id ON fan_card(motor_id);
CREATE INDEX IF NOT EXISTS idx_fan_card_type ON fan_card(fan_type);
CREATE INDEX IF NOT EXISTS idx_fan_card_class ON fan_card(fan_class);
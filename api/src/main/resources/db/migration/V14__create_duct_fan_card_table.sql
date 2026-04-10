-- V14__create_duct_fan_card_table.sql
-- Создание таблицы для канальных вентиляторов

CREATE TABLE IF NOT EXISTS duct_fan_card (
                                             id BIGSERIAL PRIMARY KEY,
                                             series_name VARCHAR(100),
    housing_type VARCHAR(20),
    noise_level INTEGER,
    execution_type VARCHAR(20),
    duct_fan_type VARCHAR(20),
    motor_wheel_id BIGINT,
    radial_wheel_id BIGINT,
    wheel_size DOUBLE PRECISION,
    motor_poles_code VARCHAR(10),
    cup_id BIGINT,
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id),
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id),
    FOREIGN KEY (cup_id) REFERENCES cup_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_duct_fan_series ON duct_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_duct_fan_type ON duct_fan_card(duct_fan_type);
CREATE INDEX IF NOT EXISTS idx_duct_fan_execution ON duct_fan_card(execution_type);
CREATE INDEX IF NOT EXISTS idx_duct_fan_motor_wheel ON duct_fan_card(motor_wheel_id);
CREATE INDEX IF NOT EXISTS idx_duct_fan_radial_wheel ON duct_fan_card(radial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_duct_fan_cup ON duct_fan_card(cup_id);

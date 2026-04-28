-- V12__create_duct_fan_card_table.sql
-- Создание таблицы для канальных вентиляторов

CREATE TABLE IF NOT EXISTS duct_fan_card (
                                             id BIGSERIAL PRIMARY KEY,
                                             series_name VARCHAR(100) DEFAULT 'VRK-PatAIR',
    duct_size VARCHAR(50),
    execution_type VARCHAR(20),
    duct_fan_type VARCHAR(20),
    motor_wheel_id BIGINT,
    radial_wheel_id BIGINT,
    wheel_size INTEGER,
    motor_wheel_full_marking VARCHAR(200),
    radial_wheel_full_marking VARCHAR(200),
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id),
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_duct_fan_series ON duct_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_duct_fan_type ON duct_fan_card(duct_fan_type);
CREATE INDEX IF NOT EXISTS idx_duct_fan_motor_wheel ON duct_fan_card(motor_wheel_id);
CREATE INDEX IF NOT EXISTS idx_duct_fan_radial_wheel ON duct_fan_card(radial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_duct_fan_execution ON duct_fan_card(execution_type);

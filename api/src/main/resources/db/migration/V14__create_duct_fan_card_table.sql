-- V14__create_duct_fan_card_table.sql
-- Создание таблицы для канальных вентиляторов

CREATE TABLE IF NOT EXISTS duct_fan_card (
                                             id BIGSERIAL PRIMARY KEY,

    -- Основная информация
                                             series_name VARCHAR(100) DEFAULT 'VRK-PatAIR',
    duct_size VARCHAR(50),                      -- типоразмер (40-20, 60-30)
    execution_type VARCHAR(20),                -- P, PS, PKV, PRV
    duct_fan_type VARCHAR(20),                 -- MOTOR_WHEEL или RADIAL_WHEEL

-- Для типа MOTOR_WHEEL
    motor_wheel_id BIGINT,

    -- Для типа RADIAL_WHEEL
    radial_wheel_id BIGINT,
    motor_id BIGINT,
    wheel_size INTEGER,                         -- размер колеса (25, 30)

-- Расчётные поля (заполняются из выбранных компонентов)
    poles INTEGER,
    voltage INTEGER,
    voltage_code VARCHAR(10),                  -- E - 220, D - 380
    rated_speed_rpm INTEGER,
    actual_speed_rpm INTEGER,

    -- Полная маркировка
    full_marking VARCHAR(200),

    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id),
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id),
    FOREIGN KEY (motor_id) REFERENCES motor_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_duct_fan_series ON duct_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_duct_fan_type ON duct_fan_card(duct_fan_type);
CREATE INDEX IF NOT EXISTS idx_duct_fan_motor_wheel ON duct_fan_card(motor_wheel_id);
CREATE INDEX IF NOT EXISTS idx_duct_fan_radial_wheel ON duct_fan_card(radial_wheel_id);

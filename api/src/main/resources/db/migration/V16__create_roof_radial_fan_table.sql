-- V16__create_roof_radial_fan_table.sql
-- Создание таблицы для крышных радиальных вентиляторов

CREATE TABLE IF NOT EXISTS roof_radial_fan (
                                               id BIGSERIAL PRIMARY KEY,

    -- Основная информация
                                               series_name VARCHAR(100) DEFAULT 'VR-PatAIR',
    execution_type VARCHAR(20),                -- KpR, KpRS
    roof_size VARCHAR(50),                     -- типоразмер
    climate_type VARCHAR(10) DEFAULT 'У1',

    -- Радиальное колесо
    radial_wheel_id BIGINT,

    -- Электродвигатель
    motor_id BIGINT,

    -- Расчётные поля (из электродвигателя)
    poles INTEGER,
    voltage INTEGER,
    voltage_code VARCHAR(10),
    rated_speed_rpm INTEGER,
    actual_speed_rpm INTEGER,

    -- Полная маркировка
    full_marking VARCHAR(200),

    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id),
    FOREIGN KEY (motor_id) REFERENCES motor_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_roof_radial_series ON roof_radial_fan(series_name);
CREATE INDEX IF NOT EXISTS idx_roof_radial_radial_wheel ON roof_radial_fan(radial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_roof_radial_motor ON roof_radial_fan(motor_id);
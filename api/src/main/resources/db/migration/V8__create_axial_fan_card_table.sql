-- V8__create_axial_fan_card_table.sql
-- Создание таблицы для осевых вентиляторов

CREATE TABLE IF NOT EXISTS axial_fan_card (
                                              id BIGSERIAL PRIMARY KEY,
                                              series_name VARCHAR(100),
    position VARCHAR(10),
    -- Ссылки на компоненты (вместо прямых полей колеса)
    axial_wheel_id BIGINT,
    motor_id BIGINT,
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (axial_wheel_id) REFERENCES axial_wheel_card(id),
    FOREIGN KEY (motor_id) REFERENCES motor_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_axial_fan_series ON axial_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_axial_fan_wheel ON axial_fan_card(axial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_axial_fan_motor ON axial_fan_card(motor_id);
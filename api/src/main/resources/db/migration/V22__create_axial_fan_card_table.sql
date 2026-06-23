-- V22__create_axial_fan_card_table.sql
-- Создание таблицы для осевых вентиляторов

CREATE TABLE IF NOT EXISTS axial_fan_card (
                                              id BIGSERIAL PRIMARY KEY,
                                              series_name VARCHAR(100)DEFAULT 'VO-PatAIR',
    position VARCHAR(10),
    axial_wheel_id BIGINT,
    cable_spec VARCHAR(100),
    fan_class VARCHAR(20),
    axial_wheel_full_marking VARCHAR(200),
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (axial_wheel_id) REFERENCES axial_wheel_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_axial_fan_series ON axial_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_axial_fan_wheel ON axial_fan_card(axial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_axial_fan_class ON axial_fan_card(fan_class);
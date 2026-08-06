-- V23__create_duct_fan_card_table.sql
-- Создание таблицы для канальных вентиляторов

CREATE TABLE IF NOT EXISTS duct_fan_card (
                                             id BIGSERIAL PRIMARY KEY,
                                             series_name VARCHAR(100) DEFAULT 'VRK-PatAIR',
    duct_size VARCHAR(50),
    execution_type VARCHAR(20),
    duct_fan_type VARCHAR(20),
    wheel_size INTEGER,
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_duct_fan_series ON duct_fan_card(series_name);
CREATE INDEX IF NOT EXISTS idx_duct_fan_type ON duct_fan_card(duct_fan_type);
CREATE INDEX IF NOT EXISTS idx_duct_fan_execution ON duct_fan_card(execution_type);

-- V16__create_roof_radial_fan_table.sql
-- Создание таблицы для крышных радиальных вентиляторов

CREATE TABLE IF NOT EXISTS roof_radial_fan (
                                               id BIGSERIAL PRIMARY KEY,
                                               series_name VARCHAR(100) DEFAULT 'VR-PatAIR',
    execution_type VARCHAR(20),
    roof_size VARCHAR(50),
    climate_type VARCHAR(10) DEFAULT 'У1',
    radial_wheel_id BIGINT,
    radial_wheel_full_marking VARCHAR(200),
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE,
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id)
    );

CREATE INDEX IF NOT EXISTS idx_roof_radial_series ON roof_radial_fan(series_name);
CREATE INDEX IF NOT EXISTS idx_roof_radial_execution ON roof_radial_fan(execution_type);
CREATE INDEX IF NOT EXISTS idx_roof_radial_wheel ON roof_radial_fan(radial_wheel_id);
-- V25__create_roof_axial_fan_table.sql
-- Создание таблицы для крышных осевых вентиляторов

CREATE TABLE IF NOT EXISTS roof_axial_fan (
                                              id BIGSERIAL PRIMARY KEY,
                                              series_name VARCHAR(100) DEFAULT 'VA-PatAIR',
    execution_type VARCHAR(20),
    roof_size VARCHAR(50),
    climate_type VARCHAR(10) DEFAULT 'У1',
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

-- Индексы
CREATE INDEX IF NOT EXISTS idx_roof_axial_series ON roof_axial_fan(series_name);
CREATE INDEX IF NOT EXISTS idx_roof_axial_execution ON roof_axial_fan(execution_type);

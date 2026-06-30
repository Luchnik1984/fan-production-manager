-- V26__create_roof_low_profile_fan_table.sql
-- Создание таблицы для крышных низкопрофильных вентиляторов

CREATE TABLE IF NOT EXISTS roof_low_profile_fan (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    series_name VARCHAR(100) DEFAULT 'VR-PatAIR',
    execution_type VARCHAR(20),
    roof_size VARCHAR(50),
    climate_type VARCHAR(10) DEFAULT 'У1',
    FOREIGN KEY (id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_roof_low_profile_series ON roof_low_profile_fan(series_name);
CREATE INDEX IF NOT EXISTS idx_roof_low_profile_execution ON roof_low_profile_fan(execution_type);

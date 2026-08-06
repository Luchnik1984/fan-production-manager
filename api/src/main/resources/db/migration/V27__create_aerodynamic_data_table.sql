-- V27__create_aerodynamic_data_table.sql
-- Создание таблицы для аэродинамических характеристик

CREATE TABLE IF NOT EXISTS aerodynamic_data (
                                                id BIGSERIAL PRIMARY KEY,
                                                fan_card_id BIGINT NOT NULL,
                                                airflow DOUBLE PRECISION,
                                                static_pressure DOUBLE PRECISION,
                                                total_pressure DOUBLE PRECISION,
                                                power_kw DOUBLE PRECISION,
                                                speed_rpm INTEGER,
                                                note VARCHAR(255),
    FOREIGN KEY (fan_card_id) REFERENCES fan_card(id) ON DELETE CASCADE
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_aerodynamic_fan_card ON aerodynamic_data(fan_card_id);
CREATE INDEX IF NOT EXISTS idx_aerodynamic_speed ON aerodynamic_data(speed_rpm);
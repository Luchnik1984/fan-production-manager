-- V6__create_fan_card_table.sql
-- Создание таблицы для общих полей вентиляторов

CREATE TABLE IF NOT EXISTS fan_card (
                                        id BIGSERIAL PRIMARY KEY,

    -- ========== ОБЩИЕ ПОЛЯ ==========
                                        manufacturer VARCHAR(100),
                                        size DOUBLE PRECISION,
                                        series VARCHAR(50),
                                        marking VARCHAR(100),
                                        climate_type VARCHAR(10),
                                        motor_id BIGINT,
                                        motor_wheel_id BIGINT,
                                        radial_wheel_id BIGINT,
                                        axial_wheel_id BIGINT,
                                        hub_type VARCHAR(50),
                                        wheel_formula VARCHAR(100),
                                        wheel_diameter DOUBLE PRECISION,
                                        cable_spec VARCHAR(100),
                                        has_ha BOOLEAN DEFAULT FALSE,
                                        has_ca BOOLEAN DEFAULT FALSE,
                                        certificate_number VARCHAR(100),
                                        fan_class VARCHAR(20),
                                        fan_type VARCHAR(30),
                                        fan_subtype VARCHAR(30),
                                        max_speed_rpm INTEGER,

    -- ========== ЭЛЕКТРИЧЕСКИЕ ПАРАМЕТРЫ ==========
                                        power_kw DOUBLE PRECISION,
                                        poles INTEGER,
                                        voltage INTEGER,
                                        voltage_code VARCHAR(10),
                                        rated_speed_rpm INTEGER,
                                        actual_speed_rpm INTEGER,

    -- ========== ТИП ИЗГОТОВЛЕНИЯ (партнерское/фирменное) ==========
                                        is_partner_production BOOLEAN DEFAULT FALSE,
                                        is_own_production BOOLEAN DEFAULT FALSE,

    -- ========== ИСПОЛНЕНИЕ ПО НАЗНАЧЕНИЮ ==========
                                        general_purpose BOOLEAN DEFAULT TRUE,
                                        fireproof BOOLEAN DEFAULT FALSE,
                                        fireproof_marking VARCHAR(100),
                                        max_temperature INTEGER,
                                        explosion_proof BOOLEAN DEFAULT FALSE,
                                        explosion_marking VARCHAR(100),

    -- ========== ПОЛНАЯ МАРКИРОВКА ==========
                                        full_marking VARCHAR(200),

    -- ========== МАРКИРОВКА КОМПОНЕНТОВ ==========
                                        motor_wheel_full_marking VARCHAR(200),
                                        radial_wheel_full_marking VARCHAR(200),
                                        axial_wheel_full_marking VARCHAR(200),
                                        motor_full_marking VARCHAR(200),

                                        FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE,
                                        FOREIGN KEY (motor_id) REFERENCES motor_card(id)
);

-- ========== ИНДЕКСЫ ==========
CREATE INDEX IF NOT EXISTS idx_fan_card_size ON fan_card(size);
CREATE INDEX IF NOT EXISTS idx_fan_card_motor_id ON fan_card(motor_id);
CREATE INDEX IF NOT EXISTS idx_fan_card_motor_wheel ON fan_card(motor_wheel_id);
CREATE INDEX IF NOT EXISTS idx_fan_card_radial_wheel ON fan_card(radial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_fan_card_axial_wheel ON fan_card(axial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_fan_card_type ON fan_card(fan_type);
CREATE INDEX IF NOT EXISTS idx_fan_card_class ON fan_card(fan_class);
CREATE INDEX IF NOT EXISTS idx_fan_card_poles ON fan_card(poles);
CREATE INDEX IF NOT EXISTS idx_fan_card_series ON fan_card(series);
CREATE INDEX IF NOT EXISTS idx_fan_card_marking ON fan_card(marking);
CREATE INDEX IF NOT EXISTS idx_fan_card_is_partner ON fan_card(is_partner_production);
CREATE INDEX IF NOT EXISTS idx_fan_card_is_own ON fan_card(is_own_production);
CREATE INDEX IF NOT EXISTS idx_fan_card_max_speed ON fan_card(max_speed_rpm);

-- ========== УНИКАЛЬНЫЙ ИНДЕКС ДЛЯ full_marking ==========
CREATE UNIQUE INDEX IF NOT EXISTS idx_fan_card_full_marking ON fan_card(LOWER(full_marking)) WHERE full_marking IS NOT NULL;
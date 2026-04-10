-- V10__create_radial_wheel_card_table.sql
-- Создание таблицы для радиальных колёс

CREATE TABLE IF NOT EXISTS radial_wheel_card (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 manufacturer VARCHAR(100),
    marking VARCHAR(100),
    blade_type VARCHAR(30),
    size DOUBLE PRECISION,
    hub_type VARCHAR(50),
    max_speed_rpm INTEGER,
    weight_kg DOUBLE PRECISION,
    wheel_formula VARCHAR(100),
    front_disk_mod VARCHAR(10),
    blade_mod VARCHAR(50),
    blade_count INTEGER,
    diameter INTEGER,
    width INTEGER,
    -- Специальные исполнения
    general_purpose BOOLEAN DEFAULT TRUE,
    fireproof BOOLEAN DEFAULT FALSE,
    max_temperature INTEGER,
    explosion_proof BOOLEAN DEFAULT FALSE,
    explosion_marking VARCHAR(100),
    full_marking VARCHAR(200),
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_radial_wheel_size ON radial_wheel_card(size);
CREATE INDEX IF NOT EXISTS idx_radial_wheel_marking ON radial_wheel_card(marking);
CREATE INDEX IF NOT EXISTS idx_radial_wheel_blade_type ON radial_wheel_card(blade_type);
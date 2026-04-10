-- V7__create_axial_wheel_card_table.sql
-- Создание таблицы для осевого колеса

CREATE TABLE IF NOT EXISTS axial_wheel_card (
                                                id BIGSERIAL PRIMARY KEY,

    -- Основная информация
                                                manufacturer VARCHAR(100),
    marking VARCHAR(100),

    -- Характеристики колеса
    blade_type VARCHAR(20),
    size DOUBLE PRECISION,
    execution VARCHAR(20),
    trim_coefficient DOUBLE PRECISION DEFAULT 0,

    -- Ступица
    hub_type VARCHAR(50),

    -- Параметры колеса
    blade_count INTEGER,
    blade_slots INTEGER,
    blade_shape VARCHAR(50),
    blade_angle INTEGER,
    blade_material VARCHAR(10),

    -- Расчётные поля
    wheel_diameter INTEGER,
    wheel_formula VARCHAR(200),

    -- Специальные поля
    general_purpose BOOLEAN DEFAULT TRUE,
    fireproof BOOLEAN DEFAULT FALSE,
    max_temperature INTEGER,
    explosion_proof BOOLEAN DEFAULT FALSE,
    explosion_marking VARCHAR(100),
    full_marking VARCHAR(500),

    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_axial_wheel_size ON axial_wheel_card(size);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_marking ON axial_wheel_card(marking);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_blade_type ON axial_wheel_card(blade_type);
-- V19__create_axial_wheel_card_table.sql
-- Создание таблицы для осевого колеса

CREATE TABLE IF NOT EXISTS axial_wheel_card (
                                                id BIGSERIAL PRIMARY KEY,

    -- ========== ОСНОВНАЯ ИНФОРМАЦИЯ ==========
                                                manufacturer VARCHAR(100),
    size DOUBLE PRECISION,
    trim_coefficient DOUBLE PRECISION,
    marking VARCHAR(100),

    -- ========== ТИП КОЛЕСА ==========
    is_partner_wheel BOOLEAN DEFAULT FALSE,
    is_own_production BOOLEAN DEFAULT FALSE,

    -- ========== ТИП ИЗГОТОВЛЕНИЯ ==========
    is_assembled_from_components BOOLEAN DEFAULT FALSE,
    is_welded_from_materials BOOLEAN DEFAULT FALSE,

    -- ========== ХАБ КОЛЕСА ==========
    wheel_hub_type VARCHAR(100),                -- для сварного (вручную)
    wheel_hub_component_id BIGINT,              -- для сборного (ссылка на компонент)
    max_blade_count INTEGER,                    -- максимальное количество лопаток

-- ========== ЛОПАТКА КОЛЕСА ==========
    blade_type VARCHAR(100),                    -- для сварного (вручную)
    blade_component_id BIGINT,                  -- для сборного (ссылка на компонент)
    blade_material VARCHAR(100),                -- материал лопатки
    blade_count INTEGER,                        -- количество установленных лопаток
    blade_angle INTEGER,                        -- угол установки лопаток

-- ========== УСТАНОВОЧНАЯ СТУПИЦА ==========
    hub_component_id BIGINT,                    -- ссылка на компонент "Установочная ступица"

-- ========== РАСЧЁТНЫЕ ПОЛЯ ==========
    wheel_diameter INTEGER,                     -- диаметр колеса (авто)
    wheel_formula VARCHAR(200),                 -- формула колеса (авто)

-- ========== ОБЩИЕ ПОЛЯ ==========
    max_speed_rpm INTEGER,
    weight_kg DOUBLE PRECISION,

    -- ========== ИСПОЛНЕНИЕ ==========
    general_purpose BOOLEAN DEFAULT TRUE,
    fireproof BOOLEAN DEFAULT FALSE,
    fireproof_marking VARCHAR(50),
    max_temperature INTEGER,
    explosion_proof BOOLEAN DEFAULT FALSE,
    explosion_marking VARCHAR(100),

    -- ========== ПОЛНАЯ МАРКИРОВКА ==========
    full_marking VARCHAR(200),

    -- ========== ВНЕШНИЕ КЛЮЧИ ==========
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE,
    FOREIGN KEY (wheel_hub_component_id) REFERENCES component(id) ON DELETE SET NULL,
    FOREIGN KEY (blade_component_id) REFERENCES component(id) ON DELETE SET NULL,
    FOREIGN KEY (hub_component_id) REFERENCES component(id) ON DELETE SET NULL
    );

-- ==========================================================
-- ИНДЕКСЫ
-- ==========================================================

CREATE INDEX IF NOT EXISTS idx_axial_wheel_size ON axial_wheel_card(size);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_full_marking ON axial_wheel_card(full_marking);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_wheel_hub_component ON axial_wheel_card(wheel_hub_component_id);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_blade_component ON axial_wheel_card(blade_component_id);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_hub_component ON axial_wheel_card(hub_component_id);
CREATE INDEX IF NOT EXISTS idx_axial_wheel_blade_count ON axial_wheel_card(blade_count);

-- ==========================================================
-- УНИКАЛЬНЫЙ ИНДЕКС ДЛЯ full_marking (регистронезависимый, частичный)
-- ==========================================================

CREATE UNIQUE INDEX IF NOT EXISTS idx_axial_wheel_full_marking_unique
    ON axial_wheel_card(LOWER(full_marking)) WHERE full_marking IS NOT NULL;
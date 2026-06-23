-- V20__create_radial_wheel_card_table.sql
-- Создание таблицы для радиальных колёс

CREATE TABLE IF NOT EXISTS radial_wheel_card (
                                                 id BIGSERIAL PRIMARY KEY,

    -- ========== РАЗДЕЛ 1: ОСНОВНАЯ ИНФОРМАЦИЯ ==========
                                                 manufacturer VARCHAR(100),          -- Производитель
    series VARCHAR(50),                 -- Серия колеса (КЦ, РК) - НОВОЕ ПОЛЕ
    size DOUBLE PRECISION,              -- Размер колеса (220, 560)
    marking VARCHAR(100),               -- Маркировка колеса (КЦ-220) - ОБЯЗАТЕЛЬНОЕ

-- ========== РАЗДЕЛ 2: ХАРАКТЕРИСТИКИ КОЛЕСА ==========
    blade_type VARCHAR(30),             -- Тип лопаток (V, N, RO)
    hub_component_id BIGINT,            -- Ссылка на компонент "Ступица"
    max_speed_rpm INTEGER,              -- Максимальная скорость вращения
    weight_kg DOUBLE PRECISION,         -- Масса

-- ========== РАЗДЕЛ 3: ДОПОЛНИТЕЛЬНЫЕ ПАРАМЕТРЫ ==========
    wheel_formula VARCHAR(200),         -- Формула колеса (N.14/B.027/6/1.03)
    wheel_code VARCHAR(50),             -- Код колеса (14, 12U)
    blade_mod VARCHAR(50),              -- Модификация лопатки (14, 12U)
    front_disk_mod VARCHAR(10),         -- Модификация переднего диска (A, B)
    wheel_width DOUBLE PRECISION,       -- Ширина колеса (0.27, 0.33)
    blade_length_coeff DOUBLE PRECISION,-- Коэф. длины лопатки (1.03, 1.00, 0.97)
    blade_count INTEGER,                -- Количество лопаток (6, 7, 9)
    diameter INTEGER,                   -- Максимальный диаметр колеса (справочно)

-- ========== РАЗДЕЛ 4: ИСПОЛНЕНИЕ ==========
    general_purpose BOOLEAN DEFAULT TRUE,
    fireproof BOOLEAN DEFAULT FALSE,
    fireproof_marking VARCHAR(50),
    max_temperature INTEGER,
    explosion_proof BOOLEAN DEFAULT FALSE,
    explosion_marking VARCHAR(100),

    -- ========== ПОЛНАЯ МАРКИРОВКА ==========
    full_marking VARCHAR(500),

    -- ========== ВНЕШНИЕ КЛЮЧИ ==========
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE,
    FOREIGN KEY (hub_component_id) REFERENCES component(id) ON DELETE SET NULL
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_radial_wheel_series ON radial_wheel_card(series);
CREATE INDEX IF NOT EXISTS idx_radial_wheel_marking ON radial_wheel_card(marking);
CREATE INDEX IF NOT EXISTS idx_radial_wheel_size ON radial_wheel_card(size);
CREATE INDEX IF NOT EXISTS idx_radial_wheel_blade_type ON radial_wheel_card(blade_type);
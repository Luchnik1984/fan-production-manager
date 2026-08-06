-- V17__create_accessory_card_table.sql
-- Создание таблицы для комплектующих

CREATE TABLE IF NOT EXISTS accessory_card (
                                              id BIGSERIAL PRIMARY KEY,
                                              accessory_type VARCHAR(50),
    compatible_models VARCHAR(500),
    vendor_code VARCHAR(50),
    unit VARCHAR(20),
    price DOUBLE PRECISION,
    -- Специальные поля (самостоятельная продукция)
    general_purpose BOOLEAN DEFAULT TRUE,
    fireproof BOOLEAN DEFAULT FALSE,
    max_temperature INTEGER,
    explosion_proof BOOLEAN DEFAULT FALSE,
    explosion_marking VARCHAR(100),
    full_marking VARCHAR(200),
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_accessory_type ON accessory_card(accessory_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_accessory_full_marking ON accessory_card(LOWER(full_marking)) WHERE full_marking IS NOT NULL;
-- V11__create_accessory_card_table.sql
-- Создание таблицы для комплектующих

CREATE TABLE IF NOT EXISTS accessory_card (
                                              id BIGSERIAL PRIMARY KEY,
                                              accessory_type VARCHAR(50),
    compatible_models VARCHAR(500),
    vendor_code VARCHAR(50),
    unit VARCHAR(20),
    price DOUBLE PRECISION,
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_accessory_type ON accessory_card(accessory_type);
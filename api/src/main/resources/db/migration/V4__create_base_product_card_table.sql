-- V4__create_base_product_card_table.sql
-- Создание базовой таблицы для карточек продукции

CREATE TABLE IF NOT EXISTS base_product_card (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 name VARCHAR(200) NOT NULL,
    code VARCHAR(50) UNIQUE,
    card_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by VARCHAR(100),
    is_temporary BOOLEAN NOT NULL DEFAULT FALSE
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_base_product_card_type ON base_product_card(card_type);
CREATE INDEX IF NOT EXISTS idx_base_product_card_name ON base_product_card(name);
CREATE INDEX IF NOT EXISTS idx_base_product_card_code ON base_product_card(code);
CREATE INDEX IF NOT EXISTS idx_base_product_card_temporary ON base_product_card(is_temporary) WHERE is_temporary = TRUE;
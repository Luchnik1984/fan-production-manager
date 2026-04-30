-- V24__create_product_component_table.sql
-- Создание таблицы связи продукции с компонентами
CREATE TABLE IF NOT EXISTS product_component (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 product_card_id BIGINT NOT NULL REFERENCES base_product_card(id) ON DELETE CASCADE,
    component_id BIGINT NOT NULL REFERENCES component(id) ON DELETE CASCADE,
    quantity DOUBLE PRECISION DEFAULT 1.0,
    note VARCHAR(255)
    );

CREATE UNIQUE INDEX IF NOT EXISTS idx_product_component_unique
    ON product_component(product_card_id, component_id);

CREATE INDEX IF NOT EXISTS idx_product_component_product_card
    ON product_component(product_card_id);

CREATE INDEX IF NOT EXISTS idx_product_component_component
    ON product_component(component_id);
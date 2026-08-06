-- V11__create_product_component_table.sql
CREATE TABLE IF NOT EXISTS product_component (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 product_card_id BIGINT NOT NULL REFERENCES base_product_card(id) ON DELETE CASCADE,
    component_id BIGINT NOT NULL REFERENCES component(id) ON DELETE CASCADE,
    quantity DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    position VARCHAR(50),
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    UNIQUE(product_card_id, component_id)
    );

CREATE INDEX IF NOT EXISTS idx_product_component_product_card ON product_component(product_card_id);
CREATE INDEX IF NOT EXISTS idx_product_component_component ON product_component(component_id);
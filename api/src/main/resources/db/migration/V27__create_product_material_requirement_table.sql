-- V27__create_product_material_requirement_table.sql
CREATE TABLE IF NOT EXISTS product_material_requirement (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            product_card_id BIGINT NOT NULL REFERENCES base_product_card(id) ON DELETE CASCADE,
    material_id BIGINT NOT NULL REFERENCES material(id) ON DELETE CASCADE,
    quantity_per_unit DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100),
    UNIQUE(product_card_id, material_id)
    );

CREATE INDEX IF NOT EXISTS idx_product_material_product_card ON product_material_requirement(product_card_id);
CREATE INDEX IF NOT EXISTS idx_product_material_material ON product_material_requirement(material_id);
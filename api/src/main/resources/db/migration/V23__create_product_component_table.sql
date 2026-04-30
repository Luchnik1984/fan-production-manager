-- V23__create_component_table.sql
CREATE TABLE IF NOT EXISTS component (
                                         id BIGSERIAL PRIMARY KEY,
                                         class_id BIGINT NOT NULL REFERENCES component_class(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    vendor_code VARCHAR(100),
    unit_id BIGINT NOT NULL REFERENCES unit_of_measure(id),
    quantity_per_unit DOUBLE PRECISION DEFAULT 1.0,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

-- Индексы для component
CREATE INDEX IF NOT EXISTS idx_component_class_id ON component(class_id);
CREATE INDEX IF NOT EXISTS idx_component_name ON component(name);
CREATE INDEX IF NOT EXISTS idx_component_unit_id ON component(unit_id);
CREATE INDEX IF NOT EXISTS idx_component_class_name ON component(class_id, name);
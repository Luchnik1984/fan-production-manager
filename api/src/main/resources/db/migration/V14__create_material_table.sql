-- V14__create_material_table.sql
CREATE TABLE IF NOT EXISTS material (
                                        id BIGSERIAL PRIMARY KEY,
                                        class_id BIGINT NOT NULL REFERENCES material_class(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    designation VARCHAR(100) NOT NULL,
    standard VARCHAR(100),
    specification VARCHAR(200),
    material_type VARCHAR(50),
    unit_id BIGINT NOT NULL REFERENCES unit_of_measure(id),
    density DOUBLE PRECISION,
    vendor_code VARCHAR(100),
    min_order DOUBLE PRECISION,
    description VARCHAR(500),
    technical_specs JSONB,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_material_class_id ON material(class_id);
CREATE INDEX IF NOT EXISTS idx_material_name ON material(name);
CREATE INDEX IF NOT EXISTS idx_material_designation ON material(designation);
CREATE UNIQUE INDEX IF NOT EXISTS idx_material_designation_unique ON material(designation);
CREATE INDEX IF NOT EXISTS idx_material_standard ON material(standard);
CREATE INDEX IF NOT EXISTS idx_material_unit_id ON material(unit_id);
CREATE INDEX IF NOT EXISTS idx_material_type ON material(material_type);
CREATE UNIQUE INDEX IF NOT EXISTS idx_material_vendor_code_unique ON material(vendor_code);
CREATE UNIQUE INDEX IF NOT EXISTS idx_material_unique ON material(class_id, name, standard, specification);
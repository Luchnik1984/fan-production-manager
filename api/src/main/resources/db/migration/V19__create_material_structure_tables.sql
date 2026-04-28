-- V19__create_material_structure_tables.sql
-- Создание иерархической структуры материалов

-- 1. Категории материалов
CREATE TABLE IF NOT EXISTS material_category (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 parent_id BIGINT,
                                                 name VARCHAR(100) NOT NULL,
    level INTEGER,
    path VARCHAR(500),
    sort_order INTEGER,
    FOREIGN KEY (parent_id) REFERENCES material_category(id) ON DELETE CASCADE
    );

-- 2. Конкретные материалы
CREATE TABLE IF NOT EXISTS material (
                                        id BIGSERIAL PRIMARY KEY,
                                        category_id BIGINT NOT NULL,
                                        name VARCHAR(200) NOT NULL,
    standard VARCHAR(100),
    specification VARCHAR(200),
    material_type VARCHAR(50),
    unit VARCHAR(20),
    density DOUBLE PRECISION,
    vendor_code VARCHAR(100),
    min_order DOUBLE PRECISION,
    description VARCHAR(500),
    FOREIGN KEY (category_id) REFERENCES material_category(id) ON DELETE CASCADE
    );

-- 3. Связь продукции с материалами
CREATE TABLE IF NOT EXISTS product_material_requirement (
                                                            id BIGSERIAL PRIMARY KEY,
                                                            product_card_id BIGINT NOT NULL,
                                                            material_id BIGINT NOT NULL,
                                                            quantity_per_unit DOUBLE PRECISION,
                                                            unit VARCHAR(20),
    note VARCHAR(255),
    FOREIGN KEY (product_card_id) REFERENCES base_product_card(id) ON DELETE CASCADE,
    FOREIGN KEY (material_id) REFERENCES material(id) ON DELETE CASCADE
    );

-- Индексы для быстрого поиска
CREATE INDEX IF NOT EXISTS idx_material_category_parent ON material_category(parent_id);
CREATE INDEX IF NOT EXISTS idx_material_category_path ON material_category(path);
CREATE INDEX IF NOT EXISTS idx_material_category_name ON material_category(name);
CREATE INDEX IF NOT EXISTS idx_material_category_id ON material(category_id);
CREATE INDEX IF NOT EXISTS idx_material_name ON material(name);
CREATE INDEX IF NOT EXISTS idx_material_type ON material(material_type);
CREATE INDEX IF NOT EXISTS idx_product_material_product_card ON product_material_requirement(product_card_id);
CREATE INDEX IF NOT EXISTS idx_product_material_material ON product_material_requirement(material_id);
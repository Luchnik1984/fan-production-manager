-- V8__create_component_category_table.sql
CREATE TABLE IF NOT EXISTS component_category (
                                                  id BIGSERIAL PRIMARY KEY,
                                                  parent_id BIGINT REFERENCES component_category(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    path VARCHAR(500),
    sort_order INT DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_component_category_parent ON component_category(parent_id);
CREATE INDEX IF NOT EXISTS idx_component_category_path ON component_category(path);
CREATE INDEX IF NOT EXISTS idx_component_category_level ON component_category(level);

-- Корневые категории
INSERT INTO component_category (name, level, path, sort_order, created_at, created_by) VALUES
                                                                                           ('Ступицы и втулки', 1, '/Ступицы и втулки/', 1, NOW(), 'system'),
                                                                                           ('Электрические компоненты', 1, '/Электрические компоненты/', 2, NOW(), 'system'),
                                                                                           ('Кабельные вводы', 1, '/Кабельные вводы/', 3, NOW(), 'system'),
                                                                                           ('Муфты', 1, '/Муфты/', 4, NOW(), 'system');

-- Подкатегории для "Ступицы и втулки"
INSERT INTO component_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Ступицы болтовые', 2, CONCAT(path, 'Ступицы болтовые/'), 1, NOW(), 'system'
FROM component_category WHERE name = 'Ступицы и втулки';

INSERT INTO component_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Втулки конические', 2, CONCAT(path, 'Втулки конические/'), 2, NOW(), 'system'
FROM component_category WHERE name = 'Ступицы и втулки';

-- Подкатегории для "Электрические компоненты"
INSERT INTO component_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Клеммные коробки', 2, CONCAT(path, 'Клеммные коробки/'), 1, NOW(), 'system'
FROM component_category WHERE name = 'Электрические компоненты';
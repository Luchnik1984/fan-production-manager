-- V12__create_material_category_table.sql
CREATE TABLE IF NOT EXISTS material_category (
                                                 id BIGSERIAL PRIMARY KEY,
                                                 parent_id BIGINT REFERENCES material_category(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    level INT NOT NULL DEFAULT 1,
    path VARCHAR(500),
    sort_order INT DEFAULT 0,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_material_category_parent ON material_category(parent_id);
CREATE INDEX IF NOT EXISTS idx_material_category_path ON material_category(path);
CREATE INDEX IF NOT EXISTS idx_material_category_level ON material_category(level);
CREATE INDEX IF NOT EXISTS idx_material_category_name ON material_category(name);

INSERT INTO material_category (name, level, path, sort_order, created_at, created_by) VALUES
                                                                                          ('Метизы', 1, '/Метизы/', 1, NOW(), 'system'),
                                                                                          ('Металлопрокат', 1, '/Металлопрокат/', 2, NOW(), 'system'),
                                                                                          ('Краски и покрытия', 1, '/Краски и покрытия/', 3, NOW(), 'system'),
                                                                                          ('Упаковка', 1, '/Упаковка/', 4, NOW(), 'system');

INSERT INTO material_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Болты', 2, CONCAT(path, 'Болты/'), 1, NOW(), 'system'
FROM material_category WHERE name = 'Метизы';

INSERT INTO material_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Гайки', 2, CONCAT(path, 'Гайки/'), 2, NOW(), 'system'
FROM material_category WHERE name = 'Метизы';

INSERT INTO material_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Шайбы', 2, CONCAT(path, 'Шайбы/'), 3, NOW(), 'system'
FROM material_category WHERE name = 'Метизы';

INSERT INTO material_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Листовой прокат', 2, CONCAT(path, 'Листовой прокат/'), 1, NOW(), 'system'
FROM material_category WHERE name = 'Металлопрокат';

INSERT INTO material_category (parent_id, name, level, path, sort_order, created_at, created_by)
SELECT id, 'Сортовой прокат', 2, CONCAT(path, 'Сортовой прокат/'), 2, NOW(), 'system'
FROM material_category WHERE name = 'Металлопрокат';
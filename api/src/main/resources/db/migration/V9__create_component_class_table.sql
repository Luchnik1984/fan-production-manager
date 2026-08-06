-- V9__create_component_class_table.sql
CREATE TABLE IF NOT EXISTS component_class (
                                               id BIGSERIAL PRIMARY KEY,
                                               category_id BIGINT NOT NULL REFERENCES component_category(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    unit_id BIGINT REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_component_class_category ON component_class(category_id);
CREATE INDEX IF NOT EXISTS idx_component_class_name ON component_class(name);
CREATE INDEX IF NOT EXISTS idx_component_class_unit ON component_class(unit_id);

INSERT INTO component_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Ступицы с цилиндрической посадкой', 'Для валов 20-40 мм', u.id, NOW(), 'system'
FROM component_category c, unit_of_measure u
WHERE c.name = 'Ступицы болтовые' AND u.code = 'шт';

INSERT INTO component_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Ступицы с конической посадкой', 'Для валов 25-50 мм', u.id, NOW(), 'system'
FROM component_category c, unit_of_measure u
WHERE c.name = 'Ступицы болтовые' AND u.code = 'шт';

INSERT INTO component_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Втулки разрезные', 'С замком', u.id, NOW(), 'system'
FROM component_category c, unit_of_measure u
WHERE c.name = 'Втулки конические' AND u.code = 'шт';

INSERT INTO component_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Клеммные коробки универсальные', 'IP65, пластик', u.id, NOW(), 'system'
FROM component_category c, unit_of_measure u
WHERE c.name = 'Клеммные коробки' AND u.code = 'шт';
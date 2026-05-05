-- V25__create_material_class_table.sql
CREATE TABLE IF NOT EXISTS material_class (
                                              id BIGSERIAL PRIMARY KEY,
                                              category_id BIGINT NOT NULL REFERENCES material_category(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    unit_id BIGINT REFERENCES unit_of_measure(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_material_class_category ON material_class(category_id);
CREATE INDEX IF NOT EXISTS idx_material_class_name ON material_class(name);
CREATE INDEX IF NOT EXISTS idx_material_class_unit ON material_class(unit_id);

INSERT INTO material_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Болты с шестигранной головкой', 'ГОСТ 7798-70, класс точности B', u.id, NOW(), 'system'
FROM material_category c, unit_of_measure u
WHERE c.name = 'Болты' AND u.code = 'шт';

INSERT INTO material_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Болты с цилиндрической головкой', 'ГОСТ 7805-70', u.id, NOW(), 'system'
FROM material_category c, unit_of_measure u
WHERE c.name = 'Болты' AND u.code = 'шт';

INSERT INTO material_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Холоднокатаный лист', 'Толщина до 3 мм', u.id, NOW(), 'system'
FROM material_category c, unit_of_measure u
WHERE c.name = 'Листовой прокат' AND u.code = 'кг';

INSERT INTO material_class (category_id, name, description, unit_id, created_at, created_by)
SELECT c.id, 'Горячекатаный лист', 'Толщина от 3 мм', u.id, NOW(), 'system'
FROM material_category c, unit_of_measure u
WHERE c.name = 'Листовой прокат' AND u.code = 'kg';
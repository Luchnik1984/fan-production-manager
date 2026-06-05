-- V28__insert_sample_data.sql
-- ============================================
-- КОМПОНЕНТЫ
-- ============================================

INSERT INTO component (class_id, name, designation, vendor_code, unit_id, description, weight_kg, material, created_at, created_by)
SELECT cl.id, 'Ступица болтовая', 'SM1210', 'SM1210', u.id, 'Ступица для вала 20 мм', 0.45, 'Чугун СЧ20', NOW(), 'system'
FROM component_class cl, unit_of_measure u
WHERE cl.name = 'Ступицы с цилиндрической посадкой' AND u.code = 'шт';

INSERT INTO component (class_id, name, designation, vendor_code, unit_id, description, weight_kg, material, created_at, created_by)
SELECT cl.id, 'Втулка коническая', '1210х24', 'BSH-1210-24', u.id, 'Коническая втулка диаметр 24 мм', 0.12, 'Сталь 45', NOW(), 'system'
FROM component_class cl, unit_of_measure u
WHERE cl.name = 'Втулки разрезные' AND u.code = 'шт';

INSERT INTO component (class_id, name, designation, vendor_code, unit_id, description, technical_specs, material, created_at, created_by)
SELECT cl.id, 'Клеммная коробка', 'FS 100x100x55', 'BOX-FS-100', u.id, 'Клеммная коробка IP65',
       '{"ip_rating": "IP65", "dimensions": "100x100x55", "material": "пластик"}'::jsonb, 'Пластик ABS', NOW(), 'system'
FROM component_class cl, unit_of_measure u
WHERE cl.name = 'Клеммные коробки универсальные' AND u.code = 'шт';

-- ============================================
-- МАТЕРИАЛЫ
-- ============================================

INSERT INTO material (class_id, name, standard, specification, material_type, unit_id, description, created_at, created_by)
SELECT cl.id, 'Болт М6х12', 'ГОСТ 7798-70', 'M6-6gx12', 'крепёж', u.id, 'Болт с шестигранной головкой, класс точности B', NOW(), 'system'
FROM material_class cl, unit_of_measure u
WHERE cl.name = 'Болты с шестигранной головкой' AND u.code = 'шт';

INSERT INTO material (class_id, name, standard, specification, material_type, unit_id, density, description, created_at, created_by)
SELECT cl.id, 'Лист оцинкованный 08пс', 'ГОСТ 14918-80', '1,0 мм', 'металл', u.id, 7850, 'Лист оцинкованный, толщина 1,0 мм', NOW(), 'system'
FROM material_class cl, unit_of_measure u
WHERE cl.name = 'Холоднокатаный лист' AND u.code = 'кг';
-- V25__insert_sample_components.sql
-- Классы компонентов
INSERT INTO component_class (name, description, created_at, created_by) VALUES
                                                                            ('Кабельные вводы', 'Устройства для ввода кабеля в корпус', NOW(), 'system'),
                                                                            ('Ступицы', 'Элементы для крепления колеса к валу', NOW(), 'system'),
                                                                            ('Втулки', 'Переходные элементы для посадки', NOW(), 'system'),
                                                                            ('Муфты', 'Соединительные элементы', NOW(), 'system'),
                                                                            ('Кабели', 'Электрические кабели и провода', NOW(), 'system'),
                                                                            ('Клеммные коробки', 'Распределительные коробки', NOW(), 'system');

-- Кабельные вводы (единица: шт)
INSERT INTO component (class_id, name, vendor_code, unit_id, description, created_at, created_by)
SELECT c.id, 'PG21', 'CBL-PG21', u.id, 'Кабельный ввод PG21, IP68', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Кабельные вводы' AND u.code = 'шт';

INSERT INTO component (class_id, name, vendor_code, unit_id, description, created_at, created_by)
SELECT c.id, 'PG16', 'CBL-PG16', u.id, 'Кабельный ввод PG16, IP68', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Кабельные вводы' AND u.code = 'шт';

-- Ступицы (единица: шт)
INSERT INTO component (class_id, name, vendor_code, unit_id, description, created_at, created_by)
SELECT c.id, 'SM1610', 'HUB-SM1610', u.id, 'Ступица SM1610', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Ступицы' AND u.code = 'шт';

INSERT INTO component (class_id, name, vendor_code, unit_id, description, created_at, created_by)
SELECT c.id, 'BF2012', 'HUB-BF2012', u.id, 'Ступица BF2012', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Ступицы' AND u.code = 'шт';

-- Кабели (единица: м)
INSERT INTO component (class_id, name, vendor_code, unit_id, quantity_per_unit, description, created_at, created_by)
SELECT c.id, 'КГВВнг(А) 4х2,5', 'CBL-KGVV-4x2.5', u.id, 1.0, 'Кабель контрольный, 4 жилы по 2,5 мм²', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Кабели' AND u.code = 'м';

INSERT INTO component (class_id, name, vendor_code, unit_id, quantity_per_unit, description, created_at, created_by)
SELECT c.id, 'ВВГнг(А) 3х1,5', 'CBL-VVG-3x1.5', u.id, 1.0, 'Кабель силовой, 3 жилы по 1,5 мм²', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Кабели' AND u.code = 'м';

-- Клеммная коробка (единица: шт)
INSERT INTO component (class_id, name, vendor_code, unit_id, description, created_at, created_by)
SELECT c.id, 'КМ-100х75х50', 'BOX-KM-100', u.id, 'Клеммная коробка IP65 100x75x50', NOW(), 'system'
FROM component_class c, unit_of_measure u
WHERE c.name = 'Клеммные коробки' AND u.code = 'шт';
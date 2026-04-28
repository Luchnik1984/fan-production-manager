-- V20__insert_sample_materials.sql
-- Добавление тестовых категорий и материалов

-- Категории
INSERT INTO material_category (parent_id, name, level, path, sort_order) VALUES
                                                                             (NULL, 'Метизы', 1, '/Метизы/', 1),
                                                                             (NULL, 'Металл', 1, '/Металл/', 2),
                                                                             (NULL, 'Краски и покрытия', 1, '/Краски и покрытия/', 3);

-- Подкатегория: Болты (родитель Метизы)
INSERT INTO material_category (parent_id, name, level, path, sort_order)
SELECT id, 'Болты', 2, CONCAT(path, 'Болты/'), 1
FROM material_category WHERE name = 'Метизы';

-- Подкатегория: ГОСТ 7798-70 (родитель Болты)
INSERT INTO material_category (parent_id, name, level, path, sort_order)
SELECT id, 'ГОСТ 7798-70', 3, CONCAT(path, 'ГОСТ 7798-70/'), 1
FROM material_category WHERE name = 'Болты';

-- Подкатегория: Листовой (родитель Металл)
INSERT INTO material_category (parent_id, name, level, path, sort_order)
SELECT id, 'Листовой', 2, CONCAT(path, 'Листовой/'), 1
FROM material_category WHERE name = 'Металл';

-- Подкатегория: Оцинкованный (родитель Листовой)
INSERT INTO material_category (parent_id, name, level, path, sort_order)
SELECT id, 'Оцинкованный', 3, CONCAT(path, 'Оцинкованный/'), 1
FROM material_category WHERE name = 'Листовой';

-- Материалы
INSERT INTO material (category_id, name, standard, specification, material_type, unit, density) VALUES
                                                                                                    ((SELECT id FROM material_category WHERE name = 'ГОСТ 7798-70'),
                                                                                                     'Болт ГОСТ 7798-70', 'ГОСТ 7798-70', 'M6-6gx50', 'крепёж', 'шт', NULL),

                                                                                                    ((SELECT id FROM material_category WHERE name = 'ГОСТ 7798-70'),
                                                                                                     'Болт ГОСТ 7798-70', 'ГОСТ 7798-70', 'M8-6gx60', 'крепёж', 'шт', NULL),

                                                                                                    ((SELECT id FROM material_category WHERE name = 'Оцинкованный'),
                                                                                                     'Лист оцинкованный', 'ГОСТ 14918-80', '08пс, Б-ПН-О-1,0', 'металл', 'кг', 7850),

                                                                                                    ((SELECT id FROM material_category WHERE name = 'Оцинкованный'),
                                                                                                     'Лист оцинкованный', 'ГОСТ 14918-80', '08пс, Б-ПН-О-1,5', 'металл', 'кг', 7850);
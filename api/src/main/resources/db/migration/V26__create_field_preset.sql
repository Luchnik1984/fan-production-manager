-- ============================================================================
-- V26__create_field_preset.sql
-- US10.6: пресеты полей шаблона.
--
-- Пресет — именованный набор определений полей, который применяется
-- к DRAFT-версии шаблона кнопкой «Применить пресет».
-- Формат fields_json совпадает с product_template_version.fields_json,
-- поэтому пресет можно применять и сохранять без конвертаций.
--
-- Системные пресеты (is_system = TRUE) нельзя изменить или удалить.
-- ============================================================================

CREATE TABLE IF NOT EXISTS field_preset
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    fields_json JSONB        NOT NULL DEFAULT '[]'::jsonb,
    is_system   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by  VARCHAR(100)
);

-- Имя пресета уникально (регистронезависимо)
CREATE UNIQUE INDEX IF NOT EXISTS idx_field_preset_name_unique
    ON field_preset (LOWER(name));

COMMENT ON TABLE field_preset IS 'Пресеты полей шаблона (готовые наборы полей для типовых изделий)';
COMMENT ON COLUMN field_preset.fields_json IS 'Массив FieldDefinition в формате product_template_version.fields_json';
COMMENT ON COLUMN field_preset.is_system IS 'Системный пресет: нельзя изменить или удалить';

-- ============================================================================
-- Системные пресеты
-- ============================================================================
INSERT INTO field_preset (name, description, fields_json, is_system, created_at, created_by)
VALUES
    ('Осевое колесо', 'Поля карточки осевого колеса (из хардкод-типа AXIAL_WHEEL)', '[{"key":"manufacturer","label":"Производитель","type":"TEXT","required":false,"displayOrder":1},{"key":"series","label":"Серия колеса","type":"TEXT","required":false,"displayOrder":2},{"key":"size","label":"Типоразмер","type":"DOUBLE","required":false,"displayOrder":3},{"key":"marking","label":"Маркировка","type":"TEXT","required":false,"displayOrder":4},{"key":"trimCoefficient","label":"Коэф. подрезки (%)","type":"DOUBLE","required":false,"displayOrder":5},{"key":"bladeType","label":"Тип лопатки","type":"TEXT","required":false,"displayOrder":6},{"key":"bladeCount","label":"Количество лопаток","type":"NUMBER","required":false,"displayOrder":7},{"key":"bladeAngle","label":"Угол установки","type":"NUMBER","required":false,"displayOrder":8},{"key":"bladeMaterial","label":"Материал лопатки","type":"TEXT","required":false,"displayOrder":9},{"key":"maxBladeCount","label":"Макс. кол-во лопаток","type":"NUMBER","required":false,"displayOrder":10},{"key":"wheelDiameter","label":"Диаметр колеса (мм)","type":"NUMBER","required":false,"displayOrder":11},{"key":"wheelFormula","label":"Формула колеса","type":"TEXT","required":false,"displayOrder":12},{"key":"maxSpeedRpm","label":"Максимальная скорость (об/мин)","type":"NUMBER","required":false,"displayOrder":13},{"key":"weightKg","label":"Масса (кг)","type":"DOUBLE","required":false,"displayOrder":14},{"key":"fullMarking","label":"Полная маркировка","type":"TEXT","required":false,"displayOrder":15}]'::jsonb, TRUE, NOW(), 'system'),
    ('Радиальное колесо', 'Поля карточки радиального колеса (из хардкод-типа RADIAL_WHEEL)', '[{"key":"manufacturer","label":"Производитель","type":"TEXT","required":false,"displayOrder":1},{"key":"series","label":"Серия колеса","type":"TEXT","required":false,"displayOrder":2},{"key":"size","label":"Размер колеса (мм)","type":"DOUBLE","required":false,"displayOrder":3},{"key":"marking","label":"Маркировка","type":"TEXT","required":false,"displayOrder":4},{"key":"bladeType","label":"Тип лопаток","type":"TEXT","required":false,"displayOrder":5},{"key":"bladeMod","label":"Модификация лопатки","type":"TEXT","required":false,"displayOrder":6},{"key":"frontDiskMod","label":"Модификация переднего диска","type":"TEXT","required":false,"displayOrder":7},{"key":"wheelWidth","label":"Ширина колеса","type":"DOUBLE","required":false,"displayOrder":8},{"key":"bladeCount","label":"Количество лопаток","type":"NUMBER","required":false,"displayOrder":9},{"key":"bladeLengthCoeff","label":"Коэф. длины лопатки","type":"DOUBLE","required":false,"displayOrder":10},{"key":"wheelCode","label":"Код колеса","type":"TEXT","required":false,"displayOrder":11},{"key":"wheelFormula","label":"Формула колеса","type":"TEXT","required":false,"displayOrder":12},{"key":"diameter","label":"Диаметр (мм)","type":"NUMBER","required":false,"displayOrder":13},{"key":"maxSpeedRpm","label":"Максимальная скорость (об/мин)","type":"NUMBER","required":false,"displayOrder":14},{"key":"weightKg","label":"Масса (кг)","type":"DOUBLE","required":false,"displayOrder":15},{"key":"fullMarking","label":"Полная маркировка","type":"TEXT","required":false,"displayOrder":16}]'::jsonb, TRUE, NOW(), 'system'),
    ('Электродвигатель AC', 'Поля карточки электродвигателя (AC, IEC 60034-1)', '[{"key":"series","label":"Серия","type":"TEXT","required":false,"displayOrder":1},{"key":"motorType","label":"Тип двигателя","type":"TEXT","required":false,"displayOrder":2},{"key":"poles","label":"Количество полюсов","type":"NUMBER","required":false,"displayOrder":3},{"key":"powerKw","label":"Мощность (кВт)","type":"DOUBLE","required":false,"displayOrder":4},{"key":"ratedSpeedRpm","label":"Номинальная скорость (об/мин)","type":"NUMBER","required":false,"displayOrder":5},{"key":"actualSpeedRpm","label":"Фактическая скорость (об/мин)","type":"NUMBER","required":false,"displayOrder":6},{"key":"shaftSize","label":"Размер вала (мм)","type":"NUMBER","required":false,"displayOrder":7},{"key":"mountingType","label":"Исполнение по монтажу","type":"TEXT","required":false,"displayOrder":8},{"key":"climateType","label":"Климатическое исполнение","type":"TEXT","required":false,"displayOrder":9},{"key":"voltage","label":"Напряжение (В)","type":"NUMBER","required":false,"displayOrder":10},{"key":"operationMode","label":"Режим работы","type":"TEXT","required":false,"displayOrder":11},{"key":"weightKg","label":"Масса (кг)","type":"DOUBLE","required":false,"displayOrder":12},{"key":"fullMarking","label":"Полная маркировка","type":"TEXT","required":false,"displayOrder":13}]'::jsonb, TRUE, NOW(), 'system');

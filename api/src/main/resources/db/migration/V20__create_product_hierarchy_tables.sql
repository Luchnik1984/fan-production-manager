-- ============================================================================
-- V20__create_product_hierarchy_tables.sql
-- Иерархия продукции: тип → вид → серия.
--
-- Все три таблицы — «тонкие» справочники без бизнес-логики.
-- Тип привязан к разделу (product_section) и несёт атрибуты, которые
-- используют журнал (US11) и генерация документов (US13):
--   code_prefix         — префикс номера изделия (ED, MW, RW, AW, VRK);
--   system_card_type    — для 4 хардкод-типов, NULL для динамических;
--   journal_number_rule — правило нумерации журнала (JSONB);
--   journal_columns     — набор столбцов журнала для типа (JSONB).
-- ============================================================================

-- ============================================================================
-- Таблица product_type — типы продукции (верхний уровень иерархии)
-- Пример: «Вентилятор канальный (VRK-PatAIR)»
-- ============================================================================
CREATE TABLE product_type
(
    id                  BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200),                   -- «Вентилятор канальный»
    designation         VARCHAR(50) NOT NULL,           -- «VRK-PatAIR» или «ED»
    is_system           BOOLEAN     NOT NULL DEFAULT FALSE,
    section_id          BIGINT,
    code_prefix         VARCHAR(4),                     -- ED, MW, RW, AW, VRK
    system_card_type    VARCHAR(30),                    -- MOTOR, MOTOR_WHEEL, ...
    journal_number_rule JSONB,
    journal_columns     JSONB,
    created_at          TIMESTAMP   NOT NULL,
    created_by          VARCHAR(100),

    CONSTRAINT fk_product_type_section
        FOREIGN KEY (section_id) REFERENCES product_section (id) ON DELETE SET NULL,

    CONSTRAINT chk_product_type_system_card_type
        CHECK (system_card_type IS NULL OR system_card_type IN
                                             ('MOTOR', 'MOTOR_WHEEL', 'RADIAL_WHEEL', 'AXIAL_WHEEL'))
);

-- Обозначение типа уникально (регистронезависимо)
CREATE UNIQUE INDEX idx_product_type_designation_unique
    ON product_type (LOWER(designation));

-- Префикс номера изделия уникален (NULL-значения не конфликтуют между собой)
CREATE UNIQUE INDEX idx_product_type_code_prefix_unique
    ON product_type (code_prefix);

CREATE INDEX idx_product_type_name ON product_type (name);
CREATE INDEX idx_product_type_section_id ON product_type (section_id);

COMMENT ON TABLE product_type IS 'Типы продукции (верхний уровень иерархии)';
COMMENT ON COLUMN product_type.name IS 'Наименование типа, например «Вентилятор канальный»';
COMMENT ON COLUMN product_type.designation IS 'Обозначение типа, например «VRK-PatAIR» или «ED» (уникальное)';
COMMENT ON COLUMN product_type.is_system IS 'Системный тип — нельзя удалять';
COMMENT ON COLUMN product_type.section_id IS 'Ссылка на раздел продукции';
COMMENT ON COLUMN product_type.code_prefix IS 'Префикс уникального номера изделия: ED, MW, RW, AW, VRK (VARCHAR(4), уникальный)';
COMMENT ON COLUMN product_type.system_card_type IS 'Хардкод-тип: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL; NULL для динамических';
COMMENT ON COLUMN product_type.journal_number_rule IS 'Правило нумерации журнала (JSONB), например {"format": "{YYYY}{MM}{TYPE_CODE}{NNNN}"}';
COMMENT ON COLUMN product_type.journal_columns IS 'Набор столбцов журнала для этого типа (JSONB)';

-- ============================================================================
-- Четыре системных типа продукции
-- До US10 они существовали только как значения enum CardTemplateType;
-- теперь это записи справочника, на которые ссылается dynamic_card,
-- журнал и генерация документов.
-- ============================================================================
INSERT INTO product_type (name, designation, is_system, section_id, code_prefix, system_card_type,
                          created_at, created_by)
SELECT v.name, v.designation, TRUE, s.id, v.code_prefix, v.system_card_type, NOW(), 'system'
FROM (VALUES ('Электродвигатель', 'ED', 'ED', 'MOTOR'),
             ('Мотор-колесо', 'MW', 'MW', 'MOTOR_WHEEL'),
             ('Колесо радиальное', 'RW', 'RW', 'RADIAL_WHEEL'),
             ('Колесо осевое', 'AW', 'AW', 'AXIAL_WHEEL')) AS v(name, designation, code_prefix, system_card_type)
         CROSS JOIN product_section s
WHERE s.parent_id IS NULL
  AND LOWER(s.name) = LOWER('Комплектующие');


-- ============================================================================
-- Таблица product_species — виды продукции (второй уровень)
-- Пример: «Прямоугольный», «Круглый», «Спиральный»
-- ============================================================================
CREATE TABLE product_species
(
    id         BIGSERIAL PRIMARY KEY,
    type_id    BIGINT       NOT NULL,
    name       VARCHAR(100),                       -- «Прямоугольный» или NULL
    is_system  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL,
    created_by VARCHAR(100),

    CONSTRAINT fk_product_species_type
        FOREIGN KEY (type_id) REFERENCES product_type (id) ON DELETE CASCADE
);

-- Уникальность имени вида в рамках типа (регистронезависимо).
-- NULL допускается — означает «No_species».
CREATE UNIQUE INDEX idx_product_species_type_name_unique
    ON product_species (type_id, LOWER(name))
    WHERE name IS NOT NULL;

CREATE INDEX idx_product_species_type_id ON product_species (type_id);

COMMENT ON TABLE product_species IS 'Виды продукции (второй уровень иерархии)';
COMMENT ON COLUMN product_species.name IS 'Наименование вида, например «Прямоугольный» (NULL = No_species)';


-- ============================================================================
-- Таблица product_series — серии продукции (третий уровень)
-- Пример: «P», «PS», «PKV», «N», «Vn»
-- ============================================================================
CREATE TABLE product_series
(
    id         BIGSERIAL PRIMARY KEY,
    species_id BIGINT       NOT NULL,
    name       VARCHAR(100),                       -- «P», «PS» или NULL
    is_system  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL,
    created_by VARCHAR(100),

    CONSTRAINT fk_product_series_species
        FOREIGN KEY (species_id) REFERENCES product_species (id) ON DELETE CASCADE
);

-- Уникальность имени серии в рамках вида (регистронезависимо).
-- NULL допускается — означает «No_series».
CREATE UNIQUE INDEX idx_product_series_species_name_unique
    ON product_series (species_id, LOWER(name))
    WHERE name IS NOT NULL;

CREATE INDEX idx_product_series_species_id ON product_series (species_id);

COMMENT ON TABLE product_series IS 'Серии продукции (третий уровень иерархии)';
COMMENT ON COLUMN product_series.name IS 'Наименование серии, например «P», «PS» (NULL = No_series)';

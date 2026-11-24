-- ============================================================================
-- V18__create_product_hierarchy_tables.sql
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
-- Справочник ролей продукции (US10.7.2)
-- Роль отвечает на вопрос «чем это изделие является для другого изделия»:
-- осевое колесо, радиальное колесо, клапан, теплообменник…
-- Роли — редактируемый справочник: администратор может добавлять свои.
-- ============================================================================
CREATE TABLE card_role
(
    id         BIGSERIAL PRIMARY KEY,
    code       VARCHAR(40) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    is_system  BOOLEAN     NOT NULL DEFAULT FALSE,
    sort_order INTEGER     NOT NULL DEFAULT 0,
    created_at TIMESTAMP   NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100)
);

CREATE UNIQUE INDEX idx_card_role_code_unique ON card_role (UPPER(code));

COMMENT ON TABLE card_role IS 'Роли продукции: назначение изделия при комплектации';
COMMENT ON COLUMN card_role.code IS 'Код роли (AXIAL_WHEEL, VALVE…), используется в referenceType полей шаблона';
COMMENT ON COLUMN card_role.is_system IS 'Системная роль — нельзя удалить';

INSERT INTO card_role (code, name, is_system, sort_order, created_at, created_by)
VALUES ('MOTOR', 'Электродвигатель', TRUE, 10, NOW(), 'system'),
       ('MOTOR_WHEEL', 'Мотор-колесо', TRUE, 20, NOW(), 'system'),
       ('AXIAL_WHEEL', 'Осевое колесо', TRUE, 30, NOW(), 'system'),
       ('RADIAL_WHEEL', 'Радиальное колесо', TRUE, 40, NOW(), 'system');

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
    card_role_id        BIGINT,                         -- роль при комплектации (US10.7.2)
    code_prefix         VARCHAR(4),                     -- ED, MW, RW, AW, VRK
    system_card_type    VARCHAR(30),                    -- MOTOR, MOTOR_WHEEL, ...
    journal_number_rule JSONB,
    journal_columns     JSONB,
    created_at          TIMESTAMP   NOT NULL,
    created_by          VARCHAR(100),

    CONSTRAINT fk_product_type_section
        FOREIGN KEY (section_id) REFERENCES product_section (id) ON DELETE SET NULL,

    CONSTRAINT fk_product_type_card_role
        FOREIGN KEY (card_role_id) REFERENCES card_role (id) ON DELETE SET NULL,

    CONSTRAINT chk_product_type_system_card_type
        CHECK (system_card_type IS NULL OR system_card_type IN
                                             ('MOTOR', 'MOTOR_WHEEL'))
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
COMMENT ON COLUMN product_type.system_card_type IS 'Хардкод-тип: MOTOR, MOTOR_WHEEL; NULL для динамических (включая колёса с US10.7)';
COMMENT ON COLUMN product_type.journal_number_rule IS 'Правило нумерации журнала (JSONB), например {"format": "{YYYY}{MM}{TYPE_CODE}{NNNN}"}';
COMMENT ON COLUMN product_type.journal_columns IS 'Набор столбцов журнала для этого типа (JSONB)';

-- ============================================================================
-- Хардкод-типы продукции (US10.7: осталось два)
-- До US10 они существовали только как значения enum CardTemplateType;
-- теперь это записи справочника, на которые ссылается dynamic_card,
-- журнал и генерация документов.
--
-- Колёса (AW, RW) с US10.7 — обычные динамические типы: ниже они создаются
-- с system_card_type = NULL и получают шаблоны, как остальная продукция.
-- ============================================================================
INSERT INTO product_type (name, designation, is_system, section_id, card_role_id,
                          code_prefix, system_card_type, created_at, created_by)
SELECT v.name, v.designation, TRUE, s.id,
       (SELECT r.id FROM card_role r WHERE r.code = v.role_code),
       v.code_prefix, v.system_card_type, NOW(), 'system'
FROM (VALUES ('Электродвигатель', 'ED', 'ED', 'MOTOR', 'MOTOR'),
             ('Мотор-колесо', 'MW', 'MW', 'MOTOR_WHEEL', 'MOTOR_WHEEL'))
         AS v(name, designation, code_prefix, system_card_type, role_code)
         CROSS JOIN product_section s
WHERE s.parent_id IS NULL
  AND LOWER(s.name) = LOWER('Комплектующие');

-- Колёса — динамические типы (шаблоны создаёт администратор через пресеты)
INSERT INTO product_type (name, designation, is_system, section_id, card_role_id,
                          code_prefix, system_card_type, created_at, created_by)
SELECT v.name, v.designation, FALSE, s.id,
       (SELECT r.id FROM card_role r WHERE r.code = v.role_code),
       v.code_prefix, NULL, NOW(), 'system'
FROM (VALUES ('Колесо радиальное', 'RW', 'RW', 'RADIAL_WHEEL'),
             ('Колесо осевое', 'AW', 'AW', 'AXIAL_WHEEL'))
         AS v(name, designation, code_prefix, role_code)
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

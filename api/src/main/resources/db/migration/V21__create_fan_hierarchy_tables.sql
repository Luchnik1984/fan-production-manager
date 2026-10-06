-- ============================================================================
-- V21__create_fan_hierarchy_tables.sql
-- Создание таблиц иерархии вентиляторов: тип, вид, серия.
--
-- Иерархия: Тип → Вид → Серия → Шаблон.
-- Все три таблицы — «тонкие» справочники, без бизнес-логики.
-- ============================================================================

-- ============================================================================
-- Таблица fan_type — типы вентиляторов (верхний уровень)
-- Пример: «Вентилятор канальный (VRK-PatAIR)»
-- ============================================================================
CREATE TABLE fan_type (
                          id              BIGSERIAL PRIMARY KEY,
                          name            VARCHAR(200),                       -- «Вентилятор канальный»
                          designation     VARCHAR(50) NOT NULL,               -- «VRK-PatAIR»
                          is_system       BOOLEAN NOT NULL DEFAULT FALSE,     -- системный тип (нельзя удалить)
                          created_at      TIMESTAMP NOT NULL,
                          created_by      VARCHAR(100)
);

-- Обозначение типа уникально (регистронезависимо)
CREATE UNIQUE INDEX idx_fan_type_designation_unique
    ON fan_type(LOWER(designation));

CREATE INDEX idx_fan_type_name ON fan_type(name);

COMMENT ON TABLE fan_type IS 'Типы вентиляторов (верхний уровень иерархии)';
COMMENT ON COLUMN fan_type.name IS 'Наименование типа, например «Вентилятор канальный»';
COMMENT ON COLUMN fan_type.designation IS 'Обозначение типа, например «VRK-PatAIR» (уникальное)';
COMMENT ON COLUMN fan_type.is_system IS 'Системный тип — нельзя удалять';


-- ============================================================================
-- Таблица fan_species — виды вентиляторов (второй уровень)
-- Пример: «Прямоугольный», «Круглый», «Спиральный»
-- ============================================================================
CREATE TABLE fan_species (
                             id              BIGSERIAL PRIMARY KEY,
                             type_id         BIGINT NOT NULL,
                             name            VARCHAR(100),                       -- «Прямоугольный» или NULL
                             is_system       BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at      TIMESTAMP NOT NULL,
                             created_by      VARCHAR(100),

                             CONSTRAINT fk_fan_species_type
                                 FOREIGN KEY (type_id) REFERENCES fan_type(id) ON DELETE CASCADE
);

-- Уникальность имени вида в рамках типа (регистронезависимо).
-- NULL допускается — означает «No_species».
CREATE UNIQUE INDEX idx_fan_species_type_name_unique
    ON fan_species(type_id, LOWER(name))
    WHERE name IS NOT NULL;

CREATE INDEX idx_fan_species_type_id ON fan_species(type_id);

COMMENT ON TABLE fan_species IS 'Виды вентиляторов (второй уровень иерархии)';
COMMENT ON COLUMN fan_species.name IS 'Наименование вида, например «Прямоугольный» (NULL = No_species)';


-- ============================================================================
-- Таблица fan_series — серии вентиляторов (третий уровень)
-- Пример: «P», «PS», «PKV», «N», «Vn»
-- ============================================================================
CREATE TABLE fan_series (
                            id              BIGSERIAL PRIMARY KEY,
                            species_id      BIGINT NOT NULL,
                            name            VARCHAR(100),                       -- «P», «PS» или NULL
                            is_system       BOOLEAN NOT NULL DEFAULT FALSE,
                            created_at      TIMESTAMP NOT NULL,
                            created_by      VARCHAR(100),

                            CONSTRAINT fk_fan_series_species
                                FOREIGN KEY (species_id) REFERENCES fan_species(id) ON DELETE CASCADE
);

-- Уникальность имени серии в рамках вида (регистронезависимо).
-- NULL допускается — означает «No_series».
CREATE UNIQUE INDEX idx_fan_series_species_name_unique
    ON fan_series(species_id, LOWER(name))
    WHERE name IS NOT NULL;

CREATE INDEX idx_fan_series_species_id ON fan_series(species_id);

COMMENT ON TABLE fan_series IS 'Серии вентиляторов (третий уровень иерархии)';
COMMENT ON COLUMN fan_series.name IS 'Наименование серии, например «P», «PS» (NULL = No_series)';
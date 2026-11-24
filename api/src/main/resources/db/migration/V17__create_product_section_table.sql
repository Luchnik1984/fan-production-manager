-- ============================================================================
-- V17__create_product_section_table.sql
-- Разделы продукции — верхний уровень иерархии.
--
--   Раздел → Тип → Вид → Серия → Шаблон → Карточка
--
-- Дерево разделов самодостаточно (parent_id), поэтому администратор может
-- создавать подразделы: «Комплектующие → Колёса», «Вентиляторы → Канальные».
-- Системные разделы (is_system = TRUE) удалить нельзя.
-- ============================================================================

CREATE TABLE product_section
(
    id         BIGSERIAL PRIMARY KEY,
    parent_id  BIGINT,
    name       VARCHAR(200) NOT NULL,
    sort_order INTEGER      NOT NULL DEFAULT 0,
    is_system  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    created_by VARCHAR(100),

    CONSTRAINT fk_product_section_parent
        FOREIGN KEY (parent_id) REFERENCES product_section (id) ON DELETE CASCADE
);

-- Уникальность имени раздела в рамках родителя (регистронезависимо).
-- COALESCE нужен, чтобы корневые разделы (parent_id IS NULL) тоже участвовали
-- в уникальности: в PostgreSQL NULL != NULL.
CREATE UNIQUE INDEX idx_product_section_parent_name_unique
    ON product_section (COALESCE(parent_id, 0), LOWER(name));

CREATE INDEX idx_product_section_parent_id ON product_section (parent_id);
CREATE INDEX idx_product_section_sort_order ON product_section (sort_order, id);

COMMENT ON TABLE product_section IS 'Разделы продукции (верхний уровень иерархии, редактируется администратором)';
COMMENT ON COLUMN product_section.parent_id IS 'Родительский раздел; NULL — корневой раздел';
COMMENT ON COLUMN product_section.sort_order IS 'Порядок отображения среди соседей (по возрастанию)';
COMMENT ON COLUMN product_section.is_system IS 'Системный раздел — нельзя удалять';

-- ============================================================================
-- Базовые разделы предметной области
-- ============================================================================
INSERT INTO product_section (name, sort_order, is_system, created_at, created_by)
VALUES ('Комплектующие', 10, TRUE, NOW(), 'system'),
       ('Вентиляторы', 20, TRUE, NOW(), 'system'),
       ('Стаканы', 30, TRUE, NOW(), 'system'),
       ('Клапаны', 40, TRUE, NOW(), 'system');

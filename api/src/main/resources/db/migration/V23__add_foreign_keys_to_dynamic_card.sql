-- ============================================================================
-- V23__add_foreign_keys_to_dynamic_card.sql
-- Добавление внешних ключей в таблицу dynamic_card.
-- Выполняется после создания всех связанных таблиц (V16-V21).
--
--   - колонка product_template_id (ссылка на логический шаблон)
--   - FK template_version_id → product_template_version.id
--   - FK product_type_id → product_type.id
-- ============================================================================

-- ============================================================================
-- 1. ВНЕШНИЙ КЛЮЧ ДЛЯ motor_wheel_id
-- ============================================================================
ALTER TABLE dynamic_card ADD CONSTRAINT fk_dynamic_card_motor_wheel
    FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id) ON DELETE SET NULL;

-- ============================================================================
-- 2. ВНЕШНИЙ КЛЮЧ ДЛЯ radial_wheel_id
-- ============================================================================
ALTER TABLE dynamic_card ADD CONSTRAINT fk_dynamic_card_radial_wheel
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id) ON DELETE SET NULL;

-- ============================================================================
-- 3. ВНЕШНИЙ КЛЮЧ ДЛЯ axial_wheel_id
-- ============================================================================
ALTER TABLE dynamic_card ADD CONSTRAINT fk_dynamic_card_axial_wheel
    FOREIGN KEY (axial_wheel_id) REFERENCES axial_wheel_card(id) ON DELETE SET NULL;

-- ============================================================================
-- 4. НОВАЯ КОЛОНКА product_template_id — ссылка на логический шаблон
-- ============================================================================
ALTER TABLE dynamic_card
    ADD COLUMN product_template_id BIGINT;

ALTER TABLE dynamic_card
    ADD CONSTRAINT fk_dynamic_card_product_template
        FOREIGN KEY (product_template_id) REFERENCES product_template(id) ON DELETE SET NULL;

CREATE INDEX idx_dynamic_card_product_template ON dynamic_card(product_template_id);

COMMENT ON COLUMN dynamic_card.product_template_id IS 'Ссылка на логический шаблон (product_template)';

-- ============================================================================
-- 5. ВНЕШНИЙ КЛЮЧ ДЛЯ template_version_id
-- ============================================================================
ALTER TABLE dynamic_card
    ADD CONSTRAINT fk_dynamic_card_template_version
        FOREIGN KEY (template_version_id) REFERENCES product_template_version(id) ON DELETE SET NULL;

COMMENT ON COLUMN dynamic_card.template_version_id IS 'Ссылка на текущую версию шаблона';

-- ============================================================================
-- 6. ВНЕШНИЙ КЛЮЧ ДЛЯ product_type_id
-- ============================================================================
ALTER TABLE dynamic_card
    ADD CONSTRAINT fk_dynamic_card_product_type
        FOREIGN KEY (product_type_id) REFERENCES product_type(id) ON DELETE SET NULL;

COMMENT ON COLUMN dynamic_card.product_type_id IS 'Тип продукции (product_type); для динамических карточек задаётся при создании';

-- ============================================================================
-- 7. КОММЕНТАРИИ К РАНЕЕ СОЗДАННЫМ ПОЛЯМ
-- ============================================================================
COMMENT ON COLUMN dynamic_card.motor_wheel_id IS 'Ссылка на мотор-колесо';
COMMENT ON COLUMN dynamic_card.radial_wheel_id IS 'Ссылка на радиальное колесо';
COMMENT ON COLUMN dynamic_card.axial_wheel_id IS 'Ссылка на осевое колесо';
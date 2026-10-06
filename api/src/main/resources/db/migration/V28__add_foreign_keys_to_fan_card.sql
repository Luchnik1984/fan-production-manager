-- ============================================================================
-- V28__add_foreign_keys_to_fan_card.sql
-- Добавление внешних ключей в таблицу fan_card.
-- Выполняется после создания всех связанных таблиц.
--
--   - колонка fan_template_id (ссылка на логический шаблон)
--   - FK template_version_id → fan_template_version.id
-- ============================================================================

-- ============================================================================
-- 1. ВНЕШНИЙ КЛЮЧ ДЛЯ motor_wheel_id
-- ============================================================================
ALTER TABLE fan_card ADD CONSTRAINT fk_fan_card_motor_wheel
    FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id) ON DELETE SET NULL;

-- ============================================================================
-- 2. ВНЕШНИЙ КЛЮЧ ДЛЯ radial_wheel_id
-- ============================================================================
ALTER TABLE fan_card ADD CONSTRAINT fk_fan_card_radial_wheel
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id) ON DELETE SET NULL;

-- ============================================================================
-- 3. ВНЕШНИЙ КЛЮЧ ДЛЯ axial_wheel_id
-- ============================================================================
ALTER TABLE fan_card ADD CONSTRAINT fk_fan_card_axial_wheel
    FOREIGN KEY (axial_wheel_id) REFERENCES axial_wheel_card(id) ON DELETE SET NULL;

-- ============================================================================
-- 4. НОВАЯ КОЛОНКА fan_template_id — ссылка на логический шаблон
-- ============================================================================
ALTER TABLE fan_card
    ADD COLUMN fan_template_id BIGINT;

ALTER TABLE fan_card
    ADD CONSTRAINT fk_fan_card_fan_template
        FOREIGN KEY (fan_template_id) REFERENCES fan_template(id) ON DELETE SET NULL;

CREATE INDEX idx_fan_card_fan_template ON fan_card(fan_template_id);

COMMENT ON COLUMN fan_card.fan_template_id IS 'Ссылка на логический шаблон (fan_template)';

-- ============================================================================
-- 5. ВНЕШНИЙ КЛЮЧ ДЛЯ template_version_id
-- ============================================================================
ALTER TABLE fan_card
    ADD CONSTRAINT fk_fan_card_template_version
        FOREIGN KEY (template_version_id) REFERENCES fan_template_version(id) ON DELETE SET NULL;

COMMENT ON COLUMN fan_card.template_version_id IS 'Ссылка на текущую версию шаблона';

-- ============================================================================
-- 6. КОММЕНТАРИИ К РАНЕЕ СОЗДАННЫМ ПОЛЯМ
-- ============================================================================
COMMENT ON COLUMN fan_card.motor_wheel_id IS 'Ссылка на мотор-колесо';
COMMENT ON COLUMN fan_card.radial_wheel_id IS 'Ссылка на радиальное колесо';
COMMENT ON COLUMN fan_card.axial_wheel_id IS 'Ссылка на осевое колесо';
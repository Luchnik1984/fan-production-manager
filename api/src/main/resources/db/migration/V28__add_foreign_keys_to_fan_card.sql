-- V28__add_foreign_keys_to_fan_card.sql
-- Добавление внешних ключей в таблицу fan_card.
-- Выполняется после создания всех таблиц

-- ==========================================================
-- 1. ВНЕШНИЙ КЛЮЧ ДЛЯ motor_wheel_id
-- ==========================================================

ALTER TABLE fan_card ADD CONSTRAINT fk_fan_card_motor_wheel
    FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id) ON DELETE SET NULL;

-- ==========================================================
-- 2. ВНЕШНИЙ КЛЮЧ ДЛЯ radial_wheel_id
-- ==========================================================

ALTER TABLE fan_card ADD CONSTRAINT fk_fan_card_radial_wheel
    FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id) ON DELETE SET NULL;

-- ==========================================================
-- 3. ВНЕШНИЙ КЛЮЧ ДЛЯ axial_wheel_id
-- ==========================================================

ALTER TABLE fan_card ADD CONSTRAINT fk_fan_card_axial_wheel
    FOREIGN KEY (axial_wheel_id) REFERENCES axial_wheel_card(id) ON DELETE SET NULL;

-- ==========================================================
-- 4. КОММЕНТАРИИ
-- ==========================================================

COMMENT ON COLUMN fan_card.motor_wheel_id IS 'Ссылка на мотор-колесо';
COMMENT ON COLUMN fan_card.radial_wheel_id IS 'Ссылка на радиальное колесо';
COMMENT ON COLUMN fan_card.axial_wheel_id IS 'Ссылка на осевое колесо';
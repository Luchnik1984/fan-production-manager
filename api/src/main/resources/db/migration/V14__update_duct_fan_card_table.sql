-- V14__update_duct_fan_card_table.sql
-- Добавление новых полей в таблицу канальных вентиляторов

ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS execution_type VARCHAR(20);
ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS duct_fan_type VARCHAR(20);
ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS motor_wheel_id BIGINT;
ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS radial_wheel_id BIGINT;
ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS wheel_size DOUBLE PRECISION;
ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS motor_poles_code VARCHAR(10);
ALTER TABLE duct_fan_card ADD COLUMN IF NOT EXISTS cup_id BIGINT;

-- Внешние ключи
ALTER TABLE duct_fan_card ADD CONSTRAINT fk_duct_fan_motor_wheel FOREIGN KEY (motor_wheel_id) REFERENCES motor_wheel_card(id);
ALTER TABLE duct_fan_card ADD CONSTRAINT fk_duct_fan_radial_wheel FOREIGN KEY (radial_wheel_id) REFERENCES radial_wheel_card(id);
ALTER TABLE duct_fan_card ADD CONSTRAINT fk_duct_fan_cup FOREIGN KEY (cup_id) REFERENCES cup_card(id);
-- V22__create_unit_of_measure_table.sql
-- Создание справочника единиц измерения

CREATE TABLE IF NOT EXISTS unit_of_measure (
                                               id BIGSERIAL PRIMARY KEY,
                                               code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_unit_of_measure_code ON unit_of_measure(code);

-- Базовые единицы измерения
INSERT INTO unit_of_measure (code, name, symbol, is_default, created_at, created_by) VALUES
                                                                                         ('шт', 'штука', 'шт', TRUE, NOW(), 'system'),
                                                                                         ('м', 'метр', 'м', FALSE, NOW(), 'system'),
                                                                                         ('кг', 'килограмм', 'кг', FALSE, NOW(), 'system'),
                                                                                         ('г', 'грамм', 'г', FALSE, NOW(), 'system'),
                                                                                         ('л', 'литр', 'л', FALSE, NOW(), 'system'),
                                                                                         ('компл', 'комплект', 'компл', FALSE, NOW(), 'system'),
                                                                                         ('упак', 'упаковка', 'упак', FALSE, NOW(), 'system'),
                                                                                         ('пог.м', 'погонный метр', 'пог.м', FALSE, NOW(), 'system'),
                                                                                         ('м²', 'квадратный метр', 'м²', FALSE, NOW(), 'system');
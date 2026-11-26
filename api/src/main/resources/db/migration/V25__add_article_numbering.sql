-- ============================================================================
-- V25__add_article_numbering.sql
-- US10.7.7: артикулы продукции.
--
-- Артикул — внутренний идентификатор изделия: присваивается один раз,
-- не меняется и не переиспользуется. Формат: <PREFIX>-<6 цифр>,
-- например AW-000123.
--
-- Префикс берётся из product_type.code_prefix (AW, RW, ED, MW, VRK…),
-- для справочников — CMP (компоненты) и MAT (материалы).
-- ============================================================================

CREATE TABLE article_sequence
(
    prefix     VARCHAR(8) PRIMARY KEY,
    next_value BIGINT NOT NULL DEFAULT 1
);

COMMENT ON TABLE article_sequence IS 'Счётчики артикулов по префиксам (US10.7.7)';
COMMENT ON COLUMN article_sequence.next_value IS 'Следующее значение счётчика (выдаётся атомарно)';

-- Счётчики для существующих типов продукции
INSERT INTO article_sequence (prefix, next_value)
SELECT DISTINCT UPPER(code_prefix), 1
FROM product_type
WHERE code_prefix IS NOT NULL
  AND code_prefix <> '';

-- Счётчики справочников
INSERT INTO article_sequence (prefix, next_value)
VALUES ('CMP', 1),
       ('MAT', 1);

-- Артикул производителя (для покупных изделий), необязательный и неуникальный
ALTER TABLE base_product_card
    ADD COLUMN manufacturer_article VARCHAR(100);

COMMENT ON COLUMN base_product_card.code IS 'Внутренний артикул: PREFIX-000123, неизменяемый';
COMMENT ON COLUMN base_product_card.manufacturer_article IS 'Артикул производителя/продавца (необязательный, неуникальный)';

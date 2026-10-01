-- V16__create_cup_card_table.sql
-- Создание таблицы для стаканов

CREATE TABLE IF NOT EXISTS cup_card (
                                        id BIGSERIAL PRIMARY KEY,
                                        diameter INTEGER,
                                        height INTEGER,
                                        material VARCHAR(50),
    full_marking VARCHAR(200),
    thickness DOUBLE PRECISION,
    FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE
    );

CREATE INDEX IF NOT EXISTS idx_cup_card_diameter ON cup_card(diameter);
CREATE UNIQUE INDEX IF NOT EXISTS idx_cup_full_marking ON cup_card(LOWER(full_marking)) WHERE full_marking IS NOT NULL;

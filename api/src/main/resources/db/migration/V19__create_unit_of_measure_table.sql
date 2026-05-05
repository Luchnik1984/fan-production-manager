-- V19__create_unit_of_measure_table.sql
-- Создание справочника единиц измерения

CREATE TABLE IF NOT EXISTS unit_of_measure (
                                               id BIGSERIAL PRIMARY KEY,
                                               code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    symbol VARCHAR(10),
    category VARCHAR(50) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    created_by VARCHAR(100)
    );

CREATE INDEX IF NOT EXISTS idx_unit_of_measure_code ON unit_of_measure(code);
CREATE INDEX IF NOT EXISTS idx_unit_of_measure_category ON unit_of_measure(category);
CREATE INDEX IF NOT EXISTS idx_unit_of_measure_default ON unit_of_measure(is_default) WHERE is_default = TRUE;

INSERT INTO unit_of_measure (code, name, symbol, category, is_default, created_at, created_by) VALUES

                                                                                                   -- ========== БАЗОВЫЕ (шт) ==========
                                                                                                   ('шт', 'штука', 'шт', 'Базовые', TRUE, NOW(), 'system'),

                                                                                                   -- ========== ДЛИНА / ПЛОЩАДЬ / ОБЪЁМ ==========
                                                                                                   ('мм', 'миллиметр', 'мм', 'Длина', FALSE, NOW(), 'system'),
                                                                                                   ('м', 'метр', 'м', 'Длина', FALSE, NOW(), 'system'),
                                                                                                   ('м²', 'квадратный метр', 'м²', 'Площадь', FALSE, NOW(), 'system'),
                                                                                                   ('м³', 'кубический метр', 'м³', 'Объём', FALSE, NOW(), 'system'),
                                                                                                   ('л', 'литр', 'л', 'Объём', FALSE, NOW(), 'system'),

                                                                                                   -- ========== МАССА / ПЛОТНОСТЬ ==========
                                                                                                   ('кг', 'килограмм', 'кг', 'Масса', FALSE, NOW(), 'system'),
                                                                                                   ('г', 'грамм', 'г', 'Масса', FALSE, NOW(), 'system'),
                                                                                                   ('т', 'тонна', 'т', 'Масса', FALSE, NOW(), 'system'),
                                                                                                   ('кг/м³', 'килограмм на кубический метр', 'кг/м³', 'Плотность', FALSE, NOW(), 'system'),

                                                                                                   -- ========== ДАВЛЕНИЕ ==========
                                                                                                   ('Па', 'паскаль', 'Па', 'Давление', FALSE, NOW(), 'system'),
                                                                                                   ('кПа', 'килопаскаль', 'кПа', 'Давление', FALSE, NOW(), 'system'),
                                                                                                   ('МПа', 'мегапаскаль', 'МПа', 'Давление', FALSE, NOW(), 'system'),
                                                                                                   ('бар', 'бар', 'бар', 'Давление', FALSE, NOW(), 'system'),
                                                                                                   ('мм.вод.ст', 'миллиметр водяного столба', 'мм.вод.ст', 'Давление', FALSE, NOW(), 'system'),
                                                                                                   ('атм', 'атмосфера', 'атм', 'Давление', FALSE, NOW(), 'system'),

                                                                                                   -- ========== РАСХОД ==========
                                                                                                   ('м³/ч', 'кубический метр в час', 'м³/ч', 'Расход', FALSE, NOW(), 'system'),
                                                                                                   ('м³/с', 'кубический метр в секунду', 'м³/с', 'Расход', FALSE, NOW(), 'system'),
                                                                                                   ('л/с', 'литр в секунду', 'л/с', 'Расход', FALSE, NOW(), 'system'),
                                                                                                   ('л/мин', 'литр в минуту', 'л/мин', 'Расход', FALSE, NOW(), 'system'),

                                                                                                   -- ========== СКОРОСТЬ ==========
                                                                                                   ('м/с', 'метр в секунду', 'м/с', 'Скорость', FALSE, NOW(), 'system'),
                                                                                                   ('км/ч', 'километр в час', 'км/ч', 'Скорость', FALSE, NOW(), 'system'),
                                                                                                   ('об/мин', 'оборот в минуту', 'об/мин', 'Скорость вращения', FALSE, NOW(), 'system'),
                                                                                                   ('рад/с', 'радиан в секунду', 'рад/с', 'Скорость вращения', FALSE, NOW(), 'system'),

                                                                                                   -- ========== ЭЛЕКТРИЧЕСКИЕ ==========
                                                                                                   ('Вт', 'ватт', 'Вт', 'Мощность', FALSE, NOW(), 'system'),
                                                                                                   ('кВт', 'киловатт', 'кВт', 'Мощность', FALSE, NOW(), 'system'),
                                                                                                   ('В', 'вольт', 'В', 'Электричество', FALSE, NOW(), 'system'),
                                                                                                   ('А', 'ампер', 'А', 'Электричество', FALSE, NOW(), 'system'),
                                                                                                   ('Ом', 'ом', 'Ом', 'Электричество', FALSE, NOW(), 'system'),
                                                                                                   ('Гц', 'герц', 'Гц', 'Электричество', FALSE, NOW(), 'system'),

                                                                                                   -- ========== МОМЕНТ (КРУТЯЩИЙ) ==========
                                                                                                   ('Н·м', 'ньютон-метр', 'Н·м', 'Момент', FALSE, NOW(), 'system'),
                                                                                                   ('кгс·м', 'килограмм-сила-метр', 'кгс·м', 'Момент', FALSE, NOW(), 'system'),

                                                                                                   -- ========== ВРЕМЯ ==========
                                                                                                   ('ч', 'час', 'ч', 'Время', FALSE, NOW(), 'system'),
                                                                                                   ('мин', 'минута', 'мин', 'Время', FALSE, NOW(), 'system'),
                                                                                                   ('с', 'секунда', 'с', 'Время', FALSE, NOW(), 'system'),

                                                                                                   -- ========== ТЕМПЕРАТУРА ==========
                                                                                                   ('°C', 'градус Цельсия', '°C', 'Температура', FALSE, NOW(), 'system'),
                                                                                                   ('K', 'кельвин', 'K', 'Температура', FALSE, NOW(), 'system'),

                                                                                                   -- ========== ПРОЧИЕ ==========
                                                                                                   ('компл', 'комплект', 'компл', 'Прочие', FALSE, NOW(), 'system'),
                                                                                                   ('упак', 'упаковка', 'упак', 'Прочие', FALSE, NOW(), 'system'),
                                                                                                   ('пог.м', 'погонный метр', 'пог.м', 'Прочие', FALSE, NOW(), 'system'),
                                                                                                   ('%', 'процент', '%', 'Прочие', FALSE, NOW(), 'system'),
                                                                                                   ('дБ', 'децибел', 'дБ', 'Прочие', FALSE, NOW(), 'system');

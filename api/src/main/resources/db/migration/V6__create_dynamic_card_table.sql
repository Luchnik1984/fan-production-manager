-- ============================================================================
-- V6__create_dynamic_card_table.sql
-- Создание таблицы карточек динамических типов.
--
-- Таблица dynamic_card используется как единая таблица для всех вентиляторов
-- (осевых, радиальных, канальных, крышных, струйных и т.д.).
-- Поля карточки делятся на две категории:
--   1. Системные — хранятся в отдельных колонках (для поиска, фильтрации, отчётов).
--   2. Пользовательские — хранятся в JSONB-поле dynamic_fields (задаются шаблоном).
--
-- Каждая карточка привязана к конкретной версии шаблона (template_version_id).
-- Это позволяет безопасно менять шаблоны, не ломая уже созданные карточки.
-- ============================================================================

CREATE TABLE IF NOT EXISTS dynamic_card (
                                        id BIGSERIAL PRIMARY KEY,

    -- ========================================================================
    -- ОБЩИЕ ПОЛЯ
    -- ========================================================================
                                        manufacturer        VARCHAR(100),               -- Производитель
                                        series              VARCHAR(50),                -- Серия (P, PS, PKV, PRV и т.д.)
                                        size                DOUBLE PRECISION,           -- Типоразмер (для фильтрации)
                                        marking             VARCHAR(100),               -- Маркировка производителя (для партнёрской продукции)
                                        climate_type        VARCHAR(10),                -- Климатическое исполнение

    -- ========================================================================
    -- ФЛАГИ ПРОИЗВОДСТВА
    -- ========================================================================
                                        is_partner_production BOOLEAN DEFAULT FALSE,    -- Партнёрская продукция
                                        is_own_production     BOOLEAN DEFAULT FALSE,    -- Собственное производство

    -- ========================================================================
    -- ССЫЛКИ НА КОМПОНЕНТЫ (сборочные узлы)
    -- ========================================================================
                                        motor_id            BIGINT,                     -- Ссылка на электродвигатель
                                        motor_wheel_id      BIGINT,                     -- Ссылка на мотор-колесо
                                        radial_wheel_id     BIGINT,                     -- Ссылка на радиальное колесо
                                        axial_wheel_id      BIGINT,                     -- Ссылка на осевое колесо

    -- ========================================================================
    -- ИНФОРМАЦИОННЫЕ ПОЛЯ
    -- ========================================================================
                                        hub_type            VARCHAR(50),                -- Тип ступицы (справочно, из выбранного колеса)
                                        wheel_formula       VARCHAR(100),               -- Формула колеса
                                        wheel_diameter      DOUBLE PRECISION,           -- Диаметр колеса
                                        cable_spec          VARCHAR(100),               -- Кабель подключения
                                        has_ha              BOOLEAN DEFAULT FALSE,      -- Наличие направляющего аппарата (НА)
                                        has_ca              BOOLEAN DEFAULT FALSE,      -- Наличие спрямляющего аппарата (СА)
                                        certificate_number  VARCHAR(100),               -- Номер сертификата/декларации
                                        fan_class           VARCHAR(20),                -- Класс вентилятора
                                        fan_type            VARCHAR(30),                -- Тип вентилятора (осевой, радиальный, ...) — легаси
                                        fan_subtype         VARCHAR(30),                -- Подтип

    -- ========================================================================
    -- ЭЛЕКТРИЧЕСКИЕ ПАРАМЕТРЫ
    -- ========================================================================
                                        power_kw            DOUBLE PRECISION,           -- Мощность (кВт)
                                        poles               INTEGER,                    -- Количество полюсов
                                        voltage             INTEGER,                    -- Рабочее напряжение (В)
                                        voltage_code        VARCHAR(10),                -- Код напряжения (E, D)
                                        rated_speed_rpm     INTEGER,                    -- Номинальная скорость (об/мин)
                                        actual_speed_rpm    INTEGER,                    -- Фактическая скорость (об/мин)
                                        max_speed_rpm       INTEGER,                    -- Максимальная скорость вращения (об/мин)

    -- ========================================================================
    -- ИСПОЛНЕНИЕ ПО НАЗНАЧЕНИЮ
    -- ========================================================================
                                        general_purpose     BOOLEAN DEFAULT TRUE,       -- Общего применения
                                        fireproof           BOOLEAN DEFAULT FALSE,      -- Огнестойкость
                                        fireproof_marking   VARCHAR(100),               -- Маркировка огнестойкости
                                        max_temperature     INTEGER,                    -- Предельная температура (°C)
                                        explosion_proof     BOOLEAN DEFAULT FALSE,      -- Взрывозащита
                                        explosion_marking   VARCHAR(100),               -- Маркировка взрывозащиты

    -- ========================================================================
    -- ПОЛНАЯ МАРКИРОВКА
    -- ========================================================================
                                        full_marking        VARCHAR(200),               -- Полная маркировка (вычисляется по правилу шаблона)

    -- ========================================================================
    -- МАРКИРОВКИ КОМПОНЕНТОВ (сохраняются для справки и отображения)
    -- ========================================================================
                                        motor_wheel_full_marking  VARCHAR(200),         -- Полная маркировка мотор-колеса
                                        radial_wheel_full_marking VARCHAR(200),         -- Полная маркировка радиального колеса
                                        axial_wheel_full_marking  VARCHAR(200),         -- Полная маркировка осевого колеса
                                        motor_full_marking        VARCHAR(200),         -- Полная маркировка электродвигателя

    -- ========================================================================
    -- ПОЛЯ КОНСТРУКТОРА ШАБЛОНОВ
    -- ========================================================================
    -- Тип продукции (product_type), к которому относится карточка.
    -- Внешний ключ добавляется в V23, когда таблица product_type уже создана.
                                        product_type_id     BIGINT,

    -- Ссылка на конкретную версию шаблона, по которой создана карточка.
    -- Внешний ключ на product_template_version добавляется в V23.
                                        template_version_id BIGINT,

    -- Пользовательские поля, определённые в шаблоне.
    -- Формат: {"ductSize": "40-20", "wheelSize": 22.2, ...}
                                        dynamic_fields      JSONB,

    -- ========================================================================
    -- ВНЕШНИЕ КЛЮЧИ
    -- ========================================================================
    -- Ссылка на базовую карточку (наследование)
                                        FOREIGN KEY (id) REFERENCES base_product_card(id) ON DELETE CASCADE,

    -- Ссылка на электродвигатель (таблица создана в V5, поэтому FK можно добавить здесь)
                                        FOREIGN KEY (motor_id) REFERENCES motor_card(id)
);

-- ============================================================================
-- ИНДЕКСЫ
-- ============================================================================
-- Общие индексы (существующие)
CREATE INDEX IF NOT EXISTS idx_dynamic_card_size              ON dynamic_card(size);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_motor_id          ON dynamic_card(motor_id);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_motor_wheel       ON dynamic_card(motor_wheel_id);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_radial_wheel      ON dynamic_card(radial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_axial_wheel       ON dynamic_card(axial_wheel_id);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_fan_type          ON dynamic_card(fan_type);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_fan_class             ON dynamic_card(fan_class);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_poles             ON dynamic_card(poles);

-- Новые индексы (для полей, добавленных в этом шаге)
CREATE INDEX IF NOT EXISTS idx_dynamic_card_series            ON dynamic_card(series);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_marking           ON dynamic_card(marking);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_is_partner        ON dynamic_card(is_partner_production);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_is_own            ON dynamic_card(is_own_production);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_max_speed         ON dynamic_card(max_speed_rpm);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_template_version  ON dynamic_card(template_version_id);
CREATE INDEX IF NOT EXISTS idx_dynamic_card_product_type      ON dynamic_card(product_type_id);

-- Уникальный индекс на полную маркировку (регистронезависимый, частичный)
-- Гарантирует уникальность маркировки среди всех вентиляторов.
CREATE UNIQUE INDEX IF NOT EXISTS idx_dynamic_card_full_marking
    ON dynamic_card(LOWER(full_marking))
    WHERE full_marking IS NOT NULL;

-- ============================================================================
-- КОММЕНТАРИИ К ТАБЛИЦЕ И КОЛОНКАМ
-- ============================================================================
COMMENT ON TABLE dynamic_card IS 'Карточки продукции динамических типов (вентиляторы, клапаны, стаканы и др.)';

COMMENT ON COLUMN dynamic_card.manufacturer            IS 'Производитель вентилятора';
COMMENT ON COLUMN dynamic_card.series                  IS 'Серия вентилятора (P, PS, PKV, PRV и т.д.)';
COMMENT ON COLUMN dynamic_card.size                    IS 'Типоразмер вентилятора';
COMMENT ON COLUMN dynamic_card.marking                 IS 'Маркировка производителя (для партнёрской продукции)';
COMMENT ON COLUMN dynamic_card.climate_type            IS 'Климатическое исполнение';
COMMENT ON COLUMN dynamic_card.is_partner_production   IS 'Флаг: партнёрская продукция';
COMMENT ON COLUMN dynamic_card.is_own_production       IS 'Флаг: собственная продукция';
COMMENT ON COLUMN dynamic_card.motor_id                IS 'Ссылка на электродвигатель';
COMMENT ON COLUMN dynamic_card.motor_wheel_id          IS 'Ссылка на мотор-колесо';
COMMENT ON COLUMN dynamic_card.radial_wheel_id         IS 'Ссылка на радиальное колесо';
COMMENT ON COLUMN dynamic_card.axial_wheel_id          IS 'Ссылка на осевое колесо';
COMMENT ON COLUMN dynamic_card.hub_type                IS 'Тип ступицы (справочно, из выбранного колеса)';
COMMENT ON COLUMN dynamic_card.wheel_formula           IS 'Формула колеса';
COMMENT ON COLUMN dynamic_card.wheel_diameter          IS 'Диаметр колеса';
COMMENT ON COLUMN dynamic_card.cable_spec              IS 'Кабель подключения';
COMMENT ON COLUMN dynamic_card.has_ha                  IS 'Наличие направляющего аппарата';
COMMENT ON COLUMN dynamic_card.has_ca                  IS 'Наличие спрямляющего аппарата';
COMMENT ON COLUMN dynamic_card.certificate_number      IS 'Номер сертификата или декларации соответствия';
COMMENT ON COLUMN dynamic_card.power_kw                IS 'Мощность электродвигателя (кВт)';
COMMENT ON COLUMN dynamic_card.poles                   IS 'Количество полюсов электродвигателя';
COMMENT ON COLUMN dynamic_card.voltage                 IS 'Рабочее напряжение (В)';
COMMENT ON COLUMN dynamic_card.voltage_code            IS 'Код напряжения (E = 220В, D = 380В)';
COMMENT ON COLUMN dynamic_card.rated_speed_rpm         IS 'Номинальная скорость вращения (об/мин)';
COMMENT ON COLUMN dynamic_card.actual_speed_rpm        IS 'Фактическая скорость вращения (об/мин)';
COMMENT ON COLUMN dynamic_card.max_speed_rpm           IS 'Максимальная скорость вращения (об/мин)';
COMMENT ON COLUMN dynamic_card.general_purpose         IS 'Исполнение: общего применения';
COMMENT ON COLUMN dynamic_card.fireproof               IS 'Исполнение: огнестойкость';
COMMENT ON COLUMN dynamic_card.explosion_proof         IS 'Исполнение: взрывозащита';
COMMENT ON COLUMN dynamic_card.full_marking            IS 'Полная маркировка (вычисляется по правилу шаблона)';
COMMENT ON COLUMN dynamic_card.template_version_id     IS 'Ссылка на версию шаблона, по которой создана карточка';
COMMENT ON COLUMN dynamic_card.dynamic_fields          IS 'Пользовательские поля шаблона в формате JSONB';
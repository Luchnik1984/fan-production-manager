-- ============================================================================
-- V22__create_fan_template_tables.sql
-- Создание таблиц шаблонов вентиляторов: логический шаблон + версии.
--
-- Шаблон — сущность, привязанная к серии. Имеет версии.
-- Версия — неизменяемый снимок полей и правила маркировки (JSONB).
-- ============================================================================

-- ============================================================================
-- Таблица fan_template — логический шаблон (привязан к серии)
-- ============================================================================
CREATE TABLE fan_template (
                              id                  BIGSERIAL PRIMARY KEY,
                              series_id           BIGINT NOT NULL,
                              name                VARCHAR(200) NOT NULL,
                              description         VARCHAR(500),
                              current_version_id  BIGINT,
                              created_at          TIMESTAMP NOT NULL,
                              created_by          VARCHAR(100),

                              CONSTRAINT fk_fan_template_series
                                  FOREIGN KEY (series_id) REFERENCES fan_series(id) ON DELETE CASCADE
);

CREATE INDEX idx_fan_template_series_id ON fan_template(series_id);

-- На одну серию — один шаблон
CREATE UNIQUE INDEX idx_fan_template_series_unique ON fan_template(series_id);

COMMENT ON TABLE fan_template IS 'Логические шаблоны карточек вентиляторов';
COMMENT ON COLUMN fan_template.name IS 'Наименование шаблона (например, «Шаблон P-серии»)';
COMMENT ON COLUMN fan_template.current_version_id IS 'Ссылка на актуальную опубликованную версию';


-- ============================================================================
-- Таблица fan_template_version — версии шаблона
-- Содержит JSONB-поля fields_json и marking_rule_json.
-- ============================================================================
CREATE TABLE fan_template_version (
                                      id                  BIGSERIAL PRIMARY KEY,
                                      template_id         BIGINT NOT NULL,
                                      version             INTEGER NOT NULL,
                                      status              VARCHAR(20) NOT NULL,
                                      fields_json         JSONB NOT NULL,
                                      marking_rule_json   JSONB NOT NULL,
                                      created_at          TIMESTAMP NOT NULL,
                                      created_by          VARCHAR(100),
                                      published_at        TIMESTAMP,
                                      published_by        VARCHAR(100),

                                      CONSTRAINT fk_fan_template_version_template
                                          FOREIGN KEY (template_id) REFERENCES fan_template(id) ON DELETE CASCADE,

                                      CONSTRAINT chk_fan_template_version_status
                                          CHECK (status IN ('DRAFT', 'PUBLISHED', 'LEGACY', 'DEPRECATED', 'ARCHIVED'))
);

CREATE UNIQUE INDEX idx_fan_template_version_template_version
    ON fan_template_version(template_id, version);

CREATE INDEX idx_fan_template_version_template_id ON fan_template_version(template_id);
CREATE INDEX idx_fan_template_version_status ON fan_template_version(status);

-- Частичный индекс для быстрого поиска «текущая PUBLISHED версия»
CREATE INDEX idx_fan_template_version_template_status
    ON fan_template_version(template_id, status)
    WHERE status = 'PUBLISHED';

COMMENT ON TABLE fan_template_version IS 'Версии шаблонов (DRAFT/PUBLISHED/LEGACY/DEPRECATED/ARCHIVED)';
COMMENT ON COLUMN fan_template_version.version IS 'Номер версии в рамках шаблона (1, 2, 3...)';
COMMENT ON COLUMN fan_template_version.status IS 'DRAFT, PUBLISHED, LEGACY, DEPRECATED, ARCHIVED';
COMMENT ON COLUMN fan_template_version.fields_json IS 'Список полей шаблона (JSONB)';
COMMENT ON COLUMN fan_template_version.marking_rule_json IS 'Правило маркировки (JSONB)';


-- ============================================================================
-- Обратный FK на current_version_id в fan_template
-- Добавляем ПОСЛЕ создания fan_template_version — иначе циклическая зависимость.
-- ============================================================================
ALTER TABLE fan_template
    ADD CONSTRAINT fk_fan_template_current_version
        FOREIGN KEY (current_version_id)
            REFERENCES fan_template_version(id)
            ON DELETE SET NULL;

COMMENT ON COLUMN fan_template.current_version_id IS 'Актуальная PUBLISHED версия шаблона';
package com.fanproduction.template.service;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;
import java.util.Map;

/**
 * Движок вычисления маркировки.
 * <p>
 * Обходит правило {@link MarkingRule} и собирает строку маркировки.
 * <p>
 * См. FR4.5.3.
 */
public interface MarkingEngine {

    String evaluate(MarkingRule rule, MarkingContext context);

    /**
     * Вычисление маркировки по значениям полей (без метаданных).
     * <p>
     * Используется в тестах и в местах, где нет templateVersionId.
     */
    String evaluate(MarkingRule rule,
                    Map<String, Object> values,
                    List<FieldDefinition> fields);

    /**
     * Вычисление маркировки с метаданными шаблона (US9.8).
     * <p>
     * Позволяет использовать {@code FIELD('typeDesignation')},
     * {@code FIELD('typeName')}, {@code FIELD('seriesName')},
     * {@code FIELD('speciesName')} в правиле маркировки.
     *
     * @param templateVersionId ID версии шаблона (для резолва метаданных).
     *                          Если {@code null} — метаданные не используются
     *                          (поведение как у {@link #evaluate(MarkingRule, Map, List)}).
     */
    String evaluate(MarkingRule rule,
                    Map<String, Object> values,
                    List<FieldDefinition> fields,
                    Long templateVersionId);
}

package com.fanproduction.template.service;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;
import java.util.Map;

/**
 * Движок вычисления маркировки.
 * <p>
 * Обходит правило {@link MarkingRule} и собирает строку маркировки,
 * подставляя значения полей из {@link MarkingContext}.
 * <p>
 * Типы элементов правила:
 * <ul>
 *   <li>{@code LITERAL}   — фиксированный текст.</li>
 *   <li>{@code SEPARATOR} — символ-разделитель.</li>
 *   <li>{@code FIELD}     — значение поля (обычное или вычисляемое).</li>
 *   <li>{@code CONDITION} — условный блок с {@code then} / {@code otherwise}.</li>
 * </ul>
 * <p>
 * См. FR4.5.3.
 */
public interface MarkingEngine {

    /**
     * Вычислить маркировку по правилу и контексту.
     *
     * @param rule    правило маркировки
     * @param context контекст с значениями полей
     * @return строка маркировки
     */
    String evaluate(MarkingRule rule, MarkingContext context);

    /**
     * Удобная обёртка: вычисление маркировки по значениям и определениям полей.
     * <p>
     * Создаёт {@link MarkingContext} внутри.
     *
     * @param rule   правило маркировки
     * @param values значения полей
     * @param fields определения полей шаблона
     * @return строка маркировки
     */
    String evaluate(MarkingRule rule,
                    Map<String, Object> values,
                    List<FieldDefinition> fields);
}

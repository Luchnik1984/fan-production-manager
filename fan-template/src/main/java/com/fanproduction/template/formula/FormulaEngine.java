package com.fanproduction.template.formula;

import com.fanproduction.template.formula.ast.Expression;
import com.fanproduction.template.model.FieldDefinition;

import java.util.Map;

/**
 * Движок исполнения формул DSL.
 * <p>
 * Содержит методы парсинга (через {@code FormulaParser}) и интерпретации AST.
 * <p>
 * Основные сценарии использования:
 * <ul>
 *   <li>{@link #evaluate(String, Map)} — вычислить формулу по строке.</li>
 *   <li>{@link #evaluateToString(String, Map)} — вычислить формулу, привести к строке.</li>
 *   <li>{@link #evaluateFieldDefinition(FieldDefinition, Map)} — вычислить значение COMPUTED-поля.</li>
 * </ul>
 */
public interface FormulaEngine {

    /**
     * Разобрать формулу в AST.
     *
     * @throws com.fanproduction.template.formula.exception.FormulaException при синтаксической ошибке
     */
    Expression parse(String formula);

    /**
     * Вычислить формулу.
     *
     * @param formula строка формулы
     * @param values  значения полей карточки
     * @return результат или {@code null}
     * @throws com.fanproduction.template.formula.exception.FormulaException при ошибке
     */
    Object evaluate(String formula, Map<String, Object> values);

    /**
     * Вычислить AST-выражение.
     */
    Object evaluate(Expression expression, Map<String, Object> values);

    /**
     * Вычислить формулу и вернуть строковый результат.
     * <p>
     * {@code null} остаётся {@code null}. Другие типы приводятся к строке.
     */
    String evaluateToString(String formula, Map<String, Object> values);

    /**
     * Вычислить значение поля типа {@code COMPUTED}.
     * <p>
     * Извлекает формулу из {@link FieldDefinition#formula()} и вычисляет её.
     * Если поле не COMPUTED или формула пуста — возвращает {@code null}.
     * <p>
     * Используется в {@code MarkingEngine} (US7.11) для подстановки значений
     * вычисляемых полей при обходе правила маркировки.
     *
     * @param field  определение поля
     * @param values значения полей карточки
     * @return вычисленное строковое значение или {@code null}
     */
    String evaluateFieldDefinition(FieldDefinition field, Map<String, Object> values);
}

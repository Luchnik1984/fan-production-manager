package com.fanproduction.template.formula;

import com.fanproduction.template.formula.ast.Expression;

import java.util.Map;

/**
 * Движок исполнения формул DSL.
 * <p>
 * Содержит методы парсинга (через {@code FormulaParser}) и интерпретации AST.
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
}

package com.fanproduction.template.formula.ast;

import java.util.List;

/**
 * Вызов функции: {@code FLOOR(arg)}, {@code DIVIDE(a, b)}.
 * <p>
 * Имя функции нормализовано к верхнему регистру при парсинге.
 */
public record FunctionExpression(
        String name,
        List<Expression> arguments
) implements Expression {
}

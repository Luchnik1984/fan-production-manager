package com.fanproduction.template.formula.ast;

import java.util.Map;

/**
 * Словарь: {@code &#123;380: 'D', 220: 'E'&#125;}.
 * <p>
 * Ключи — строки или числа. Значения — произвольные выражения.
 * Используется в функции {@code MAP} для преобразования значений.
 */
public record MapExpression(Map<Object, Expression> entries) implements Expression {
}
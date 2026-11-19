package com.fanproduction.template.formula.ast;

/**
 * Литерал: строка, число или boolean.
 */
public record LiteralExpression(Object value) implements Expression {
}

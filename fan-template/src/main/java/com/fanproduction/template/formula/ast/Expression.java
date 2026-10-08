package com.fanproduction.template.formula.ast;

/**
 * Узел AST формулы.
 * <p>
 * Использует sealed-иерархию (Java 17) для гарантии исчерпывающего
 * pattern matching в интерпретаторе.
 */
public sealed interface Expression
        permits LiteralExpression, FieldExpression, FunctionExpression, MapExpression {
}
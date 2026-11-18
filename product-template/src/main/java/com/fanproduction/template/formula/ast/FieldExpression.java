package com.fanproduction.template.formula.ast;

/**
 * Ссылка на поле карточки: {@code FIELD('wheelSize')}.
 */
public record FieldExpression(String fieldName) implements Expression {
}

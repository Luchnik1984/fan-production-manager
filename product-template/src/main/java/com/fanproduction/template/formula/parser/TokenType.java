package com.fanproduction.template.formula.parser;

/**
 * Тип токена в DSL формул.
 */
public enum TokenType {
    /** Строковый литерал в одинарных кавычках: {@code 'text'}. */
    STRING,
    /** Числовой литерал: {@code 10}, {@code 3.14}. */
    NUMBER,
    /** Идентификатор (имя функции или ключевое слово): {@code FLOOR}, {@code FIELD}, {@code true}. */
    IDENTIFIER,
    /** Открывающая скобка {@code (}. */
    LPAREN,
    /** Закрывающая скобка {@code )}. */
    RPAREN,
    /** Открывающая фигурная скобка {@code &#123;}. */
    LBRACE,
    /** Закрывающая фигурная скобка {@code &#125;}. */
    RBRACE,
    /** Запятая {@code ,}. */
    COMMA,
    /** Двоеточие {@code :}. */
    COLON,
    /** Конец ввода. */
    EOF
}

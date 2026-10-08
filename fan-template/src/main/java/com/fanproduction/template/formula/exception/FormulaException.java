package com.fanproduction.template.formula.exception;

/**
 * Исключение при работе с формулами DSL.
 * <p>
 * Возникает при синтаксических ошибках, неверных аргументах,
 * превышении лимитов (длина формулы, глубина AST).
 */
public class FormulaException extends RuntimeException {

    public FormulaException(String message) {
        super(message);
    }

    public FormulaException(String message, Throwable cause) {
        super(message, cause);
    }
}

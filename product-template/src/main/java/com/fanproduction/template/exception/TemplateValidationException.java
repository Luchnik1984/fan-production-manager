package com.fanproduction.template.exception;

/**
 * Исключение валидации в конструкторе шаблонов.
 * <p>
 * Используется для ошибок бизнес-валидации: несоответствие типов,
 * дубликаты, неверные ссылки на поля, ошибки в правилах маркировки.
 * <p>
 * Обрабатывается в {@code GlobalExceptionHandler} как HTTP 400 (Bad Request).
 */

public class TemplateValidationException extends RuntimeException {

    public TemplateValidationException(String message) {
        super(message);
    }

    public TemplateValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

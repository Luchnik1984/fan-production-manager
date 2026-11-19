package com.fanproduction.services.exception;

/**
 * Исключение при ошибках создания/обновления FAN-карточки.
 * Наследуется от IllegalArgumentException → 400 в GlobalExceptionHandler.
 */
public class DynamicCardCreationException extends IllegalArgumentException {

    public DynamicCardCreationException(String message) {
        super(message);
    }

    public DynamicCardCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

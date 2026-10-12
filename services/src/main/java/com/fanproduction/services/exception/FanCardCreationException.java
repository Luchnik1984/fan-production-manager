package com.fanproduction.services.exception;

/**
 * Исключение при ошибках создания/обновления FAN-карточки.
 * Наследуется от IllegalArgumentException → 400 в GlobalExceptionHandler.
 */
public class FanCardCreationException extends IllegalArgumentException {

    public FanCardCreationException(String message) {
        super(message);
    }

    public FanCardCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

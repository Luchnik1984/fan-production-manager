package com.fanproduction.api.exception;

import com.fanproduction.api.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обработчик ошибок валидации (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Обработчик для IllegalStateException (бизнес-ошибки, конфликты)
     * Возвращает статус 409 (Conflict) с понятным сообщением
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Обработчик для BadCredentialsException (неверные учетные данные)
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Обработчик для IllegalArgumentException
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Обработчик для SecurityException (доступ запрещен)
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Обработчик для нарушения уникальности (DataIntegrityViolationException)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String errorMsg = ex.getMessage();

        // Ранний return: если сообщения нет, возвращаем стандартную ошибку
        if (errorMsg == null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Ошибка сохранения данных"));
        }

        // Switch expression для определения типа ошибки
        String message = switch (getErrorType(errorMsg)) {
            case FULL_MARKING -> extractEntityType(errorMsg) + " с такой маркировкой уже существует";
            case COMPONENT_VENDOR_CODE -> "Компонент с таким артикулом уже существует";
            case MATERIAL_DUPLICATE -> "Материал с такими параметрами уже существует";
            case GENERIC_UNIQUE -> "Запись с такими данными уже существует";
            case UNKNOWN -> "Ошибка сохранения данных";
        };

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message));
    }

    /**
     * Типы ошибок для switch expression
     */
    private enum ErrorType {
        FULL_MARKING,
        COMPONENT_VENDOR_CODE,
        MATERIAL_DUPLICATE,
        GENERIC_UNIQUE,
        UNKNOWN
    }

    /**
     * Определяет тип ошибки по тексту сообщения
     */
    private ErrorType getErrorType(String errorMsg) {
        if (errorMsg.contains("full_marking")) {
            return ErrorType.FULL_MARKING;
        }
        if (errorMsg.contains("idx_component_vendor_code_unique")) {
            return ErrorType.COMPONENT_VENDOR_CODE;
        }
        if (errorMsg.contains("idx_material_unique")) {
            return ErrorType.MATERIAL_DUPLICATE;
        }
        if (errorMsg.contains("unique") || errorMsg.contains("UNIQUE")) {
            return ErrorType.GENERIC_UNIQUE;
        }
        return ErrorType.UNKNOWN;
    }

    /**
     * Извлекает название сущности из имени индекса
     */
    private String extractEntityType(String errorMsg) {
        if (errorMsg.contains("motor_card")) return "Электродвигатель";
        if (errorMsg.contains("motor_wheel_card")) return "Мотор-колесо";
        if (errorMsg.contains("radial_wheel_card")) return "Радиальное колесо";
        if (errorMsg.contains("axial_wheel_card")) return "Осевое колесо";
        if (errorMsg.contains("cup_card")) return "Стакан";
        if (errorMsg.contains("accessory_card")) return "Комплектующее";
        return "Изделие";
    }

    /**
     * Обработчик для всех остальных исключений
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Внутренняя ошибка сервера"));
    }
}
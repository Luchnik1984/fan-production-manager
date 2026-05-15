package com.fanproduction.api.exception;

import com.fanproduction.api.dto.response.ApiResponse;
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
     * Обработчик для BadCredentialsException (неверные учетные данные или неактивный аккаунт)
     * Возвращает статус 401 (Unauthorized) с понятным сообщением
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        // Возвращаем статус 401 (Unauthorized) с сообщением из исключения
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
     * Обработчик для нарушения уникальности
     */

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = "Ошибка сохранения данных";
        String errorMsg = ex.getMessage();

        if (errorMsg != null) {
            // Определяем тип сущности по имени индекса
            if (errorMsg.contains("full_marking")) {
                String entityType = extractEntityType(errorMsg);
                message = entityType + " с такой маркировкой уже существует";
            } else if (errorMsg.contains("idx_component_vendor_code_unique")) {
                message = "Компонент с таким артикулом уже существует";
            } else if (errorMsg.contains("idx_material_unique")) {
                message = "Материал с такими параметрами уже существует";
            } else if (errorMsg.contains("unique") || errorMsg.contains("UNIQUE")) {
                message = "Запись с такими данными уже существует";
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(message));
    }

    /**
     * Извлекает название сущности из имени индекса
     * Пример: idx_motor_card_full_marking -> Электродвигатель
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
        ex.printStackTrace();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Внутренняя ошибка сервера: " + ex.getMessage()));
    }
}
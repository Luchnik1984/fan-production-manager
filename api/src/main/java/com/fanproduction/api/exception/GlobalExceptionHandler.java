package com.fanproduction.api.exception;

import com.fanproduction.api.dto.response.ApiResponse;
import com.fanproduction.template.exception.TemplateValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Глобальный обработчик исключений REST API.
 * <p>
 * Возвращает {@link ApiResponse} с корректным HTTP-кодом:
 * <ul>
 *   <li><b>400</b> — ошибки валидации, некорректный JSON, отсутствуют обязательные параметры.</li>
 *   <li><b>401</b> — неверные учётные данные.</li>
 *   <li><b>403</b> — доступ запрещён (нет прав).</li>
 *   <li><b>404</b> — запрошенный ресурс не найден.</li>
 *   <li><b>405</b> — неподдерживаемый HTTP-метод.</li>
 *   <li><b>409</b> — конфликт (уникальность, нарушение бизнес-правил).</li>
 *   <li><b>415</b> — неподдерживаемый тип содержимого.</li>
 *   <li><b>500</b> — внутренняя ошибка сервера (логируется полностью).</li>
 * </ul>
 * <p>
 * Клиентские ошибки (400, 403, 404, 405, 415) логируются на уровне
 * {@code warn} без stacktrace, чтобы не засорять логи.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    /**
     * Ошибки валидации @Valid.
     * <p>
     * <b>Важно:</b> формат ответа — {@code Map<String, String>} (field → message),
     * а не {@code ApiResponse}. Это контракт с GUI-клиентом, который парсит
     * ошибки построчно и подсвечивает поля формы.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * Ошибки валидации TemplateValidationException (шаблоны вентиляторов).
     */
    @ExceptionHandler(TemplateValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleTemplateValidationException(
            TemplateValidationException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ==========================================================
    // БИЗНЕС-ЛОГИКА
    // ==========================================================

    /**
     * Конфликт бизнес-логики (например, «нельзя удалить, используется»).
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Ошибки аргументов (например, «поле обязательно»).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ==========================================================
    // БЕЗОПАСНОСТЬ
    // ==========================================================

    /**
     * Неверные учётные данные (неверный email или пароль).
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(BadCredentialsException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * Пользователь аутентифицирован, но нет прав на операцию.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Доступ запрещён: недостаточно прав"));
    }

    /**
     * Программные проверки безопасности (не имеют отношения к Spring Security).
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(SecurityException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // ==========================================================
    // БАЗА ДАННЫХ
    // ==========================================================

    /**
     * Нарушение уникальности и целостности БД.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        String errorMsg = ex.getMessage();

        if (errorMsg == null) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Ошибка сохранения данных"));
        }

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

    private enum ErrorType {
        FULL_MARKING,
        COMPONENT_VENDOR_CODE,
        MATERIAL_DUPLICATE,
        GENERIC_UNIQUE,
        UNKNOWN
    }

    private ErrorType getErrorType(String errorMsg) {
        if (errorMsg.contains("full_marking")) return ErrorType.FULL_MARKING;
        if (errorMsg.contains("idx_component_vendor_code_unique")) return ErrorType.COMPONENT_VENDOR_CODE;
        if (errorMsg.contains("idx_material_unique")) return ErrorType.MATERIAL_DUPLICATE;
        if (errorMsg.contains("unique") || errorMsg.contains("UNIQUE")) return ErrorType.GENERIC_UNIQUE;
        return ErrorType.UNKNOWN;
    }

    private String extractEntityType(String errorMsg) {
        if (errorMsg.contains("motor_card")) return "Электродвигатель";
        if (errorMsg.contains("motor_wheel_card")) return "Мотор-колесо";
        if (errorMsg.contains("radial_wheel_card")) return "Радиальное колесо";
        if (errorMsg.contains("axial_wheel_card")) return "Осевое колесо";
        if (errorMsg.contains("cup_card")) return "Стакан";
        if (errorMsg.contains("accessory_card")) return "Комплектующее";
        if (errorMsg.contains("fan_card")) return "Вентилятор";
        return "Изделие";
    }

    // ==========================================================
    // КЛИЕНТСКИЕ ОШИБКИ HTTP (не логируем как error)
    // ==========================================================

    /**
     * Неподдерживаемый HTTP-метод (например, POST на GET-only endpoint).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(
                        "Метод " + ex.getMethod() + " не поддерживается для этого эндпоинта"));
    }

    /**
     * Неподдерживаемый Content-Type (например, отправлен text/plain вместо JSON).
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .body(ApiResponse.error(
                        "Неподдерживаемый тип содержимого. Ожидается application/json"));
    }

    /**
     * Некорректный JSON в теле запроса.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMessageNotReadable(
            HttpMessageNotReadableException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Некорректный JSON в теле запроса"));
    }

    /**
     * Неверный тип параметра запроса (например, ?id=abc вместо ?id=123).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Неверный тип параметра '" + ex.getName() + "': " + ex.getMessage()));
    }

    /**
     * Не передан обязательный параметр запроса.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(
            MissingServletRequestParameterException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(
                        "Отсутствует обязательный параметр: '" + ex.getParameterName() + "'"));
    }

    /**
     * Ресурс не найден (Spring Boot 3.2+).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Запрошенный ресурс не найден"));
    }

    // ==========================================================
    // FALLBACK — ВНУТРЕННЯЯ ОШИБКА
    // ==========================================================

    /**
     * Все остальные исключения.
     * <p>
     * Это настоящие 500-е — логируем полный stacktrace + URI запроса,
     * чтобы понимать источник проблемы.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {} {}: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Внутренняя ошибка сервера"));
    }
}
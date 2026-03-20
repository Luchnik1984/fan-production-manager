package com.fanproduction.core.exception;

import com.fanproduction.core.entity.UserEntity;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

public class ValidationException  extends RuntimeException {
    private final Set<ConstraintViolation<UserEntity>> violations;

    public ValidationException(Set<ConstraintViolation<UserEntity>> violations) {
        super("Ошибка валидации");
        this.violations = violations;
    }

    public Set<ConstraintViolation<UserEntity>> getViolations() {
        return violations;
    }
}

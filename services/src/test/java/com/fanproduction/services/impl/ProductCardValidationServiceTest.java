package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.enums.CardTemplateType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты валидации обязательных полей карточек.
 */
@DisplayName("ProductCardValidationService")
class ProductCardValidationServiceTest {

    private ProductCardValidationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductCardValidationServiceImpl();
    }

    // ==========================================================
    // MOTOR — валидация на create
    // ==========================================================

    @Test
    @DisplayName("MOTOR: все поля заполнены — ок")
    void motorAllFieldsPresent() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("series", "АИР");
        fields.put("motorType", "100L");
        fields.put("poles", 2);
        fields.put("powerKw", 5.5);
        fields.put("mountingType", "IM1081");

        assertThatCode(() -> service.validateForCreate(CardTemplateType.MOTOR, fields))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("MOTOR: нет mountingType — ошибка")
    void motorMissingMountingType() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("series", "АИР");
        fields.put("motorType", "100L");
        fields.put("poles", 2);
        fields.put("powerKw", 5.5);
        // mountingType отсутствует

        assertThatThrownBy(() -> service.validateForCreate(CardTemplateType.MOTOR, fields))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mountingType");
    }

    @Test
    @DisplayName("MOTOR: mountingType пустая строка — ошибка")
    void motorEmptyMountingType() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("series", "АИР");
        fields.put("motorType", "100L");
        fields.put("poles", 2);
        fields.put("powerKw", 5.5);
        fields.put("mountingType", "   ");

        assertThatThrownBy(() -> service.validateForCreate(CardTemplateType.MOTOR, fields))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mountingType");
    }

    @Test
    @DisplayName("MOTOR: нет motorType — ошибка")
    void motorMissingMotorType() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("series", "АИР");
        fields.put("poles", 2);
        fields.put("powerKw", 5.5);
        fields.put("mountingType", "IM1081");

        assertThatThrownBy(() -> service.validateForCreate(CardTemplateType.MOTOR, fields))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motorType");
    }

    @Test
    @DisplayName("MOTOR: пустой Map — ошибка")
    void motorEmptyMap() {
        assertThatThrownBy(() -> service.validateForCreate(CardTemplateType.MOTOR, Map.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("MOTOR: null Map — ошибка")
    void motorNullMap() {
        assertThatThrownBy(() -> service.validateForCreate(CardTemplateType.MOTOR, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ==========================================================
    // MOTOR_WHEEL
    // ==========================================================

    @Test
    @DisplayName("MOTOR_WHEEL: все поля — ок")
    void motorWheelAllFields() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("manufacturerMarking", "RE280F-4D-AC0E");
        fields.put("poles", 4);
        fields.put("voltageCode", "D");
        fields.put("powerKw", 0.32);
        fields.put("size", 310);

        assertThatCode(() -> service.validateForCreate(CardTemplateType.MOTOR_WHEEL, fields))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("MOTOR_WHEEL: нет manufacturerMarking — ошибка")
    void motorWheelMissingMarking() {
        Map<String, Object> fields = new HashMap<>();
        fields.put("poles", 4);
        fields.put("voltageCode", "D");
        fields.put("powerKw", 0.32);
        fields.put("size", 310);

        assertThatThrownBy(() -> service.validateForCreate(CardTemplateType.MOTOR_WHEEL, fields))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("manufacturerMarking");
    }

    // ==========================================================
    // ТИПЫ БЕЗ ВАЛИДАЦИИ
    // ==========================================================

    @Test
    @DisplayName("FAN: не валидируется здесь (валидация в FanCardService)")
    void fanNotValidated() {
        assertThatCode(() -> service.validateForCreate(CardTemplateType.FAN, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("RADIAL_WHEEL: не валидируется в первом приближении")
    void radialWheelNotValidated() {
        assertThatCode(() -> service.validateForCreate(CardTemplateType.RADIAL_WHEEL, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("AXIAL_WHEEL: не валидируется в первом приближении")
    void axialWheelNotValidated() {
        assertThatCode(() -> service.validateForCreate(CardTemplateType.AXIAL_WHEEL, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("CUP: не валидируется")
    void cupNotValidated() {
        assertThatCode(() -> service.validateForCreate(CardTemplateType.CUP, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("ACCESSORY: не валидируется")
    void accessoryNotValidated() {
        assertThatCode(() -> service.validateForCreate(CardTemplateType.ACCESSORY, Map.of()))
                .doesNotThrowAnyException();
    }

    // ==========================================================
    // UPDATE — merged state
    // ==========================================================

    @Test
    @DisplayName("Update MOTOR: existing полный, new пустой — ок")
    void updateMotorExistingComplete() {
        MotorCardEntity existing = new MotorCardEntity();
        existing.setSeries("АИР");
        existing.setMotorType("100L");
        existing.setPoles(2);
        existing.setPowerKw(5.5);
        existing.setMountingType("IM1081");

        assertThatCode(() -> service.validateForUpdate(
                CardTemplateType.MOTOR, existing, Map.of()))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Update MOTOR: existing неполный, new не покрывает — ошибка")
    void updateMotorExistingIncomplete() {
        MotorCardEntity existing = new MotorCardEntity();
        existing.setSeries("АИР");
        // motorType = null
        existing.setPoles(2);
        existing.setPowerKw(5.5);
        existing.setMountingType("IM1081");

        assertThatThrownBy(() -> service.validateForUpdate(
                CardTemplateType.MOTOR, existing, Map.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motorType");
    }

    @Test
    @DisplayName("Update MOTOR: existing неполный, new дополняет — ок")
    void updateMotorExistingIncompleteNewFills() {
        MotorCardEntity existing = new MotorCardEntity();
        existing.setSeries("АИР");
        // motorType = null
        existing.setPoles(2);
        existing.setPowerKw(5.5);
        existing.setMountingType("IM1081");

        Map<String, Object> newFields = new HashMap<>();
        newFields.put("motorType", "100L");

        assertThatCode(() -> service.validateForUpdate(
                CardTemplateType.MOTOR, existing, newFields))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Update MOTOR: newFields с пустым значением затирает existing — ошибка")
    void updateMotorNewClearsExisting() {
        MotorCardEntity existing = new MotorCardEntity();
        existing.setSeries("АИР");
        existing.setMotorType("100L");
        existing.setPoles(2);
        existing.setPowerKw(5.5);
        existing.setMountingType("IM1081");

        Map<String, Object> newFields = new HashMap<>();
        newFields.put("motorType", "");   // пустая строка

        assertThatThrownBy(() -> service.validateForUpdate(
                CardTemplateType.MOTOR, existing, newFields))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("motorType");
    }
}

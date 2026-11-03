package com.fanproduction.services.catalog;

import com.fanproduction.core.dto.FieldInfoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты {@link CardFieldsRegistry} (US9.9.a).
 */
@DisplayName("CardFieldsRegistry")
class CardFieldsRegistryTest {

    private CardFieldsRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new CardFieldsRegistry();
    }

    // ==========================================================
    // MOTOR
    // ==========================================================

    @Test
    @DisplayName("MOTOR: содержит ключевые поля")
    void motorContainsKeyFields() {
        List<FieldInfoDto> fields = registry.getFields("MOTOR");
        assertThat(fields).extracting(FieldInfoDto::key)
                .contains("series", "motorType", "poles", "powerKw",
                        "mountingType", "voltage", "fullMarking");
    }

    @Test
    @DisplayName("MOTOR: poles — INTEGER, powerKw — DOUBLE")
    void motorTypes() {
        List<FieldInfoDto> fields = registry.getFields("MOTOR");
        FieldInfoDto poles = find(fields, "poles");
        FieldInfoDto powerKw = find(fields, "powerKw");
        assertThat(poles.type()).isEqualTo("INTEGER");
        assertThat(powerKw.type()).isEqualTo("DOUBLE");
    }

    // ==========================================================
    // MOTOR_WHEEL
    // ==========================================================

    @Test
    @DisplayName("MOTOR_WHEEL: содержит ключевые поля")
    void motorWheelContainsKeyFields() {
        List<FieldInfoDto> fields = registry.getFields("MOTOR_WHEEL");
        assertThat(fields).extracting(FieldInfoDto::key)
                .contains("manufacturerMarking", "bladeType", "size",
                        "poles", "voltageCode", "fullMarking");
    }

    // ==========================================================
    // RADIAL_WHEEL
    // ==========================================================

    @Test
    @DisplayName("RADIAL_WHEEL: содержит size и fullMarking")
    void radialWheelContainsKeyFields() {
        List<FieldInfoDto> fields = registry.getFields("RADIAL_WHEEL");
        assertThat(fields).extracting(FieldInfoDto::key)
                .contains("size", "fullMarking", "bladeType", "wheelFormula");
    }

    // ==========================================================
    // AXIAL_WHEEL
    // ==========================================================

    @Test
    @DisplayName("AXIAL_WHEEL: содержит size и wheelDiameter")
    void axialWheelContainsKeyFields() {
        List<FieldInfoDto> fields = registry.getFields("AXIAL_WHEEL");
        assertThat(fields).extracting(FieldInfoDto::key)
                .contains("size", "wheelDiameter", "bladeMaterial", "fullMarking");
    }

    // ==========================================================
    // COMPONENT
    // ==========================================================

    @Test
    @DisplayName("COMPONENT: содержит name, designation, vendorCode")
    void componentContainsKeyFields() {
        List<FieldInfoDto> fields = registry.getFields("COMPONENT");
        assertThat(fields).extracting(FieldInfoDto::key)
                .contains("name", "designation", "vendorCode", "weightKg", "material");
    }

    // ==========================================================
    // РЕГИСТРОНЕЗАВИСИМОСТЬ
    // ==========================================================

    @Test
    @DisplayName("Регистронезависимый ввод: motor == MOTOR")
    void caseInsensitive() {
        assertThat(registry.getFields("motor"))
                .hasSameSizeAs(registry.getFields("MOTOR"));
    }

    // ==========================================================
    // NEGATIVE
    // ==========================================================

    @Test
    @DisplayName("Неизвестный тип → пустой список")
    void unknownType() {
        assertThat(registry.getFields("UNKNOWN")).isEmpty();
        assertThat(registry.getFields(null)).isEmpty();
        assertThat(registry.getFields("")).isEmpty();
    }

    // ==========================================================
    // УТИЛИТЫ
    // ==========================================================

    private FieldInfoDto find(List<FieldInfoDto> fields, String key) {
        return fields.stream()
                .filter(f -> key.equals(f.key()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Поле не найдено: " + key));
    }
}


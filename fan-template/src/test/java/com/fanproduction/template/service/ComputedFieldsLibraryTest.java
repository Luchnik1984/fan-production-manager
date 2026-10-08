package com.fanproduction.template.service;

import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты библиотеки вычисляемых полей.
 */
@DisplayName("ComputedFieldsLibrary")
class ComputedFieldsLibraryTest {

    private ComputedFieldsLibraryImpl library;

    @BeforeEach
    void setUp() {
        library = new ComputedFieldsLibraryImpl();
        library.init();
    }

    // ==========================================================
    // ИНИЦИАЛИЗАЦИЯ
    // ==========================================================

    @Test
    @DisplayName("должна зарегистрировать 14 системных полей")
    void shouldRegister14SystemFields() {
        assertThat(library.getRegisteredNames()).hasSize(14);
        assertThat(library.getRegisteredNames()).contains(
                "wheelSizeMarking", "voltageCode", "executionMarking",
                "sizeMarking", "powerMarking", "bladeMarking",
                "wheelDiameter", "wheelFormulaAxial", "wheelFormulaRadial",
                "fireproofMarking", "explosionMarking",
                "climateTypeMarking", "positionMarking", "connectionTypeMarking"
        );
    }

    // ==========================================================
    // voltageCode
    // ==========================================================

    @Test
    @DisplayName("voltageCode: 220 → E, 380 → D")
    void voltageCode_shouldMap220ToE_and380ToD() {
        assertThat(library.evaluate("voltageCode", Map.of("voltage", 220))).isEqualTo("E");
        assertThat(library.evaluate("voltageCode", Map.of("voltage", 380))).isEqualTo("D");
        assertThat(library.evaluate("voltageCode", Map.of("voltage", 660))).isNull();
    }

    @Test
    @DisplayName("voltageCode: null если voltage не задан")
    void voltageCode_shouldReturnNullIfVoltageMissing() {
        assertThat(library.evaluate("voltageCode", Map.of())).isNull();
    }

    // ==========================================================
    // wheelSizeMarking
    // ==========================================================

    @Test
    @DisplayName("wheelSizeMarking: 315 → 31, 225 → 22 (размер в мм)")
    void wheelSizeMarking_shouldFloorDivision() {
        assertThat(library.evaluate("wheelSizeMarking", Map.of("wheelSize", 315))).isEqualTo("31");
        assertThat(library.evaluate("wheelSizeMarking", Map.of("wheelSize", 225))).isEqualTo("22");
    }

    // ==========================================================
    // sizeMarking / powerMarking
    // ==========================================================

    @Test
    @DisplayName("sizeMarking: целые числа без запятой, дробные — с запятой")
    void sizeMarking_shouldFormatNumbers() {
        assertThat(library.evaluate("sizeMarking", Map.of("size", 5.0))).isEqualTo("5");
        assertThat(library.evaluate("sizeMarking", Map.of("size", 5.6))).isEqualTo("5,6");
    }

    @Test
    @DisplayName("powerMarking: 5.5 → 5,5")
    void powerMarking_shouldFormatDecimal() {
        assertThat(library.evaluate("powerMarking", Map.of("powerKw", 5.5))).isEqualTo("5,5");
        assertThat(library.evaluate("powerMarking", Map.of("powerKw", 11.0))).isEqualTo("11");
    }

    // ==========================================================
    // wheelDiameter
    // ==========================================================

    @Test
    @DisplayName("wheelDiameter: 6.3 с подрезкой 1% → 624")
    void wheelDiameter_shouldComputeWithTrim() {
        Map<String, Object> values = new HashMap<>();
        values.put("size", 6.3);
        values.put("trimCoefficient", 1.0);

        assertThat(library.evaluate("wheelDiameter", values)).isEqualTo("624");
    }

    @Test
    @DisplayName("wheelDiameter: без подрезки → size * 100")
    void wheelDiameter_shouldDefaultToZeroTrim() {
        assertThat(library.evaluate("wheelDiameter", Map.of("size", 5.6))).isEqualTo("560");
    }

    // ==========================================================
    // wheelFormulaAxial
    // ==========================================================

    @Test
    @DisplayName("wheelFormulaAxial: полный набор полей → формула")
    void wheelFormulaAxial_shouldBuildFormula() {
        Map<String, Object> values = new HashMap<>();
        values.put("wheelDiameter", "624");
        values.put("bladeCount", 3);
        values.put("maxBladeCount", 9);
        values.put("bladeName", "4Z");
        values.put("bladeAngle", 43);
        values.put("bladeMaterial", "PAG");

        assertThat(library.evaluate("wheelFormulaAxial", values))
                .isEqualTo("624/3-9/4Z/43/PAG");
    }

    @Test
    @DisplayName("wheelFormulaAxial: не хватает поля → null")
    void wheelFormulaAxial_shouldReturnNullIfIncomplete() {
        Map<String, Object> values = new HashMap<>();
        values.put("wheelDiameter", "624");
        values.put("bladeCount", 3);
        // остальные отсутствуют

        assertThat(library.evaluate("wheelFormulaAxial", values)).isNull();
    }

    // ==========================================================
    // wheelFormulaRadial
    // ==========================================================

    @Test
    @DisplayName("wheelFormulaRadial: полный набор → формула")
    void wheelFormulaRadial_shouldBuildFormula() {
        Map<String, Object> values = new HashMap<>();
        values.put("bladeType", "N");
        values.put("wheelCode", "14");
        values.put("frontDiskMod", "B");
        values.put("wheelWidth", "027");
        values.put("bladeCount", 6);
        values.put("bladeLengthCoeff", "1.03");

        assertThat(library.evaluate("wheelFormulaRadial", values))
                .isEqualTo("N.14/B.027/6/1.03");
    }

    // ==========================================================
    // executionMarking
    // ==========================================================

    @Test
    @DisplayName("executionMarking: generalPurpose → C")
    void executionMarking_shouldReturnCForGeneralPurpose() {
        assertThat(library.evaluate("executionMarking",
                Map.of("generalPurpose", true))).isEqualTo("C");
    }

    @Test
    @DisplayName("executionMarking: fireproof → F-2/400 по умолчанию")
    void executionMarking_shouldReturnDefaultFireproof() {
        assertThat(library.evaluate("executionMarking",
                Map.of("fireproof", true))).isEqualTo("F-2/400");
    }

    @Test
    @DisplayName("executionMarking: explosionProof → 1Ex d IIC T4 Gb по умолчанию")
    void executionMarking_shouldReturnDefaultExplosion() {
        assertThat(library.evaluate("executionMarking",
                Map.of("explosionProof", true))).isEqualTo("1Ex d IIC T4 Gb");
    }

    // ==========================================================
    // fireproofMarking
    // ==========================================================

    @Test
    @DisplayName("fireproofMarking: F-2/600 при явных параметрах")
    void fireproofMarking_shouldUseExplicitTimeAndTemp() {
        Map<String, Object> values = new HashMap<>();
        values.put("fireproof", true);
        values.put("fireproofTime", 2);
        values.put("maxTemperature", 600);

        assertThat(library.evaluate("fireproofMarking", values)).isEqualTo("F-2/600");
    }

    @Test
    @DisplayName("fireproofMarking: null если fireproof = false")
    void fireproofMarking_shouldReturnNullIfNotFireproof() {
        assertThat(library.evaluate("fireproofMarking",
                Map.of("fireproof", false))).isNull();
    }

    // ==========================================================
    // connectionTypeMarking
    // ==========================================================

    @Test
    @DisplayName("connectionTypeMarking: HA+CA → HACA")
    void connectionTypeMarking_shouldCombineFlags() {
        Map<String, Object> values = new HashMap<>();
        values.put("hasHa", true);
        values.put("hasCa", true);

        assertThat(library.evaluate("connectionTypeMarking", values)).isEqualTo("HACA");
    }

    @Test
    @DisplayName("connectionTypeMarking: null если оба флага false")
    void connectionTypeMarking_shouldReturnNullIfBothFalse() {
        assertThat(library.evaluate("connectionTypeMarking", Map.of())).isNull();
    }

    // ==========================================================
    // РЕГИСТРАЦИЯ / РАСШИРЕНИЕ
    // ==========================================================

    @Test
    @DisplayName("register: можно зарегистрировать новое поле")
    void register_shouldAddNewField() {
        library.register("customField", values -> "CUSTOM");

        assertThat(library.isRegistered("customField")).isTrue();
        assertThat(library.evaluate("customField", Map.of())).isEqualTo("CUSTOM");
    }

    @Test
    @DisplayName("register: повторная регистрация бросает исключение")
    void register_shouldThrowOnDuplicate() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> library.register("voltageCode", values -> "X")
        );
    }

    @Test
    @DisplayName("unregister: удаляет поле")
    void unregister_shouldRemoveField() {
        assertThat(library.unregister("voltageCode")).isTrue();
        assertThat(library.isRegistered("voltageCode")).isFalse();
        assertThat(library.evaluate("voltageCode", Map.of("voltage", 380))).isNull();
    }

    // ==========================================================
    // ОБРАБОТКА ОШИБОК
    // ==========================================================

    @Test
    @DisplayName("evaluate: незарегистрированное поле → null")
    void evaluate_shouldReturnNullForUnknownField() {
        assertThat(library.evaluate("unknownField", Map.of())).isNull();
    }

    @Test
    @DisplayName("evaluate: ошибка внутри функции не пробрасывается")
    void evaluate_shouldSwallowExceptions() {
        library.register("throwingField", values -> {
            throw new RuntimeException("Boom!");
        });

        assertThat(library.evaluate("throwingField", Map.of())).isNull();
    }
}

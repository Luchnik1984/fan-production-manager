package com.fanproduction.template.formula;

import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты движка формул на контрольных примерах из ТЗ.
 * <p>
 * Проверяют, что реальные формулы, используемые в маркировках
 * вентиляторов, работают корректно.
 */
@DisplayName("FormulaEngine — интеграция с контрольными примерами")
class FormulaIntegrationTest {

    private FormulaEngine engine;

    @BeforeEach
    void setUp() {
        FormulaFunctions functions = new FormulaFunctions();
        functions.init();

        ComputedFieldsLibraryImpl computedFields = new ComputedFieldsLibraryImpl();
        computedFields.init();

        engine = new FormulaEngineImpl(functions, computedFields);
    }

    // ==========================================================
    // КОНТРОЛЬНЫЙ ПРИМЕР 1: VRK-PatAIR-P-40-20-4-220
    // ==========================================================

    @Test
    @DisplayName("P-серия: VRK-PatAIR-P-40-20-4-220")
    void ductFanP() {
        Map<String, Object> values = new HashMap<>();
        values.put("series", "P");
        values.put("ductSize", "40-20");
        values.put("poles", 4);
        values.put("voltage", 220);

        String formula = "CONCAT(" +
                "'VRK-PatAIR-', " +
                "FIELD('series'), '-', " +
                "FIELD('ductSize'), '-', " +
                "FIELD('poles'), '-', " +
                "FIELD('voltage')" +
                ")";

        assertThat(engine.evaluateToString(formula, values))
                .isEqualTo("VRK-PatAIR-P-40-20-4-220");
    }

    // ==========================================================
    // КОНТРОЛЬНЫЙ ПРИМЕР 2: VRK-PatAIR-PKV-50-30/22.2D
    // ==========================================================

    @Test
    @DisplayName("PKV-серия: VRK-PatAIR-PKV-50-30/22.2D")
    void ductFanPKV() {
        Map<String, Object> values = new HashMap<>();
        values.put("series", "PKV");
        values.put("ductSize", "50-30");
        values.put("wheelSize", 225);   // размер колеса в мм
        values.put("poles", 2);
        values.put("voltage", 380);

        // Формула:
        // CONCAT( 'VRK-PatAIR-', series, '-', ductSize, '/',
        //         FLOOR(DIVIDE(wheelSize, 10)), '.',
        //         poles, MAP(voltage, {380: 'D', 220: 'E'}) )
        String formula = "CONCAT(" +
                "'VRK-PatAIR-', " +
                "FIELD('series'), '-', " +
                "FIELD('ductSize'), '/', " +
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10)), '.', " +
                "FIELD('poles'), " +
                "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})" +
                ")";

        assertThat(engine.evaluateToString(formula, values))
                .isEqualTo("VRK-PatAIR-PKV-50-30/22.2D");
    }

    // ==========================================================
    // КОНТРОЛЬНЫЙ ПРИМЕР 3: VO-PatAIR-5.6-C-3/9-5.5-2-У1
    // ==========================================================

    @Test
    @DisplayName("Осевой: VO-PatAIR-5.6-C-3/9-5.5-2-У1")
    void axialFan() {
        Map<String, Object> values = new HashMap<>();
        values.put("size", 5.6);
        values.put("executionMarking", "C");
        values.put("bladeCount", 3);
        values.put("bladeSlots", 9);
        values.put("powerKw", 5.5);
        values.put("poles", 2);
        values.put("climateType", "У1");

        String formula = "CONCAT(" +
                "'VO-PatAIR-', " +
                "FORMAT_NUMBER(FIELD('size')), '-', " +
                "FIELD('executionMarking'), '-', " +
                "FIELD('bladeCount'), '/', " +
                "FIELD('bladeSlots'), '-', " +
                "FORMAT_NUMBER(FIELD('powerKw')), '-', " +
                "FIELD('poles'), '-', " +
                "FIELD('climateType')" +
                ")";

        assertThat(engine.evaluateToString(formula, values))
                .isEqualTo("VO-PatAIR-5,6-C-3/9-5,5-2-У1");
    }

    // ==========================================================
    // ЧЕРЕЗ COMPUTED-ПОЛЯ
    // ==========================================================

    @Test
    @DisplayName("evaluateFieldDefinition: COMPUTED-поле через формулу")
    void evaluateFieldDefinition() {
        FieldDefinition field = new FieldDefinition(
                "wheelSizeMarking",
                "Маркировка размера",
                FieldType.COMPUTED,
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1,
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10))"
        );

        assertThat(engine.evaluateFieldDefinition(field, Map.of("wheelSize", 225)))
                .isEqualTo("22");
        assertThat(engine.evaluateFieldDefinition(field, Map.of("wheelSize", 315)))
                .isEqualTo("31");
    }

    @Test
    @DisplayName("evaluateFieldDefinition: пустая формула → null")
    void evaluateFieldDefinitionEmptyFormula() {
        FieldDefinition field = new FieldDefinition(
                "x", "X", FieldType.TEXT, false,
                null, null, null, null, null, null,
                null, null, null, null, null, 1,
                null
        );

        assertThat(engine.evaluateFieldDefinition(field, Map.of())).isNull();
    }

    @Test
    @DisplayName("evaluateFieldDefinition: некорректная формула → null (без падения)")
    void evaluateFieldDefinitionBrokenFormula() {
        FieldDefinition field = new FieldDefinition(
                "x", "X", FieldType.COMPUTED, false,
                null, null, null, null, null, null,
                null, null, null, null, null, 1,
                "FLOOR("   // синтаксическая ошибка
        );

        assertThat(engine.evaluateFieldDefinition(field, Map.of())).isNull();
    }

    // ==========================================================
    // ИСПОЛЬЗОВАНИЕ СИСТЕМНЫХ ПОЛЕЙ ЧЕРЕЗ FIELD
    // ==========================================================

    @Test
    @DisplayName("FIELD('voltageCode') — системное вычисляемое поле")
    void useSystemComputedField() {
        Map<String, Object> values = Map.of("voltage", 380);

        assertThat(engine.evaluateToString("FIELD('voltageCode')", values))
                .isEqualTo("D");
    }

    @Test
    @DisplayName("FIELD('wheelDiameter') — системное вычисляемое поле")
    void useSystemWheelDiameter() {
        Map<String, Object> values = new HashMap<>();
        values.put("size", 6.3);
        values.put("trimCoefficient", 1.0);

        assertThat(engine.evaluateToString("FIELD('wheelDiameter')", values))
                .isEqualTo("624");
    }

    @Test
    @DisplayName("FIELD('executionMarking') — системное вычисляемое поле")
    void useSystemExecutionMarking() {
        Map<String, Object> values = Map.of("generalPurpose", true);
        assertThat(engine.evaluateToString("FIELD('executionMarking')", values))
                .isEqualTo("C");
    }

    // ==========================================================
    // ГИБКОСТЬ: изменение формулы без пересборки
    // ==========================================================

    @Test
    @DisplayName("Гибкость: та же карточка, разные формулы маркировки")
    void flexibilityDifferentFormulas() {
        Map<String, Object> values = new HashMap<>();
        values.put("series", "PKV");
        values.put("ductSize", "50-30");
        values.put("wheelSize", 225);
        values.put("poles", 2);
        values.put("voltage", 380);

        // Старая формула: PKV-50-30/22.2D
        String oldFormula = "CONCAT(" +
                "'VRK-PatAIR-', " +
                "FIELD('series'), '-', " +
                "FIELD('ductSize'), '/', " +
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10)), '.', " +
                "FIELD('poles'), " +
                "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})" +
                ")";

        // Новая формула: PatAIR-PKV-50-30-22-2-380
        String newFormula = "CONCAT(" +
                "'PatAIR-', " +
                "FIELD('series'), '-', " +
                "FIELD('ductSize'), '-', " +
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10)), '-', " +
                "FIELD('poles'), '-', " +
                "FIELD('voltage')" +
                ")";

        assertThat(engine.evaluateToString(oldFormula, values))
                .isEqualTo("VRK-PatAIR-PKV-50-30/22.2D");

        assertThat(engine.evaluateToString(newFormula, values))
                .isEqualTo("PatAIR-PKV-50-30-22-2-380");
    }
}

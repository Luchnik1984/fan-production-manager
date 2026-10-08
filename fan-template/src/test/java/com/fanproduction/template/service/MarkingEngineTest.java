package com.fanproduction.template.service;

import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.RuleElementType;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.FormulaEngineImpl;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import com.fanproduction.template.service.impl.MarkingEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Юнит-тесты движка маркировки (US7.11.a — LITERAL, SEPARATOR, FIELD).
 */
@DisplayName("MarkingEngine (US7.11.a)")
class MarkingEngineTest {

    private MarkingEngine engine;

    @BeforeEach
    void setUp() {
        FormulaFunctions functions = new FormulaFunctions();
        functions.init();

        ComputedFieldsLibraryImpl computedFields = new ComputedFieldsLibraryImpl();
        computedFields.init();

        FormulaEngine formulaEngine = new FormulaEngineImpl(functions, computedFields);

        engine = new MarkingEngineImpl(formulaEngine, computedFields);
    }

    // ==========================================================
    // ПУСТОЕ ПРАВИЛО
    // ==========================================================

    @Test
    @DisplayName("Пустое правило → пустая строка")
    void emptyRule() {
        assertThat(engine.evaluate(null, Map.of(), List.of())).isEmpty();
        assertThat(engine.evaluate(new MarkingRule(List.of()), Map.of(), List.of())).isEmpty();
    }

    // ==========================================================
    // LITERAL
    // ==========================================================

    @Nested
    @DisplayName("LITERAL")
    class LiteralTests {

        @Test
        @DisplayName("Один литерал")
        void singleLiteral() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("VRK-PatAIR")
            ));
            assertThat(engine.evaluate(rule, Map.of(), List.of()))
                    .isEqualTo("VRK-PatAIR");
        }

        @Test
        @DisplayName("Несколько литералов подряд")
        void multipleLiterals() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("A"),
                    literal("B"),
                    literal("C")
            ));
            assertThat(engine.evaluate(rule, Map.of(), List.of()))
                    .isEqualTo("ABC");
        }

        @Test
        @DisplayName("Литерал с null value игнорируется")
        void nullLiteral() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("A"),
                    new MarkingRuleElement(RuleElementType.LITERAL, null, null, null, null, null),
                    literal("B")
            ));
            assertThat(engine.evaluate(rule, Map.of(), List.of()))
                    .isEqualTo("AB");
        }
    }

    // ==========================================================
    // SEPARATOR
    // ==========================================================

    @Nested
    @DisplayName("SEPARATOR")
    class SeparatorTests {

        @Test
        @DisplayName("Дефис между литералами")
        void dash() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("A"),
                    separator("-"),
                    literal("B")
            ));
            assertThat(engine.evaluate(rule, Map.of(), List.of()))
                    .isEqualTo("A-B");
        }

        @Test
        @DisplayName("Слэш и точка")
        void slashAndDot() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("A"),
                    separator("/"),
                    literal("B"),
                    separator("."),
                    literal("C")
            ));
            assertThat(engine.evaluate(rule, Map.of(), List.of()))
                    .isEqualTo("A/B.C");
        }
    }

    // ==========================================================
    // FIELD
    // ==========================================================

    @Nested
    @DisplayName("FIELD")
    class FieldTests {

        @Test
        @DisplayName("Поле из values")
        void fieldFromValues() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("Series="),
                    new MarkingRuleElement(RuleElementType.FIELD, null, "series", null, null, null)
            ));

            assertThat(engine.evaluate(rule, Map.of("series", "P"), List.of()))
                    .isEqualTo("Series=P");
        }

        @Test
        @DisplayName("Отсутствующее поле пропускается")
        void missingField() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("A"),
                    new MarkingRuleElement(RuleElementType.FIELD, null, "missing", null, null, null),
                    literal("B")
            ));

            assertThat(engine.evaluate(rule, Map.of(), List.of()))
                    .isEqualTo("AB");
        }

        @Test
        @DisplayName("Пустое значение поля пропускается")
        void emptyFieldValue() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("A"),
                    new MarkingRuleElement(RuleElementType.FIELD, null, "x", null, null, null),
                    literal("B")
            ));

            assertThat(engine.evaluate(rule, Map.of("x", ""), List.of()))
                    .isEqualTo("AB");
        }

        @Test
        @DisplayName("Число приводится к строке")
        void numberField() {
            MarkingRule rule = new MarkingRule(List.of(
                    new MarkingRuleElement(RuleElementType.FIELD, null, "poles", null, null, null)
            ));

            assertThat(engine.evaluate(rule, Map.of("poles", 4), List.of()))
                    .isEqualTo("4");
        }

        @Test
        @DisplayName("Дробное число как есть")
        void decimalField() {
            MarkingRule rule = new MarkingRule(List.of(
                    new MarkingRuleElement(RuleElementType.FIELD, null, "size", null, null, null)
            ));

            assertThat(engine.evaluate(rule, Map.of("size", 5.6), List.of()))
                    .isEqualTo("5.6");
        }
    }

    // ==========================================================
    // COMPUTED-ПОЛЯ
    // ==========================================================

    @Nested
    @DisplayName("COMPUTED-поля")
    class ComputedFieldTests {

        @Test
        @DisplayName("COMPUTED-поле через формулу")
        void computedFieldWithFormula() {
            FieldDefinition computed = new FieldDefinition(
                    "wheelSizeMarking", "Маркировка размера", FieldType.COMPUTED,
                    false, null, null, null, null, null, null,
                    null, null, null, null, null, 1,
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10))"
            );

            MarkingRule rule = new MarkingRule(List.of(
                    new MarkingRuleElement(RuleElementType.FIELD, null,
                            "wheelSizeMarking", null, null, null)
            ));

            String result = engine.evaluate(
                    rule,
                    Map.of("wheelSize", 225),
                    List.of(computed)
            );

            assertThat(result).isEqualTo("22");
        }

        @Test
        @DisplayName("Системное вычисляемое поле через FIELD")
        void systemComputedField() {
            MarkingRule rule = new MarkingRule(List.of(
                    new MarkingRuleElement(RuleElementType.FIELD, null,
                            "voltageCode", null, null, null)
            ));

            assertThat(engine.evaluate(rule, Map.of("voltage", 380), List.of()))
                    .isEqualTo("D");
        }
    }

    // ==========================================================
    // КОМПЛЕКСНЫЕ (без CONDITION)
    // ==========================================================

    @Nested
    @DisplayName("Комплексные")
    class ComplexTests {

        @Test
        @DisplayName("Правило: литерал + field + separator + field")
        void simpleCombination() {
            MarkingRule rule = new MarkingRule(List.of(
                    literal("VRK-PatAIR-"),
                    new MarkingRuleElement(RuleElementType.FIELD, null, "series", null, null, null),
                    separator("-"),
                    new MarkingRuleElement(RuleElementType.FIELD, null, "ductSize", null, null, null)
            ));

            Map<String, Object> values = new HashMap<>();
            values.put("series", "P");
            values.put("ductSize", "40-20");

            assertThat(engine.evaluate(rule, values, List.of()))
                    .isEqualTo("VRK-PatAIR-P-40-20");
        }

        @Test
        @DisplayName("Использование MarkingContext напрямую")
        void directContextUsage() {
            FormulaFunctions functions = new FormulaFunctions();
            functions.init();
            ComputedFieldsLibraryImpl computedFields = new ComputedFieldsLibraryImpl();
            computedFields.init();
            FormulaEngine formulaEngine = new FormulaEngineImpl(functions, computedFields);

            MarkingContext context = new MarkingContext(
                    Map.of("series", "PKV"),
                    List.of(),
                    formulaEngine,
                    computedFields
            );

            MarkingRule rule = new MarkingRule(List.of(
                    literal("X-"),
                    new MarkingRuleElement(RuleElementType.FIELD, null, "series", null, null, null)
            ));

            assertThat(engine.evaluate(rule, context)).isEqualTo("X-PKV");
        }
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private static MarkingRuleElement literal(String value) {
        return new MarkingRuleElement(RuleElementType.LITERAL, value, null, null, null, null);
    }

    private static MarkingRuleElement separator(String value) {
        return new MarkingRuleElement(RuleElementType.SEPARATOR, value, null, null, null, null);
    }
}

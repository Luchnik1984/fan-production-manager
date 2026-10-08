package com.fanproduction.template.formula;

import com.fanproduction.template.formula.exception.FormulaException;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты движка формул.
 */
@DisplayName("FormulaEngine")
class FormulaEngineTest {

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
    // ЛИТЕРАЛЫ
    // ==========================================================

    @Nested
    @DisplayName("Литералы")
    class Literals {

        @Test
        @DisplayName("Строка")
        void string() {
            assertThat(engine.evaluateToString("'hello'", Map.of())).isEqualTo("hello");
        }

        @Test
        @DisplayName("Целое число")
        void integer() {
            assertThat(engine.evaluateToString("10", Map.of())).isEqualTo("10");
        }

        @Test
        @DisplayName("Дробное число")
        void decimal() {
            assertThat(engine.evaluateToString("3.14", Map.of())).isEqualTo("3.14");
        }

        @Test
        @DisplayName("boolean true")
        void boolTrue() {
            assertThat(engine.evaluate("true", Map.of())).isEqualTo(true);
        }
    }

    // ==========================================================
    // FIELD
    // ==========================================================

    @Nested
    @DisplayName("FIELD")
    class Fields {

        @Test
        @DisplayName("FIELD из values")
        void fieldFromValues() {
            assertThat(engine.evaluateToString("FIELD('name')", Map.of("name", "Test")))
                    .isEqualTo("Test");
        }

        @Test
        @DisplayName("FIELD отсутствует → null")
        void fieldMissing() {
            assertThat(engine.evaluateToString("FIELD('unknown')", Map.of())).isNull();
        }

        @Test
        @DisplayName("FIELD — системное вычисляемое поле (voltageCode)")
        void fieldComputedSystem() {
            Map<String, Object> values = Map.of("voltage", 380);
            assertThat(engine.evaluateToString("FIELD('voltageCode')", values)).isEqualTo("D");
        }
    }

    // ==========================================================
    // МАТЕМАТИЧЕСКИЕ
    // ==========================================================

    @Nested
    @DisplayName("Математические")
    class MathFunctions {

        @Test
        @DisplayName("FLOOR(10.7) → 10")
        void floor() {
            assertThat(engine.evaluateToString("FLOOR(10.7)", Map.of())).isEqualTo("10");
        }

        @Test
        @DisplayName("CEIL(10.2) → 11")
        void ceil() {
            assertThat(engine.evaluateToString("CEIL(10.2)", Map.of())).isEqualTo("11");
        }

        @Test
        @DisplayName("ROUND(10.5) → 11")
        void round() {
            assertThat(engine.evaluateToString("ROUND(10.5)", Map.of())).isEqualTo("11");
        }

        @Test
        @DisplayName("DIVIDE(225, 10) → 22.5")
        void divide() {
            assertThat(engine.evaluateToString("DIVIDE(225, 10)", Map.of())).isEqualTo("22.5");
        }

        @Test
        @DisplayName("DIVIDE на ноль → FormulaException")
        void divideByZero() {
            assertThatThrownBy(() -> engine.evaluate("DIVIDE(10, 0)", Map.of()))
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Деление на ноль");
        }

        @Test
        @DisplayName("MULTIPLY(3, 4) → 12")
        void multiply() {
            assertThat(engine.evaluateToString("MULTIPLY(3, 4)", Map.of())).isEqualTo("12");
        }

        @Test
        @DisplayName("ADD(3, 4) → 7")
        void add() {
            assertThat(engine.evaluateToString("ADD(3, 4)", Map.of())).isEqualTo("7");
        }

        @Test
        @DisplayName("SUBTRACT(10, 3) → 7")
        void subtract() {
            assertThat(engine.evaluateToString("SUBTRACT(10, 3)", Map.of())).isEqualTo("7");
        }

        @Test
        @DisplayName("FLOOR(DIVIDE(FIELD('wheelSize'), 10)) — контрольный пример")
        void floorDivideField() {
            Map<String, Object> values = Map.of("wheelSize", 225);
            assertThat(engine.evaluateToString(
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10))", values)).isEqualTo("22");
        }
    }

    // ==========================================================
    // СТРОКОВЫЕ
    // ==========================================================

    @Nested
    @DisplayName("Строковые")
    class StringFunctions {

        @Test
        @DisplayName("CONCAT('A', 'B', 'C') → ABC")
        void concat() {
            assertThat(engine.evaluateToString("CONCAT('A', 'B', 'C')", Map.of()))
                    .isEqualTo("ABC");
        }

        @Test
        @DisplayName("CONCAT с числом и null")
        void concatWithNumberAndNull() {
            Map<String, Object> values = Map.of("x", 10);
            assertThat(engine.evaluateToString(
                    "CONCAT('val=', FIELD('x'), '-', FIELD('missing'))", values))
                    .isEqualTo("val=10-");
        }

        @Test
        @DisplayName("UPPER('abc') → ABC")
        void upper() {
            assertThat(engine.evaluateToString("UPPER('abc')", Map.of())).isEqualTo("ABC");
        }

        @Test
        @DisplayName("LOWER('ABC') → abc")
        void lower() {
            assertThat(engine.evaluateToString("LOWER('ABC')", Map.of())).isEqualTo("abc");
        }

        @Test
        @DisplayName("TRIM('  x  ') → x")
        void trim() {
            assertThat(engine.evaluateToString("TRIM('  x  ')  ", Map.of())).isEqualTo("x");
        }

        @Test
        @DisplayName("FORMAT_NUMBER(5.6) → 5,6")
        void formatNumber() {
            assertThat(engine.evaluateToString("FORMAT_NUMBER(5.6)", Map.of())).isEqualTo("5,6");
        }

        @Test
        @DisplayName("FORMAT_NUMBER(5.0) → 5")
        void formatNumberInteger() {
            assertThat(engine.evaluateToString("FORMAT_NUMBER(5.0)", Map.of())).isEqualTo("5");
        }
    }

    // ==========================================================
    // ЛОГИЧЕСКИЕ
    // ==========================================================

    @Nested
    @DisplayName("Логические")
    class LogicalFunctions {

        @Test
        @DisplayName("IF(true, 'A', 'B') → A")
        void ifTrue() {
            assertThat(engine.evaluateToString("IF(true, 'A', 'B')", Map.of())).isEqualTo("A");
        }

        @Test
        @DisplayName("IF(false, 'A', 'B') → B")
        void ifFalse() {
            assertThat(engine.evaluateToString("IF(false, 'A', 'B')", Map.of())).isEqualTo("B");
        }

        @Test
        @DisplayName("IF(EQUALS(FIELD('x'), 1), 'A', 'B')")
        void ifWithEquals() {
            assertThat(engine.evaluateToString(
                    "IF(EQUALS(FIELD('x'), 1), 'A', 'B')", Map.of("x", 1))).isEqualTo("A");
            assertThat(engine.evaluateToString(
                    "IF(EQUALS(FIELD('x'), 1), 'A', 'B')", Map.of("x", 2))).isEqualTo("B");
        }

        @Test
        @DisplayName("EQUALS('a', 'a') → true")
        void equals() {
            assertThat(engine.evaluate("EQUALS('a', 'a')", Map.of())).isEqualTo(true);
        }

        @Test
        @DisplayName("EQUALS(380, '380') → true (числовое сравнение)")
        void equalsNumberString() {
            assertThat(engine.evaluate("EQUALS(380, '380')", Map.of())).isEqualTo(true);
        }

        @Test
        @DisplayName("NOT_EQUALS(1, 2) → true")
        void notEquals() {
            assertThat(engine.evaluate("NOT_EQUALS(1, 2)", Map.of())).isEqualTo(true);
        }

        @Test
        @DisplayName("GREATER(5, 3) → true")
        void greater() {
            assertThat(engine.evaluate("GREATER(5, 3)", Map.of())).isEqualTo(true);
        }

        @Test
        @DisplayName("LESS(5, 3) → false")
        void less() {
            assertThat(engine.evaluate("LESS(5, 3)", Map.of())).isEqualTo(false);
        }

        @Test
        @DisplayName("AND(true, true, false) → false")
        void and() {
            assertThat(engine.evaluate("AND(true, true, false)", Map.of())).isEqualTo(false);
        }

        @Test
        @DisplayName("OR(false, false, true) → true")
        void or() {
            assertThat(engine.evaluate("OR(false, false, true)", Map.of())).isEqualTo(true);
        }

        @Test
        @DisplayName("NOT(true) → false")
        void not() {
            assertThat(engine.evaluate("NOT(true)", Map.of())).isEqualTo(false);
        }
    }

    // ==========================================================
    // ПРОВЕРКИ
    // ==========================================================

    @Nested
    @DisplayName("Проверки")
    class ChecksFunctions {

        @Test
        @DisplayName("EMPTY(null) → true")
        void empty() {
            assertThat(engine.evaluate("EMPTY(FIELD('x'))", Map.of())).isEqualTo(true);
        }

        @Test
        @DisplayName("EMPTY('abc') → false")
        void notEmptyString() {
            assertThat(engine.evaluate("EMPTY(FIELD('x'))", Map.of("x", "abc")))
                    .isEqualTo(false);
        }

        @Test
        @DisplayName("NOT_EMPTY('x') → true")
        void notEmpty() {
            assertThat(engine.evaluate("NOT_EMPTY(FIELD('x'))", Map.of("x", "y")))
                    .isEqualTo(true);
        }

        @Test
        @DisplayName("DEFAULT(null, 'N/A') → N/A")
        void defaultFallback() {
            assertThat(engine.evaluateToString("DEFAULT(FIELD('x'), 'N/A')", Map.of()))
                    .isEqualTo("N/A");
        }

        @Test
        @DisplayName("DEFAULT('value', 'N/A') → value")
        void defaultPresent() {
            assertThat(engine.evaluateToString(
                    "DEFAULT(FIELD('x'), 'N/A')", Map.of("x", "value")))
                    .isEqualTo("value");
        }

        @Test
        @DisplayName("COALESCE(null, null, 'x') → x")
        void coalesce() {
            assertThat(engine.evaluateToString(
                    "COALESCE(FIELD('a'), FIELD('b'), 'x')", Map.of()))
                    .isEqualTo("x");
        }
    }

    // ==========================================================
    // MAP
    // ==========================================================

    @Nested
    @DisplayName("MAP")
    class MapFunctions {

        @Test
        @DisplayName("MAP(FIELD('voltage'), {380: 'D', 220: 'E'}) → D")
        void mapVoltage() {
            assertThat(engine.evaluateToString(
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})",
                    Map.of("voltage", 380))).isEqualTo("D");
        }

        @Test
        @DisplayName("MAP с отсутствующим ключом → null")
        void mapMissing() {
            assertThat(engine.evaluateToString(
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})",
                    Map.of("voltage", 660))).isNull();
        }

        @Test
        @DisplayName("MAP со строковыми ключами")
        void mapStringKeys() {
            assertThat(engine.evaluateToString(
                    "MAP(FIELD('type'), {'A': 'Alpha', 'B': 'Beta'})",
                    Map.of("type", "B"))).isEqualTo("Beta");
        }
    }

    // ==========================================================
    // КОМПЛЕКСНЫЕ
    // ==========================================================

    @Nested
    @DisplayName("Комплексные")
    class Complex {

        @Test
        @DisplayName("Реальная формула: FLOOR(DIVIDE(FIELD('wheelSize'), 10))")
        void realWheelSize() {
            assertThat(engine.evaluateToString(
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10))",
                    Map.of("wheelSize", 315))).isEqualTo("31");

            assertThat(engine.evaluateToString(
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10))",
                    Map.of("wheelSize", 225))).isEqualTo("22");
        }

        @Test
        @DisplayName("Реальная формула: напряжение 380 → D, 220 → E")
        void realVoltageCode() {
            assertThat(engine.evaluateToString(
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})",
                    Map.of("voltage", 380))).isEqualTo("D");
            assertThat(engine.evaluateToString(
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})",
                    Map.of("voltage", 220))).isEqualTo("E");
        }

        @Test
        @DisplayName("Комплексная: CONCAT с IF и FORMAT_NUMBER")
        void realComplexConcat() {
            Map<String, Object> values = new HashMap<>();
            values.put("series", "PKV");
            values.put("ductSize", "50-30");
            values.put("wheelSize", 225);
            values.put("voltage", 380);

            String formula = "CONCAT(" +
                    "FIELD('series'), '-', " +
                    "FIELD('ductSize'), '/', " +
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10)), '.', " +
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})" +
                    ")";

            assertThat(engine.evaluateToString(formula, values))
                    .isEqualTo("PKV-50-30/22.D");
        }

        @Test
        @DisplayName("Формула с пробелами")
        void withSpaces() {
            assertThat(engine.evaluateToString(
                    "  FLOOR( DIVIDE( FIELD('x') , 10 ) )  ",
                    Map.of("x", 225))).isEqualTo("22");
        }
    }

    // ==========================================================
    // ОШИБКИ
    // ==========================================================

    @Nested
    @DisplayName("Ошибки")
    class Errors {

        @Test
        @DisplayName("Неизвестная функция — исключение")
        void unknownFunction() {
            assertThatThrownBy(() -> engine.evaluate("UNKNOWN_FN(10)", Map.of()))
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Неизвестная функция");
        }

        @Test
        @DisplayName("Синтаксическая ошибка в формуле")
        void syntaxError() {
            assertThatThrownBy(() -> engine.evaluate("FLOOR(", Map.of()))
                    .isInstanceOf(FormulaException.class);
        }
    }
}
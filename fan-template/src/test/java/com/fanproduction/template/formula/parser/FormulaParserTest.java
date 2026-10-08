package com.fanproduction.template.formula.parser;

import com.fanproduction.template.formula.ast.Expression;
import com.fanproduction.template.formula.ast.FieldExpression;
import com.fanproduction.template.formula.ast.FunctionExpression;
import com.fanproduction.template.formula.ast.LiteralExpression;
import com.fanproduction.template.formula.ast.MapExpression;
import com.fanproduction.template.formula.exception.FormulaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты парсера формул.
 */
@DisplayName("FormulaParser")
class FormulaParserTest {

    // ==========================================================
    // ЛИТЕРАЛЫ
    // ==========================================================

    @Nested
    @DisplayName("Литералы")
    class Literals {

        @Test
        @DisplayName("Строковый литерал")
        void parseString() {
            Expression expr = new FormulaParser("'VRK-PatAIR'").parse();
            assertThat(expr).isEqualTo(new LiteralExpression("VRK-PatAIR"));
        }

        @Test
        @DisplayName("Целое число")
        void parseInt() {
            Expression expr = new FormulaParser("10").parse();
            assertThat(expr).isEqualTo(new LiteralExpression(10));
        }

        @Test
        @DisplayName("Дробное число")
        void parseDouble() {
            Expression expr = new FormulaParser("3.14").parse();
            assertThat(expr).isEqualTo(new LiteralExpression(3.14));
        }

        @Test
        @DisplayName("boolean true")
        void parseTrue() {
            Expression expr = new FormulaParser("true").parse();
            assertThat(expr).isEqualTo(new LiteralExpression(true));
        }

        @Test
        @DisplayName("boolean false")
        void parseFalse() {
            Expression expr = new FormulaParser("false").parse();
            assertThat(expr).isEqualTo(new LiteralExpression(false));
        }

        @Test
        @DisplayName("boolean TRUE в верхнем регистре")
        void parseTrueUpperCase() {
            Expression expr = new FormulaParser("TRUE").parse();
            assertThat(expr).isEqualTo(new LiteralExpression(true));
        }
    }

    // ==========================================================
    // FIELD
    // ==========================================================

    @Nested
    @DisplayName("FIELD")
    class Fields {

        @Test
        @DisplayName("FIELD('wheelSize')")
        void parseField() {
            Expression expr = new FormulaParser("FIELD('wheelSize')").parse();
            assertThat(expr).isEqualTo(new FieldExpression("wheelSize"));
        }

        @Test
        @DisplayName("FIELD без кавычек — ошибка")
        void parseFieldWithoutQuotes() {
            assertThatThrownBy(() -> new FormulaParser("FIELD(wheelSize)").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("имя поля в кавычках");
        }
    }

    // ==========================================================
    // ФУНКЦИИ
    // ==========================================================

    @Nested
    @DisplayName("Функции")
    class Functions {

        @Test
        @DisplayName("Функция без аргументов")
        void parseFunctionNoArgs() {
            Expression expr = new FormulaParser("NOW()").parse();
            assertThat(expr).isEqualTo(
                    new FunctionExpression("NOW", List.of()));
        }

        @Test
        @DisplayName("Функция с одним аргументом")
        void parseFunctionOneArg() {
            Expression expr = new FormulaParser("FLOOR(10)").parse();
            assertThat(expr).isEqualTo(
                    new FunctionExpression("FLOOR",
                            List.of(new LiteralExpression(10))));
        }

        @Test
        @DisplayName("Функция с двумя аргументами")
        void parseFunctionTwoArgs() {
            Expression expr = new FormulaParser("DIVIDE(FIELD('x'), 10)").parse();
            assertThat(expr).isEqualTo(
                    new FunctionExpression("DIVIDE", List.of(
                            new FieldExpression("x"),
                            new LiteralExpression(10)
                    )));
        }

        @Test
        @DisplayName("Вложенная функция: FLOOR(DIVIDE(FIELD('wheelSize'), 10))")
        void parseNestedFunction() {
            Expression expr = new FormulaParser(
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10))").parse();

            Expression expected = new FunctionExpression("FLOOR", List.of(
                    new FunctionExpression("DIVIDE", List.of(
                            new FieldExpression("wheelSize"),
                            new LiteralExpression(10)
                    ))
            ));

            assertThat(expr).isEqualTo(expected);
        }

        @Test
        @DisplayName("Имя функции нормализуется к верхнему регистру")
        void parseFunctionLowercase() {
            Expression expr = new FormulaParser("floor(10)").parse();
            assertThat(expr).isEqualTo(
                    new FunctionExpression("FLOOR",
                            List.of(new LiteralExpression(10))));
        }

        @Test
        @DisplayName("IF с тремя аргументами")
        void parseIf() {
            Expression expr = new FormulaParser(
                    "IF(EQUALS(FIELD('x'), 1), 'A', 'B')").parse();

            assertThat(expr).isInstanceOf(FunctionExpression.class);
            FunctionExpression fn = (FunctionExpression) expr;
            assertThat(fn.name()).isEqualTo("IF");
            assertThat(fn.arguments()).hasSize(3);
        }
    }

    // ==========================================================
    // MAP
    // ==========================================================

    @Nested
    @DisplayName("MAP")
    class MapTests {

        @Test
        @DisplayName("MAP(FIELD('voltage'), {380: 'D', 220: 'E'})")
        void parseMapFunction() {
            Expression expr = new FormulaParser(
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})").parse();

            assertThat(expr).isInstanceOf(FunctionExpression.class);
            FunctionExpression fn = (FunctionExpression) expr;
            assertThat(fn.name()).isEqualTo("MAP");
            assertThat(fn.arguments()).hasSize(2);

            Expression mapArg = fn.arguments().get(1);
            assertThat(mapArg).isInstanceOf(MapExpression.class);
            MapExpression map = (MapExpression) mapArg;
            assertThat(map.entries()).hasSize(2);
            assertThat(map.entries().get(380)).isEqualTo(new LiteralExpression("D"));
            assertThat(map.entries().get(220)).isEqualTo(new LiteralExpression("E"));
        }

        @Test
        @DisplayName("Пустой MAP {}")
        void parseEmptyMap() {
            Expression expr = new FormulaParser("{}").parse();
            assertThat(expr).isInstanceOf(MapExpression.class);
            assertThat(((MapExpression) expr).entries()).isEmpty();
        }

        @Test
        @DisplayName("MAP со строковыми ключами")
        void parseMapWithStringKeys() {
            Expression expr = new FormulaParser("{'a': 1, 'b': 2}").parse();
            assertThat(expr).isInstanceOf(MapExpression.class);
            MapExpression map = (MapExpression) expr;
            assertThat(map.entries()).hasSize(2);
        }

        @Test
        @DisplayName("Дублирующийся ключ в MAP — ошибка")
        void parseMapDuplicateKey() {
            assertThatThrownBy(() -> new FormulaParser("{1: 'a', 1: 'b'}").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Дублирующийся ключ");
        }
    }

    // ==========================================================
    // ОШИБКИ
    // ==========================================================

    @Nested
    @DisplayName("Ошибки")
    class Errors {

        @Test
        @DisplayName("Пустая формула — ошибка")
        void parseEmpty() {
            assertThatThrownBy(() -> new FormulaParser("").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("не может быть пустой");
        }

        @Test
        @DisplayName("Формула из пробелов — ошибка")
        void parseWhitespaceOnly() {
            assertThatThrownBy(() -> new FormulaParser("   ").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("не может быть пустой");
        }

        @Test
        @DisplayName("Незакрытая скобка — ошибка")
        void parseUnclosedParen() {
            assertThatThrownBy(() -> new FormulaParser("FLOOR(10").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Ожидалась ')'");
        }

        @Test
        @DisplayName("Лишний токен после выражения — ошибка")
        void parseExtraToken() {
            assertThatThrownBy(() -> new FormulaParser("10 20").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Неожиданный токен после выражения");
        }

        @Test
        @DisplayName("Недопустимый символ — ошибка")
        void parseInvalidChar() {
            assertThatThrownBy(() -> new FormulaParser("10 @ 20").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Недопустимый символ");
        }

        @Test
        @DisplayName("Незакрытая строка — ошибка")
        void parseUnclosedString() {
            assertThatThrownBy(() -> new FormulaParser("'abc").parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Незакрытая строка");
        }

        @Test
        @DisplayName("Превышение длины формулы — ошибка")
        void parseTooLong() {
            String longFormula = "'" + "x".repeat(5001) + "'";
            assertThatThrownBy(() -> new FormulaParser(longFormula).parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("слишком длинная");
        }

        @Test
        @DisplayName("Превышение глубины вложенности — ошибка")
        void parseTooDeep() {
            // Строим формулу с 60 уровнями вложенности: NOT(NOT(NOT(...TRUE...)))
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 60; i++) {
                sb.append("NOT(");
            }
            sb.append("TRUE");
            for (int i = 0; i < 60; i++) {
                sb.append(")");
            }

            assertThatThrownBy(() -> new FormulaParser(sb.toString()).parse())
                    .isInstanceOf(FormulaException.class)
                    .hasMessageContaining("Слишком глубокая вложенность");
        }
    }

    // ==========================================================
    // КОМПЛЕКСНЫЕ
    // ==========================================================

    @Nested
    @DisplayName("Комплексные примеры")
    class ComplexExamples {

        @Test
        @DisplayName("Реальная формула: FLOOR(DIVIDE(FIELD('wheelSize'), 10))")
        void realFormulaWheelSize() {
            Expression expr = new FormulaParser(
                    "FLOOR(DIVIDE(FIELD('wheelSize'), 10))").parse();
            assertThat(expr).isInstanceOf(FunctionExpression.class);
            assertThat(((FunctionExpression) expr).name()).isEqualTo("FLOOR");
        }

        @Test
        @DisplayName("Реальная формула: MAP(FIELD('voltage'), {380: 'D', 220: 'E'})")
        void realFormulaVoltage() {
            Expression expr = new FormulaParser(
                    "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})").parse();
            assertThat(expr).isInstanceOf(FunctionExpression.class);
            assertThat(((FunctionExpression) expr).name()).isEqualTo("MAP");
        }

        @Test
        @DisplayName("Реальная формула: IF(EQUALS(FIELD('x'), 1), 'A', 'B')")
        void realFormulaIf() {
            Expression expr = new FormulaParser(
                    "IF(EQUALS(FIELD('x'), 1), 'A', 'B')").parse();
            assertThat(expr).isInstanceOf(FunctionExpression.class);
            assertThat(((FunctionExpression) expr).name()).isEqualTo("IF");
        }

        @Test
        @DisplayName("Формула с пробелами")
        void formulaWithSpaces() {
            Expression expr = new FormulaParser(
                    "  FLOOR( DIVIDE( FIELD('x') , 10 ) )  ").parse();
            assertThat(expr).isInstanceOf(FunctionExpression.class);
        }

        @Test
        @DisplayName("Регистронезависимость имён функций")
        void formulaCaseInsensitive() {
            Expression expr1 = new FormulaParser("floor(10)").parse();
            Expression expr2 = new FormulaParser("FLOOR(10)").parse();
            Expression expr3 = new FormulaParser("Floor(10)").parse();
            assertThat(expr1).isEqualTo(expr2);
            assertThat(expr2).isEqualTo(expr3);
        }
    }
}


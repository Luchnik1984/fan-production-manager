package com.fanproduction.template.service;

import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.RuleElementType;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.FormulaEngineImpl;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import com.fanproduction.template.service.impl.TemplateValidationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Юнит-тесты валидатора шаблонов.
 */
@DisplayName("TemplateValidationService")
class TemplateValidationServiceTest {

    private TemplateValidationService validator;

    @BeforeEach
    void setUp() {
        FormulaFunctions functions = new FormulaFunctions();
        functions.init();

        ComputedFieldsLibraryImpl computedFields = new ComputedFieldsLibraryImpl();
        computedFields.init();

        FormulaEngine formulaEngine = new FormulaEngineImpl(functions, computedFields);

        validator = new TemplateValidationServiceImpl(formulaEngine);
    }

    // ==========================================================
    // КОРРЕКТНЫЙ ШАБЛОН
    // ==========================================================

    @Test
    @DisplayName("Корректный шаблон проходит валидацию")
    void validTemplate() {
        FanTemplateVersion version = new FanTemplateVersion();
        version.setFieldsJson(List.of(
                simpleField("series", FieldType.TEXT, 1),
                simpleField("ductSize", FieldType.TEXT, 2)
        ));
        version.setMarkingRuleJson(new MarkingRule(List.of(
                literal("VRK-PatAIR-"),
                field("series"),
                separator("-"),
                field("ductSize")
        )));

        assertThatCode(() -> validator.validate(version))
                .doesNotThrowAnyException();
        assertThat(validator.collectErrors(version)).isEmpty();
    }

    // ==========================================================
    // NULL / ПУСТОЙ
    // ==========================================================

    @Nested
    @DisplayName("Null и пустые")
    class NullTests {

        @Test
        @DisplayName("null version → ошибка")
        void nullVersion() {
            assertThat(validator.collectErrors(null))
                    .containsExactly("Версия шаблона не может быть null");
        }

        @Test
        @DisplayName("Пустой список полей → ошибка")
        void emptyFields() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of());
            version.setMarkingRuleJson(MarkingRule.empty());

            assertThat(validator.collectErrors(version))
                    .contains("Список полей не может быть пустым");
        }

        @Test
        @DisplayName("Пустое правило → ошибка")
        void emptyRule() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(simpleField("x", FieldType.TEXT, 1)));
            version.setMarkingRuleJson(new MarkingRule(List.of()));

            assertThat(validator.collectErrors(version))
                    .contains("Правило маркировки не может быть пустым");
        }
    }

    // ==========================================================
    // ПОЛЯ — ОШИБКИ
    // ==========================================================

    @Nested
    @DisplayName("Поля")
    class FieldTests {

        @Test
        @DisplayName("Дубликат key → ошибка")
        void duplicateKey() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    simpleField("x", FieldType.TEXT, 1),
                    simpleField("x", FieldType.TEXT, 2)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("Дублирующийся key"));
        }

        @Test
        @DisplayName("Поле без key → ошибка")
        void noKey() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition(null, "X", FieldType.TEXT, false,
                            null, null, null, null, null, null,
                            null, null, null, null, null, 1, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("отсутствует key"));
        }

        @Test
        @DisplayName("COMBOBOX без options → ошибка")
        void comboBoxNoOptions() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("poles", "Полюсность", FieldType.COMBOBOX,
                            true, null, null, null, null, null, null,
                            null, null, null, null, null, 1, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("COMBOBOX") && e.contains("options"));
        }

        @Test
        @DisplayName("SELECTABLE без referenceType → ошибка")
        void selectableNoRef() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("motorId", "ЭД", FieldType.SELECTABLE,
                            false, null, null, null, null, null, null,
                            null, null, null, null, null, 1, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("SELECTABLE") && e.contains("referenceType"));
        }

        @Test
        @DisplayName("SELECTABLE с недопустимым referenceType → ошибка")
        void selectableInvalidRef() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("x", "X", FieldType.SELECTABLE,
                            false, null, null, null, "UNKNOWN_TYPE", null, null,
                            null, null, null, null, null, 1, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("недопустимый referenceType"));
        }

        @Test
        @DisplayName("COMPUTED без formula → ошибка")
        void computedNoFormula() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("c", "C", FieldType.COMPUTED,
                            false, null, null, null, null, null, null,
                            null, null, null, null, null, 1, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("COMPUTED") && e.contains("formula"));
        }

        @Test
        @DisplayName("COMPUTED с неверной формулой → ошибка")
        void computedInvalidFormula() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("c", "C", FieldType.COMPUTED,
                            false, null, null, null, null, null, null,
                            null, null, null, null, null, 1, "FLOOR(")
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("ошибка в формуле"));
        }

        @Test
        @DisplayName("pullFrom на несуществующее поле → ошибка")
        void pullFromToMissing() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("motorId", "ЭД", FieldType.SELECTABLE,
                            false, null, null, null, "MOTOR", null, null,
                            false, Map.of("powerKw", "powerKw"), null, null, null, 1, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("pullFrom") && e.contains("powerKw"));
        }

        @Test
        @DisplayName("pullFrom на существующее поле → без ошибок")
        void pullFromToExisting() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(
                    new FieldDefinition("motorId", "ЭД", FieldType.SELECTABLE,
                            false, null, null, null, "MOTOR", null, null,
                            false, Map.of("powerKw", "powerKw"), null, null, null, 1, null),
                    new FieldDefinition("powerKw", "Мощность", FieldType.DOUBLE,
                            false, null, null, null, null, null, null,
                            null, null, null, null, null, 2, null)
            ));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThat(validator.collectErrors(version)).isEmpty();
        }
    }

    // ==========================================================
    // ПРАВИЛО МАРКИРОВКИ — ОШИБКИ
    // ==========================================================

    @Nested
    @DisplayName("Правило маркировки")
    class RuleTests {

        @Test
        @DisplayName("LITERAL без value → ошибка")
        void literalNoValue() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.LITERAL, null, null, null, null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("LITERAL без value"));
        }

        @Test
        @DisplayName("SEPARATOR без value → ошибка")
        void separatorNoValue() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.SEPARATOR, null, null, null, null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("SEPARATOR без value"));
        }

        @Test
        @DisplayName("FIELD без fieldKey → ошибка")
        void fieldNoKey() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.FIELD, null, null, null, null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("FIELD без fieldKey"));
        }

        @Test
        @DisplayName("CONDITION без condition → ошибка")
        void conditionNoCondition() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null, null, null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("CONDITION без condition"));
        }

        @Test
        @DisplayName("CONDITION без field → ошибка")
        void conditionNoField() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition(null, ConditionOperator.NOT_EMPTY, null, null),
                                    null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("CONDITION без field"));
        }

        @Test
        @DisplayName("CONDITION без operator → ошибка")
        void conditionNoOperator() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition("x", null, null, null),
                                    null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("CONDITION без operator"));
        }

        @Test
        @DisplayName("CONDITION EQUALS без value → ошибка")
        void conditionEqualsNoValue() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition("x", ConditionOperator.EQUALS, null, null),
                                    null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("EQUALS") && e.contains("value"));
        }

        @Test
        @DisplayName("CONDITION IN без values → ошибка")
        void conditionInNoValues() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition("x", ConditionOperator.IN, null, null),
                                    null, null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("IN") && e.contains("values"));
        }

        @Test
        @DisplayName("CONDITION NOT_EMPTY без value → не ошибка")
        void conditionNotEmptyNoValue() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition("x", ConditionOperator.NOT_EMPTY, null, null),
                                    List.of(literal("A")),
                                    null)
                    )));

            assertThat(validator.collectErrors(version)).isEmpty();
        }

        @Test
        @DisplayName("Вложенный CONDITION с ошибкой внутри")
        void nestedConditionError() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition("x", ConditionOperator.NOT_EMPTY, null, null),
                                    List.of(new MarkingRuleElement(
                                            RuleElementType.LITERAL, null, null, null, null, null)),
                                    null)
                    )));

            assertThat(validator.collectErrors(version))
                    .anyMatch(e -> e.contains("then[0]") && e.contains("LITERAL"));
        }

        @Test
        @DisplayName("Пустой sequence внутри CONDITION — не ошибка")
        void emptyConditionBranches() {
            FanTemplateVersion version = baseVersionWith(
                    new MarkingRule(List.of(
                            new MarkingRuleElement(RuleElementType.CONDITION, null, null,
                                    new Condition("x", ConditionOperator.NOT_EMPTY, null, null),
                                    null, null)
                    )));

            assertThat(validator.collectErrors(version)).isEmpty();
        }
    }

    // ==========================================================
    // ВЫБРОС ИСКЛЮЧЕНИЯ
    // ==========================================================

    @Nested
    @DisplayName("Исключение")
    class ExceptionTests {

        @Test
        @DisplayName("validate бросает TemplateValidationException при ошибках")
        void validateThrows() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of());
            version.setMarkingRuleJson(MarkingRule.empty());

            assertThatThrownBy(() -> validator.validate(version))
                    .isInstanceOf(TemplateValidationException.class)
                    .hasMessageContaining("Список полей не может быть пустым")
                    .hasMessageContaining("Правило маркировки не может быть пустым");
        }

        @Test
        @DisplayName("validate не бросает при корректном шаблоне")
        void validateDoesNotThrow() {
            FanTemplateVersion version = new FanTemplateVersion();
            version.setFieldsJson(List.of(simpleField("x", FieldType.TEXT, 1)));
            version.setMarkingRuleJson(new MarkingRule(List.of(literal("A"))));

            assertThatCode(() -> validator.validate(version)).doesNotThrowAnyException();
        }
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private static FieldDefinition simpleField(String key, FieldType type, Integer displayOrder) {
        return new FieldDefinition(
                key, "Label " + key, type,
                false, null, null, null, null, null, null,
                null, null, null, null, null, displayOrder, null
        );
    }

    private static FanTemplateVersion baseVersionWith(MarkingRule rule) {
        FanTemplateVersion version = new FanTemplateVersion();
        version.setFieldsJson(List.of(simpleField("x", FieldType.TEXT, 1)));
        version.setMarkingRuleJson(rule);
        return version;
    }

    private static MarkingRuleElement literal(String value) {
        return new MarkingRuleElement(RuleElementType.LITERAL, value, null, null, null, null);
    }

    private static MarkingRuleElement separator(String value) {
        return new MarkingRuleElement(RuleElementType.SEPARATOR, value, null, null, null, null);
    }

    private static MarkingRuleElement field(String key) {
        return new MarkingRuleElement(RuleElementType.FIELD, null, key, null, null, null);
    }
}

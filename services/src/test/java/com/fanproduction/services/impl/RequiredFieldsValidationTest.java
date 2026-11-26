package com.fanproduction.services.impl;

import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.service.MarkingEngine;
import com.fanproduction.services.exception.DynamicCardCreationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Обязательность полей с учётом видимости (US10.7.5d).
 * <p>
 * Регрессия: при партнёрском колесе поля хаба скрыты, но были обязательными
 * и блокировали сохранение карточки.
 */
class RequiredFieldsValidationTest {

    private static final Condition OWN_PRODUCTION =
            new Condition("isOwnProduction", ConditionOperator.EQUALS, true, null);

    private static DynamicCardServiceImpl serviceWithConditionResult(boolean conditionResult) {
        MarkingEngine engine = mock(MarkingEngine.class);
        when(engine.evaluateCondition(any(), any())).thenReturn(conditionResult);
        return new DynamicCardServiceImpl(null, null, null, null, null, engine, null, null, null);
    }

    private static FieldDefinition requiredField(String key, Condition visibleIf) {
        return new FieldDefinition(
                key, "Поле " + key, FieldType.NUMBER,
                true, null, null, null, null, null, null,
                null, null, visibleIf, null, null, null, null);
    }

    @Test
    @DisplayName("скрытое обязательное поле не блокирует сохранение")
    void hiddenRequiredFieldIsIgnored() {
        DynamicCardServiceImpl service = serviceWithConditionResult(false);

        assertThatCode(() -> service.validateRequiredFields(
                Map.of(), List.of(requiredField("maxBladeCount", OWN_PRODUCTION))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("видимое обязательное поле по-прежнему проверяется")
    void visibleRequiredFieldIsChecked() {
        DynamicCardServiceImpl service = serviceWithConditionResult(true);

        assertThatThrownBy(() -> service.validateRequiredFields(
                Map.of(), List.of(requiredField("maxBladeCount", OWN_PRODUCTION))))
                .isInstanceOf(DynamicCardCreationException.class)
                .hasMessageContaining("Обязательное поле не заполнено");
    }

    @Test
    @DisplayName("requiredIf управляет обязательностью вместо флага required")
    void requiredIfOverridesFlag() {
        DynamicCardServiceImpl requiredNow = serviceWithConditionResult(true);
        FieldDefinition field = new FieldDefinition(
                "hubDesignation", "Обозначение ступицы", FieldType.TEXT,
                false, null, null, null, null, null, null,
                null, null, OWN_PRODUCTION, OWN_PRODUCTION, null, null, null);

        assertThatThrownBy(() -> requiredNow.validateRequiredFields(Map.of(), List.of(field)))
                .isInstanceOf(DynamicCardCreationException.class);

        DynamicCardServiceImpl notRequired = serviceWithConditionResult(false);
        assertThatCode(() -> notRequired.validateRequiredFields(Map.of(), List.of(field)))
                .doesNotThrowAnyException();
    }
}

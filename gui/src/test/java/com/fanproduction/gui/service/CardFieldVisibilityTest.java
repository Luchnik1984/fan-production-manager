package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.response.DynamicFormFieldDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Проверка скрытия полей в просмотре карточки (US10.7.4c).
 */
class CardFieldVisibilityTest {

    private static DynamicFormFieldDto fieldWith(ConditionDto condition) {
        DynamicFormFieldDto field = new DynamicFormFieldDto();
        field.setKey("maxTemperature");
        field.setVisibleIf(condition);
        return field;
    }

    @Test
    @DisplayName("без условия поле видно")
    void noCondition() {
        assertThat(CardFieldVisibility.isVisible(fieldWith(null), Map.of())).isTrue();
    }

    @Test
    @DisplayName("исполнение C: огнестойкость и взрывозащита скрыты")
    void hiddenForGeneralExecution() {
        DynamicFormFieldDto fireField = fieldWith(
                new ConditionDto("executionCode", "EQUALS", "F — огнестойкость", null));

        assertThat(CardFieldVisibility.isVisible(fireField,
                Map.of("executionCode", "C — общего применения"))).isFalse();
    }

    @Test
    @DisplayName("исполнение F: поле огнестойкости видно")
    void visibleForFireExecution() {
        DynamicFormFieldDto fireField = fieldWith(
                new ConditionDto("executionCode", "EQUALS", "F — огнестойкость", null));

        assertThat(CardFieldVisibility.isVisible(fireField,
                Map.of("executionCode", "F — огнестойкость"))).isTrue();
    }

    @Test
    @DisplayName("NOT_EMPTY: пустое значение скрывает поле")
    void notEmpty() {
        DynamicFormFieldDto field = fieldWith(
                new ConditionDto("explosionMarking", "NOT_EMPTY", null, null));

        assertThat(CardFieldVisibility.isVisible(field, Map.of())).isFalse();
        assertThat(CardFieldVisibility.isVisible(field,
                Map.of("explosionMarking", "1Ex d IIC T4 Gb"))).isTrue();
    }
}

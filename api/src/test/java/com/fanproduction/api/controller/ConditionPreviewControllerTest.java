package com.fanproduction.api.controller;

import com.fanproduction.api.dto.request.preview.ConditionPreviewRequest;
import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.formula.FormulaEngineImpl;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import com.fanproduction.template.service.impl.MarkingEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты контроллера предпросмотра условий.
 */
@DisplayName("ConditionPreviewController")
class ConditionPreviewControllerTest {

    private ConditionPreviewController controller;

    @BeforeEach
    void setUp() {
        FormulaFunctions functions = new FormulaFunctions();
        functions.init();
        ComputedFieldsLibraryImpl computedFields = new ComputedFieldsLibraryImpl();
        computedFields.init();
        FormulaEngineImpl formulaEngine = new FormulaEngineImpl(functions, computedFields);
        MarkingEngineImpl markingEngine = new MarkingEngineImpl(
                formulaEngine, computedFields, null);
        controller = new ConditionPreviewController(markingEngine);
    }

    @Test
    @DisplayName("EQUALS true == true → matched")
    void equalsTrue() {
        Condition condition = new Condition("fireproof",
                ConditionOperator.EQUALS, true, null);
        var response = controller.previewCondition(new ConditionPreviewRequest(
                condition, Map.of("fireproof", true)
        ));
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().matched()).isTrue();
    }

    @Test
    @DisplayName("EQUALS true == false → not matched")
    void equalsFalse() {
        Condition condition = new Condition("fireproof",
                ConditionOperator.EQUALS, true, null);
        var response = controller.previewCondition(new ConditionPreviewRequest(
                condition, Map.of("fireproof", false)
        ));
        assertThat(response.getData().matched()).isFalse();
    }

    @Test
    @DisplayName("NOT_EMPTY на заполненном → matched")
    void notEmptyFilled() {
        Condition condition = new Condition("motorWheelId",
                ConditionOperator.NOT_EMPTY, null, null);
        var response = controller.previewCondition(new ConditionPreviewRequest(
                condition, Map.of("motorWheelId", 11)
        ));
        assertThat(response.getData().matched()).isTrue();
    }

    @Test
    @DisplayName("NOT_EMPTY на пустом → not matched")
    void notEmptyNull() {
        Condition condition = new Condition("motorWheelId",
                ConditionOperator.NOT_EMPTY, null, null);
        var response = controller.previewCondition(new ConditionPreviewRequest(
                condition, Map.of()
        ));
        assertThat(response.getData().matched()).isFalse();
    }

    @Test
    @DisplayName("IN: 380 в списке → matched")
    void inTrue() {
        Condition condition = new Condition("voltage",
                ConditionOperator.IN, null, List.of(220, 380));
        var response = controller.previewCondition(new ConditionPreviewRequest(
                condition, Map.of("voltage", 380)
        ));
        assertThat(response.getData().matched()).isTrue();
    }

    @Test
    @DisplayName("IN: 660 не в списке → not matched")
    void inFalse() {
        Condition condition = new Condition("voltage",
                ConditionOperator.IN, null, List.of(220, 380));
        var response = controller.previewCondition(new ConditionPreviewRequest(
                condition, Map.of("voltage", 660)
        ));
        assertThat(response.getData().matched()).isFalse();
    }
}

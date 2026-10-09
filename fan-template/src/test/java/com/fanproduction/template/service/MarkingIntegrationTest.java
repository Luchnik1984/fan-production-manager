package com.fanproduction.template.service;

import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.enums.RuleElementType;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.FormulaEngineImpl;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.service.impl.ComputedFieldsLibraryImpl;
import com.fanproduction.template.service.impl.MarkingEngineImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты движка маркировки на контрольных примерах из ТЗ.
 * <p>
 * Проверяют сборку реальных маркировок вентиляторов с использованием
 * CONDITION, COMPUTED-полей, MAP, IF.
 */
@DisplayName("MarkingEngine — контрольные примеры")
class MarkingIntegrationTest {

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
    // ПРИМЕР 1: VRK-PatAIR-P-40-20-4-220
    // ==========================================================

    @Test
    @DisplayName("Канальный P: VRK-PatAIR-P-40-20-4-220")
    void ductFanP() {
        // Правило: LITERAL('VRK-PatAIR') SEP '-' FIELD('series') SEP '-' FIELD('ductSize')
        //          SEP '-' FIELD('poles') SEP '-' FIELD('voltage')
        MarkingRule rule = new MarkingRule(List.of(
                literal("VRK-PatAIR"),
                separator("-"),
                field("series"),
                separator("-"),
                field("ductSize"),
                separator("-"),
                field("poles"),
                separator("-"),
                field("voltage")
        ));

        Map<String, Object> values = new HashMap<>();
        values.put("series", "P");
        values.put("ductSize", "40-20");
        values.put("poles", 4);
        values.put("voltage", 220);

        assertThat(engine.evaluate(rule, values, List.of()))
                .isEqualTo("VRK-PatAIR-P-40-20-4-220");
    }

    // ==========================================================
    // ПРИМЕР 2: VRK-PatAIR-PKV-50-30/22.2D
    // ==========================================================

    @Test
    @DisplayName("Канальный PKV: VRK-PatAIR-PKV-50-30/22.2D")
    void ductFanPKV() {
        // COMPUTED-поле: wheelSizeMarking = FLOOR(DIVIDE(FIELD('wheelSize'), 10))
        FieldDefinition wheelSizeMarking = new FieldDefinition(
                "wheelSizeMarking", "Маркировка размера", FieldType.COMPUTED,
                false, null, null, null, null, null, null,
                null, null, null, null, null, 10,
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10))"
        );

        // COMPUTED-поле: voltageCode = MAP(voltage, {380: 'D', 220: 'E'})
        FieldDefinition voltageCode = new FieldDefinition(
                "voltageCode", "Код напряжения", FieldType.COMPUTED,
                false, null, null, null, null, null, null,
                null, null, null, null, null, 11,
                "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})"
        );

        // Правило: LITERAL('VRK-PatAIR-') FIELD('series') SEP '-' FIELD('ductSize')
        //          SEP '/' FIELD('wheelSizeMarking') SEP '.' FIELD('poles') FIELD('voltageCode')
        MarkingRule rule = new MarkingRule(List.of(
                literal("VRK-PatAIR-"),
                field("series"),
                separator("-"),
                field("ductSize"),
                separator("/"),
                field("wheelSizeMarking"),
                separator("."),
                field("poles"),
                field("voltageCode")
        ));

        Map<String, Object> values = new HashMap<>();
        values.put("series", "PKV");
        values.put("ductSize", "50-30");
        values.put("wheelSize", 225);
        values.put("poles", 2);
        values.put("voltage", 380);

        assertThat(engine.evaluate(rule, values, List.of(wheelSizeMarking, voltageCode)))
                .isEqualTo("VRK-PatAIR-PKV-50-30/22.2D");
    }

    // ==========================================================
    // ПРИМЕР 3: VO-PatAIR-5.6-C-3/9-5.5-2-У1
    // ==========================================================

    @Test
    @DisplayName("Осевой: VO-PatAIR-5,6-C-3/9-5,5-2-У1")
    void axialFan() {
        // COMPUTED: sizeMarking = FORMAT_NUMBER(size)
        FieldDefinition sizeMarking = new FieldDefinition(
                "sizeMarking", "Размер", FieldType.COMPUTED,
                false, null, null, null, null, null, null,
                null, null, null, null, null, 10,
                "FORMAT_NUMBER(FIELD('size'))"
        );

        // COMPUTED: powerMarking = FORMAT_NUMBER(powerKw)
        FieldDefinition powerMarking = new FieldDefinition(
                "powerMarking", "Мощность", FieldType.COMPUTED,
                false, null, null, null, null, null, null,
                null, null, null, null, null, 11,
                "FORMAT_NUMBER(FIELD('powerKw'))"
        );

        MarkingRule rule = new MarkingRule(List.of(
                literal("VO-PatAIR-"),
                field("sizeMarking"),
                separator("-"),
                field("executionMarking"),
                separator("-"),
                field("bladeCount"),
                separator("/"),
                field("bladeSlots"),
                separator("-"),
                field("powerMarking"),
                separator("-"),
                field("poles"),
                separator("-"),
                field("climateType")
        ));

        Map<String, Object> values = new HashMap<>();
        values.put("size", 5.6);
        values.put("generalPurpose", true);    // → executionMarking = C
        values.put("bladeCount", 3);
        values.put("bladeSlots", 9);
        values.put("powerKw", 5.5);
        values.put("poles", 2);
        values.put("climateType", "У1");

        assertThat(engine.evaluate(rule, values, List.of(sizeMarking, powerMarking)))
                .isEqualTo("VO-PatAIR-5,6-C-3/9-5,5-2-У1");
    }

    // ==========================================================
    // ПРИМЕР 4: Условное добавление угла установки и направления
    // ==========================================================

    @Test
    @DisplayName("Радиальный с CONDITION: ...-У1-90-П и ...-У1 (без угла)")
    void radialFanWithOptionalAngle() {
        // Правило: LITERAL('VR-PatAIR-Vn-') FIELD('size')
        //          SEP '-' FIELD('climateType')
        //          CONDITION[position NOT_EMPTY: SEP '-' FIELD('position')]
        //          CONDITION[direction NOT_EMPTY: SEP '-' FIELD('direction')]
        MarkingRule rule = new MarkingRule(List.of(
                literal("VR-PatAIR-Vn-"),
                field("size"),
                separator("-"),
                field("climateType"),
                new MarkingRuleElement(
                        RuleElementType.CONDITION, null, null,
                        new Condition("angle", ConditionOperator.NOT_EMPTY, null, null),
                        List.of(separator("-"), field("angle")),
                        null
                ),
                new MarkingRuleElement(
                        RuleElementType.CONDITION, null, null,
                        new Condition("direction", ConditionOperator.NOT_EMPTY, null, null),
                        List.of(separator("-"), field("direction")),
                        null
                )
        ));

        // С углом и направлением
        Map<String, Object> withBoth = new HashMap<>();
        withBoth.put("size", 5.6);
        withBoth.put("climateType", "У1");
        withBoth.put("angle", 90);
        withBoth.put("direction", "П");

        assertThat(engine.evaluate(rule, withBoth, List.of()))
                .isEqualTo("VR-PatAIR-Vn-5.6-У1-90-П");

        // Без угла и направления
        Map<String, Object> withoutBoth = new HashMap<>();
        withoutBoth.put("size", 5.6);
        withoutBoth.put("climateType", "У1");

        assertThat(engine.evaluate(rule, withoutBoth, List.of()))
                .isEqualTo("VR-PatAIR-Vn-5.6-У1");
    }

    // ==========================================================
    // ПРИМЕР 5: IF для партнёрского/фирменного
    // ==========================================================

    @Test
    @DisplayName("Условный выбор: партнёрская vs фирменная маркировка")
    void partnerVsOwn() {
        // CONDITION[isPartner = true: FIELD('marking')]
        // CONDITION[isPartner = false: LITERAL('VRK-PatAIR') SEP '-' FIELD('series')]
        MarkingRule rule = new MarkingRule(List.of(
                new MarkingRuleElement(
                        RuleElementType.CONDITION, null, null,
                        new Condition("isPartner", ConditionOperator.EQUALS, true, null),
                        List.of(field("marking")),
                        List.of(
                                literal("VRK-PatAIR"),
                                separator("-"),
                                field("series")
                        )
                )
        ));

        // Партнёрское
        Map<String, Object> partner = new HashMap<>();
        partner.put("isPartner", true);
        partner.put("marking", "PAG.400.6-3.P3HR.30.30.41-3");

        assertThat(engine.evaluate(rule, partner, List.of()))
                .isEqualTo("PAG.400.6-3.P3HR.30.30.41-3");

        // Фирменное
        Map<String, Object> own = new HashMap<>();
        own.put("isPartner", false);
        own.put("series", "P");

        assertThat(engine.evaluate(rule, own, List.of()))
                .isEqualTo("VRK-PatAIR-P");
    }

    // ==========================================================
    // ПРИМЕР 6: Полная интеграция — CONDITION + COMPUTED + MAP
    // ==========================================================

    @Test
    @DisplayName("Полная сборка с CONDITION, COMPUTED и MAP")
    void fullIntegration() {
        // COMPUTED: voltageCode = MAP(voltage, {380: 'D', 220: 'E'})
        FieldDefinition voltageCode = new FieldDefinition(
                "voltageCode", "Код напряжения", FieldType.COMPUTED,
                false, null, null, null, null, null, null,
                null, null, null, null, null, 10,
                "MAP(FIELD('voltage'), {380: 'D', 220: 'E'})"
        );

        // COMPUTED: wheelSizeMarking = FLOOR(DIVIDE(wheelSize, 10))
        FieldDefinition wheelSizeMarking = new FieldDefinition(
                "wheelSizeMarking", "Маркировка", FieldType.COMPUTED,
                false, null, null, null, null, null, null,
                null, null, null, null, null, 11,
                "FLOOR(DIVIDE(FIELD('wheelSize'), 10))"
        );

        MarkingRule rule = new MarkingRule(List.of(
                literal("VRK-PatAIR-"),
                field("series"),
                separator("-"),
                field("ductSize"),
                separator("/"),
                field("wheelSizeMarking"),
                separator("."),
                field("poles"),
                field("voltageCode"),
                // ЕСЛИ fireproof = true → добавить "-F-2/400"
                new MarkingRuleElement(
                        RuleElementType.CONDITION, null, null,
                        new Condition("fireproof", ConditionOperator.EQUALS, true, null),
                        List.of(separator("-"), literal("F-2/400")),
                        null
                )
        ));

        Map<String, Object> values = new HashMap<>();
        values.put("series", "PKV");
        values.put("ductSize", "50-30");
        values.put("wheelSize", 225);
        values.put("poles", 2);
        values.put("voltage", 380);
        values.put("fireproof", true);

        assertThat(engine.evaluate(rule, values, List.of(voltageCode, wheelSizeMarking)))
                .isEqualTo("VRK-PatAIR-PKV-50-30/22.2D-F-2/400");
    }

    // ==========================================================
    // ПРИМЕР 7: IN для климатического исполнения
    // ==========================================================

    @Test
    @DisplayName("IN: климат У1/У2/УХЛ → добавить суффикс")
    void climateTypeIn() {
        MarkingRule rule = new MarkingRule(List.of(
                literal("Fan"),
                new MarkingRuleElement(
                        RuleElementType.CONDITION, null, null,
                        new Condition("climate", ConditionOperator.IN, null,
                                List.of("У1", "У2", "УХЛ")),
                        List.of(separator("-"), field("climate")),
                        List.of(separator("-"), literal("Unknown"))
                )
        ));

        assertThat(engine.evaluate(rule, Map.of("climate", "У2"), List.of()))
                .isEqualTo("Fan-У2");
        assertThat(engine.evaluate(rule, Map.of("climate", "Т3"), List.of()))
                .isEqualTo("Fan-Unknown");
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

    private static MarkingRuleElement field(String key) {
        return new MarkingRuleElement(RuleElementType.FIELD, null, key, null, null, null);
    }
}

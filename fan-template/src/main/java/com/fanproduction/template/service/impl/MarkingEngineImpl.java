package com.fanproduction.template.service.impl;

import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.exception.FormulaException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.service.ComputedFieldsLibrary;
import com.fanproduction.template.service.MarkingContext;
import com.fanproduction.template.service.MarkingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Реализация движка вычисления маркировки.
 * <p>
 * US7.11.a: обход LITERAL, SEPARATOR, FIELD.
 * US7.11.b: обход CONDITION с then/otherwise.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkingEngineImpl implements MarkingEngine {

    private final FormulaEngine formulaEngine;
    private final ComputedFieldsLibrary computedFieldsLibrary;

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    @Override
    public String evaluate(MarkingRule rule, MarkingContext context) {
        if (rule == null || rule.sequence() == null || rule.sequence().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        evaluateSequence(rule.sequence(), context, sb);
        return sb.toString();
    }

    @Override
    public String evaluate(MarkingRule rule,
                           Map<String, Object> values,
                           List<FieldDefinition> fields) {
        MarkingContext context = new MarkingContext(
                values, fields, formulaEngine, computedFieldsLibrary);
        return evaluate(rule, context);
    }

    // ==========================================================
    // ОБХОД ПОСЛЕДОВАТЕЛЬНОСТИ
    // ==========================================================

    private void evaluateSequence(List<MarkingRuleElement> elements,
                                  MarkingContext context,
                                  StringBuilder sb) {
        for (MarkingRuleElement element : elements) {
            evaluateElement(element, context, sb);
        }
    }

    private void evaluateElement(MarkingRuleElement element,
                                 MarkingContext context,
                                 StringBuilder sb) {
        if (element == null || element.type() == null) {
            return;
        }

        switch (element.type()) {
            case LITERAL -> {
                if (element.value() != null) {
                    sb.append(element.value());
                }
            }

            case SEPARATOR -> {
                if (element.value() != null) {
                    sb.append(element.value());
                }
            }

            case FIELD -> {
                String value = context.getFieldValueAsString(element.fieldKey());
                if (value != null && !value.isEmpty()) {
                    sb.append(value);
                }
            }

            case CONDITION ->
                // US7.11.b: реализация CONDITION
                throw new FormulaException(
                        "Обработка CONDITION будет реализована в US7.11.b");


            default -> log.warn("Неизвестный тип элемента правила: {}", element.type());
        }
    }
}

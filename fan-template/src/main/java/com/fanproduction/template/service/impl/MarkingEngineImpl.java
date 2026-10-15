package com.fanproduction.template.service.impl;

import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.Condition;
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
 * <b>Сжатие разделителей (US8.5.b-fix):</b>
 * <ul>
 *   <li>SEPARATOR откладывается в {@code pendingSeparator} и не пишется сразу.</li>
 *   <li>Перед следующим непустым значением разделитель записывается в буфер.</li>
 *   <li>Если следующее значение пустое — разделитель сохраняется в ожидании
 *       следующего непустого значения.</li>
 *   <li>Висший разделитель в конце последовательности отбрасывается.</li>
 *   <li><b>Ведущий разделитель сохраняется</b> — это критично для CONDITION,
 *       которые возвращают ветку с ведущим разделителем (например, {@code "-10"}),
 *       чтобы родитель склеил корректно.</li>
 * </ul>
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
        return evaluateSequence(rule.sequence(), context);
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

    private String evaluateSequence(List<MarkingRuleElement> elements,
                                    MarkingContext context) {
        if (elements == null || elements.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String pendingSeparator = null;

        for (MarkingRuleElement element : elements) {
            if (element == null || element.type() == null) {
                continue;
            }

            switch (element.type()) {
                case LITERAL -> {
                    if (element.value() != null && !element.value().isEmpty()) {
                        if (pendingSeparator != null) {
                            sb.append(pendingSeparator);
                            pendingSeparator = null;
                        }
                        sb.append(element.value());
                    }
                }

                case SEPARATOR -> {
                    if (element.value() != null) {
                        pendingSeparator = element.value();
                    }
                }

                case FIELD -> {
                    String value = context.getFieldValueAsString(element.fieldKey());
                    if (value != null && !value.isEmpty()) {
                        if (pendingSeparator != null) {
                            sb.append(pendingSeparator);
                            pendingSeparator = null;
                        }
                        sb.append(value);
                    }
                }

                case CONDITION -> {
                    String branchResult = evaluateCondition(element, context);
                    if (branchResult != null && !branchResult.isEmpty()) {
                        if (pendingSeparator != null) {
                            sb.append(pendingSeparator);
                            pendingSeparator = null;
                        }
                        sb.append(branchResult);
                    }
                }

                default -> log.warn("Неизвестный тип элемента правила: {}", element.type());
            }
        }

        // Висший разделитель в конце отбрасывается (не записан в sb)
        return sb.toString();
    }

    // ==========================================================
    // CONDITION
    // ==========================================================

    private String evaluateCondition(MarkingRuleElement element,
                                     MarkingContext context) {
        Condition condition = element.condition();
        if (condition == null) {
            log.warn("CONDITION без условия — пропущен");
            return "";
        }

        boolean result = evaluateConditionExpression(condition, context);
        List<MarkingRuleElement> branch = result ? element.then() : element.otherwise();
        return evaluateSequence(branch, context);
    }

    private boolean evaluateConditionExpression(Condition condition,
                                                MarkingContext context) {
        String field = condition.field();
        ConditionOperator operator = condition.operator();

        if (operator == null) {
            log.warn("CONDITION без оператора (field='{}')", field);
            return false;
        }

        Object value = (field != null) ? context.getFieldValue(field) : null;

        return switch (operator) {
            case EMPTY -> FormulaFunctions.isEmpty(value);
            case NOT_EMPTY -> !FormulaFunctions.isEmpty(value);
            case EQUALS -> FormulaFunctions.valuesEqual(value, condition.value());
            case NOT_EQUALS -> !FormulaFunctions.valuesEqual(value, condition.value());
            case IN -> {
                List<Object> candidates = condition.values();
                if (candidates == null || candidates.isEmpty()) {
                    yield false;
                }
                boolean found = false;
                for (Object candidate : candidates) {
                    if (FormulaFunctions.valuesEqual(value, candidate)) {
                        found = true;
                        break;
                    }
                }
                yield found;
            }
        };
    }
}

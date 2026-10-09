package com.fanproduction.template.service.impl;

import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.formula.FormulaEngine;
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Реализация движка вычисления маркировки.
 * <p>
 * US7.11.a: обход LITERAL, SEPARATOR, FIELD.
 * US7.11.b: обход CONDITION с then/otherwise, поддержка всех операторов.
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
        if (elements == null || elements.isEmpty()) {
            return;
        }
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
            case LITERAL, SEPARATOR -> {
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

            case CONDITION -> evaluateCondition(element, context, sb);

            default -> log.warn("Неизвестный тип элемента правила: {}", element.type());
        }
    }

    // ==========================================================
    // CONDITION
    // ==========================================================

    private void evaluateCondition(MarkingRuleElement element,
                                   MarkingContext context,
                                   StringBuilder sb) {
        Condition condition = element.condition();
        if (condition == null) {
            log.warn("CONDITION без условия — пропущен");
            return;
        }

        boolean result = evaluateConditionExpression(condition, context);

        if (result) {
            evaluateSequence(element.then(), context, sb);
        } else {
            evaluateSequence(element.otherwise(), context, sb);
        }
    }

    /**
     * Вычислить условие и вернуть boolean.
     * <p>
     * Значение поля берётся «сырым» — как есть в values.
     * Это важно для операторов EQUALS / IN, где нужно сравнивать числа как числа.
     */
    private boolean evaluateConditionExpression(Condition condition,
                                                MarkingContext context) {
        String field = condition.field();
        ConditionOperator operator = condition.operator();

        if (operator == null) {
            log.warn("CONDITION без оператора (field='{}')", field);
            return false;
        }

        // Для EMPTY / NOT_EMPTY значение поля может быть null — берём как есть
        Object value = (field != null) ? context.getFieldValue(field) : null;

        return switch (operator) {
            case EMPTY -> isEmpty(value);
            case NOT_EMPTY -> !isEmpty(value);

            case EQUALS -> valuesEqual(value, condition.value());
            case NOT_EQUALS -> !valuesEqual(value, condition.value());

            case IN -> {
                List<Object> candidates = condition.values();
                if (candidates == null || candidates.isEmpty()) {
                    yield false;
                }
                boolean found = false;
                for (Object candidate : candidates) {
                    if (valuesEqual(value, candidate)) {
                        found = true;
                        break;
                    }
                }
                yield found;
            }
        };
    }

    // ==========================================================
    // СРАВНЕНИЕ ЗНАЧЕНИЙ
    // ==========================================================

    /**
     * Сравнить два значения с учётом числового/строкового представления.
     * <p>
     * Числа сравниваются как числа (380 и "380" — равны).
     * Остальное — по toString.
     */
    private boolean valuesEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        // Числовое сравнение
        Double na = toNumber(a);
        Double nb = toNumber(b);
        if (na != null && nb != null) {
            return Double.compare(na, nb) == 0;
        }

        // Boolean-сравнение
        if (a instanceof Boolean ba && b instanceof Boolean bb) {
            return ba.equals(bb);
        }

        // Строковое сравнение
        return a.toString().equals(b.toString());
    }

    /**
     * Проверка на «пустое» значение: null, пустая строка, пустая коллекция.
     */
    private boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isEmpty();
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false;
    }

    /**
     * Привести значение к числу. Возвращает null при неудаче.
     */
    private Double toNumber(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            String trimmed = s.trim().replace(',', '.');
            if (trimmed.isEmpty()) return null;
            try {
                return Double.parseDouble(trimmed);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

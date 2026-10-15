package com.fanproduction.gui.util;

import com.fanproduction.gui.dto.config.ConditionDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Вычислитель условий {@code visibleIf} / {@code requiredIf}.
 * <p>
 * Поддерживаемые операторы:
 * <ul>
 *   <li>{@code EQUALS} — значение поля равно заданному.</li>
 *   <li>{@code NOT_EQUALS} — не равно.</li>
 *   <li>{@code NOT_EMPTY} — значение не null и не пустая строка.</li>
 *   <li>{@code EMPTY} — значение null или пустая строка.</li>
 *   <li>{@code IN} — значение входит в список {@code values}.</li>
 * </ul>
 * <p>
 * Сравнение чисел — числовое ({@code 380} и {@code "380"} равны).
 * Boolean — по значению. Остальное — по {@code toString()}.
 */
public final class ConditionEvaluator {

    private ConditionEvaluator() {
    }

    /**
     * Вычислить условие.
     *
     * @param condition условие (может быть {@code null} — тогда возвращается {@code true})
     * @param values    текущие значения полей формы
     * @return {@code true}, если условие выполнено (или отсутствует)
     */
    public static boolean evaluate(ConditionDto condition, Map<String, Object> values) {
        if (condition == null) return true;
        if (values == null) values = Map.of();

        String field = condition.getField();
        String operator = condition.getOperator();

        if (field == null || operator == null) return true;

        Object actual = values.get(field);

        return switch (operator) {
            case "EQUALS" -> valuesEqual(actual, condition.getValue());
            case "NOT_EQUALS" -> !valuesEqual(actual, condition.getValue());
            case "NOT_EMPTY" -> !isEmpty(actual);
            case "EMPTY" -> isEmpty(actual);
            case "IN" -> isIn(actual, condition.getValues());
            default -> true;  // неизвестный оператор — не блокируем
        };
    }

    // ==========================================================
    // ПРИВАТНЫЕ
    // ==========================================================

    private static boolean valuesEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;

        Double na = toNumber(a);
        Double nb = toNumber(b);
        if (na != null && nb != null) {
            return Double.compare(na, nb) == 0;
        }

        if (a instanceof Boolean ba && b instanceof Boolean bb) {
            return ba.equals(bb);
        }

        return a.toString().equals(b.toString());
    }

    private static boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isEmpty();
        if (value instanceof Collection<?> c) return c.isEmpty();
        return false;
    }

    private static boolean isIn(Object actual, List<Object> candidates) {
        if (candidates == null || candidates.isEmpty()) return false;
        for (Object candidate : candidates) {
            if (valuesEqual(actual, candidate)) return true;
        }
        return false;
    }

    private static Double toNumber(Object value) {
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

package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.response.DynamicFormFieldDto;

import java.util.Map;

/**
 * Проверка условий видимости полей карточки (US10.7.4c).
 * <p>
 * Используется просмотром карточки: поля, скрытые {@code visibleIf} при
 * текущих значениях (например, огнестойкость и взрывозащита при исполнении
 * «C»), не должны отображаться, даже если их значения сохранились раньше.
 * <p>
 * Вынесено из {@link CardViewBuilderImpl} отдельно, чтобы покрыть тестом —
 * именно эта проверка однажды «не применилась» и осталась незамеченной.
 */
public final class CardFieldVisibility {

    private CardFieldVisibility() {
    }

    /**
     * @param field  описание поля шаблона
     * @param values значения карточки
     * @return {@code false}, если условие видимости не выполняется
     */
    public static boolean isVisible(DynamicFormFieldDto field, Map<String, Object> values) {
        ConditionDto condition = field.getVisibleIf();
        if (condition == null) {
            return true;
        }
        Object actual = values != null ? values.get(condition.getField()) : null;
        String operator = condition.getOperator() != null ? condition.getOperator() : "EQUALS";

        return switch (operator) {
            case "EMPTY" -> actual == null || String.valueOf(actual).isBlank();
            case "NOT_EMPTY" -> actual != null && !String.valueOf(actual).isBlank();
            case "NOT_EQUALS" -> !sameValue(actual, condition.getValue());
            case "IN" -> condition.getValues() != null
                    && condition.getValues().stream().anyMatch(v -> sameValue(actual, v));
            default -> sameValue(actual, condition.getValue());
        };
    }

    private static boolean sameValue(Object actual, Object expected) {
        if (actual == null || expected == null) {
            return actual == expected;
        }
        if (actual instanceof Boolean a && expected instanceof Boolean b) {
            return a.equals(b);
        }
        return normalize(actual).equals(normalize(expected));
    }

    /**
     * Приводит значение к каноническому виду: «F — огнестойкость»,
     * «F - огнестойкость» и «F» считаются одним значением.
     */
    private static String normalize(Object value) {
        String s = String.valueOf(value).trim();
        int cut = s.length();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == '-' || c == '—' || c == '–') {
                cut = i;
                break;
            }
        }
        return s.substring(0, cut).toUpperCase();
    }
}

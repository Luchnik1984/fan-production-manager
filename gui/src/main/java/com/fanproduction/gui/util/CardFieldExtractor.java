package com.fanproduction.gui.util;

import java.util.Map;

/**
 * Извлечение типизированных значений из {@code Map<String, Object>}, которую
 * возвращает сервер в поле {@code fields} карточки продукции.
 * <p>
 * Устраняет boilerplate в CardClient'ах:
 * <pre>
 * // было:
 * dto.setPoles(fields.get("poles") != null ? ((Number) fields.get("poles")).intValue() : null);
 *
 * // стало:
 * dto.setPoles(CardFieldExtractor.getInteger(fields, "poles"));
 * </pre>
 * <p>
 * Все методы null-safe: если {@code fields == null} или ключа нет — вернётся {@code null}.
 * Для {@link #getInteger}, {@link #getDouble}, {@link #getLong} — если значение не приводится
 * к числу, тоже вернётся {@code null} (без исключения).
 */
public final class CardFieldExtractor {

    private CardFieldExtractor() {
    }

    public static String getString(Map<String, Object> fields, String key) {
        if (fields == null || key == null) return null;
        Object value = fields.get(key);
        return value != null ? value.toString() : null;
    }

    public static Integer getInteger(Map<String, Object> fields, String key) {
        if (fields == null || key == null) return null;
        Object value = fields.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double getDouble(Map<String, Object> fields, String key) {
        if (fields == null || key == null) return null;
        Object value = fields.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Long getLong(Map<String, Object> fields, String key) {
        if (fields == null || key == null) return null;
        Object value = fields.get(key);
        if (value == null) return null;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Boolean getBoolean(Map<String, Object> fields, String key) {
        if (fields == null || key == null) return null;
        Object value = fields.get(key);
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        return Boolean.parseBoolean(value.toString());
    }
}

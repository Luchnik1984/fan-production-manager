package com.fanproduction.template.service;

import java.util.Map;

/**
 * Функциональный интерфейс вычисляемого поля.
 * <p>
 * Принимает карту значений полей карточки, возвращает вычисленное
 * строковое значение. Может вернуть {@code null} — это означает, что
 * поле не может быть вычислено (например, отсутствуют входные данные).
 * <p>
 * Все вычисляемые поля используют этот интерфейс, чтобы их можно было
 * регистрировать в {@link ComputedFieldsLibrary}.
 */
@FunctionalInterface
public interface ComputedField {

    /**
     * Вычислить значение поля.
     *
     * @param values карта значений полей карточки.
     *               Ключ — имя поля (например, {@code "wheelSize"}),
     *               значение — любое ({@code String}, {@code Number}, {@code Boolean}).
     * @return вычисленное строковое значение или {@code null}, если поле не может
     *         быть вычислено (например, отсутствуют входные данные).
     */
    String compute(Map<String, Object> values);
}

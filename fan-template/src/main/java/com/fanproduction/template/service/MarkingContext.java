package com.fanproduction.template.service;

import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.formula.function.FormulaFunctions;
import com.fanproduction.template.model.FieldDefinition;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Контекст вычисления маркировки.
 * <p>
 * Инкапсулирует логику разрешения значения поля:
 * <ol>
 *   <li>Если поле — COMPUTED с формулой, значение вычисляется через {@link FormulaEngine}.</li>
 *   <li>Иначе — берётся прямое значение из {@code values}.</li>
 *   <li>Иначе — берётся из системных вычисляемых полей ({@link ComputedFieldsLibrary}).</li>
 * </ol>
 * <p>
 * Результаты кешируются, чтобы при повторных обращениях не вычислять одно и то же
 * поле дважды (например, в CONDITION и в FIELD).
 * <p>
 * Экземпляр создаётся на одну операцию вычисления маркировки.
 * Не потокобезопасен — это осознанно: контекст живёт в рамках одного вызова.
 */
public class MarkingContext {

    /** Значения полей карточки. */
    @Getter
    private final Map<String, Object> values;

    /** Определения полей по ключу. */
    private final Map<String, FieldDefinition> fieldsByKey;

    private final FormulaEngine formulaEngine;
    private final ComputedFieldsLibrary computedFieldsLibrary;

    /** Кеш вычисленных значений (только строковые). */
    private final Map<String, String> cache = new HashMap<>();

    public MarkingContext(Map<String, Object> values,
                          List<FieldDefinition> fields,
                          FormulaEngine formulaEngine,
                          ComputedFieldsLibrary computedFieldsLibrary) {
        this.values = values != null ? values : Map.of();
        this.fieldsByKey = fields != null
                ? fields.stream().collect(Collectors.toMap(
                FieldDefinition::key,
                Function.identity(),
                (existing, replacement) -> existing))
                : Map.of();
        this.formulaEngine = formulaEngine;
        this.computedFieldsLibrary = computedFieldsLibrary;
    }

    /**
     * Получить значение поля как строку.
     * <p>
     * Порядок разрешения:
     * <ol>
     *   <li>COMPUTED-поле с формулой — через {@link FormulaEngine}.</li>
     *   <li>Прямое значение из {@code values} (приведённое к строке).</li>
     *   <li>Системное вычисляемое поле из {@link ComputedFieldsLibrary}.</li>
     *   <li>Иначе — {@code null}.</li>
     * </ol>
     *
     * @param key ключ поля
     * @return строковое значение или {@code null}
     */
    public String getFieldValueAsString(String key) {
        if (key == null || key.isBlank()) return null;

        // Кеш
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        String result = resolve(key);
        cache.put(key, result);
        return result;
    }

    /**
     * Получить «сырое» значение поля (без преобразований).
     * <p>
     * Используется для CONDITION, где нужны настоящие типы
     * (Number, Boolean, String).
     *
     * @param key ключ поля
     * @return значение или {@code null}
     */
    public Object getFieldValue(String key) {
        if (key == null || key.isBlank()) return null;

        Object direct = values.get(key);
        if (direct != null) return direct;

        // Если поля нет в values, но есть COMPUTED-формула — вычисляем через getFieldValueAsString
        FieldDefinition field = fieldsByKey.get(key);
        if (field != null && field.type() == FieldType.COMPUTED
                && field.formula() != null && !field.formula().isBlank()) {
            return getFieldValueAsString(key);
        }

        if (computedFieldsLibrary.isRegistered(key)) {
            return computedFieldsLibrary.evaluate(key, values);
        }

        return null;
    }

    /**
     * Есть ли определение поля для данного ключа.
     */
    public boolean hasFieldDefinition(String key) {
        return key != null && fieldsByKey.containsKey(key);
    }

    // ==========================================================
    // ВНУТРЕННЯЯ ЛОГИКА
    // ==========================================================

    private String resolve(String key) {
        // 1. COMPUTED-поле с формулой
        FieldDefinition field = fieldsByKey.get(key);
        if (field != null
                && field.type() == FieldType.COMPUTED
                && field.formula() != null
                && !field.formula().isBlank()) {
            return formulaEngine.evaluateFieldDefinition(field, values);
        }

        // 2. Прямое значение из values
        Object direct = values.get(key);
        if (direct != null) {
            return FormulaFunctions.toStringValue(direct);
        }

        // 3. Системное вычисляемое поле
        if (computedFieldsLibrary.isRegistered(key)) {
            return computedFieldsLibrary.evaluate(key, values);
        }

        // 4. Значение отсутствует
        return null;
    }
}

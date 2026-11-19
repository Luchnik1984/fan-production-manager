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
 * Порядок разрешения значения поля:
 * <ol>
 *   <li>Системные метаданные шаблона (typeDesignation / typeName / seriesName / speciesName)
 *       — если ключ есть в {@code metadataValues}.</li>
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

    /** Системные метаданные шаблона (typeDesignation, typeName, seriesName, speciesName). */
    private final Map<String, String> metadataValues;

    /** Определения полей по ключу. */
    private final Map<String, FieldDefinition> fieldsByKey;

    private final FormulaEngine formulaEngine;
    private final ComputedFieldsLibrary computedFieldsLibrary;

    /** Кеш вычисленных значений (только строковые). */
    private final Map<String, String> cache = new HashMap<>();

    // ==========================================================
    // КОНСТРУКТОРЫ
    // ==========================================================

    /**
     * Базовый конструктор (без метаданных — обратная совместимость).
     */
    public MarkingContext(Map<String, Object> values,
                          List<FieldDefinition> fields,
                          FormulaEngine formulaEngine,
                          ComputedFieldsLibrary computedFieldsLibrary) {
        this(values, null, fields, formulaEngine, computedFieldsLibrary);
    }

    /**
     * Конструктор с метаданными (US9.8).
     *
     * @param metadataValues карта вида {typeDesignation, typeName, seriesName, speciesName}.
     *                       Может быть {@code null} — тогда метаданные не используются.
     */
    public MarkingContext(Map<String, Object> values,
                          Map<String, String> metadataValues,
                          List<FieldDefinition> fields,
                          FormulaEngine formulaEngine,
                          ComputedFieldsLibrary computedFieldsLibrary) {
        this.values = values != null ? values : Map.of();
        this.metadataValues = metadataValues != null ? metadataValues : Map.of();
        this.fieldsByKey = fields != null
                ? fields.stream().collect(Collectors.toMap(
                FieldDefinition::key,
                Function.identity(),
                (existing, replacement) -> existing))
                : Map.of();
        this.formulaEngine = formulaEngine;
        this.computedFieldsLibrary = computedFieldsLibrary;
    }

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    /**
     * Получить значение поля как строку.
     * <p>
     * Порядок разрешения:
     * <ol>
     *   <li>Метаданные шаблона (typeDesignation / typeName / seriesName / speciesName).</li>
     *   <li>COMPUTED-поле с формулой.</li>
     *   <li>Прямое значение из {@code values}.</li>
     *   <li>Системное вычисляемое поле из {@link ComputedFieldsLibrary}.</li>
     *   <li>Иначе — {@code null}.</li>
     * </ol>
     */
    public String getFieldValueAsString(String key) {
        if (key == null || key.isBlank()) return null;

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
     * Используется для CONDITION, где нужны настоящие типы (Number, Boolean, String).
     */
    public Object getFieldValue(String key) {
        if (key == null || key.isBlank()) return null;

        // 1. Метаданные (возвращаем как строку — CONDITION сравнивает через FormulaFunctions.valuesEqual)
        if (metadataValues.containsKey(key)) {
            return metadataValues.get(key);
        }

        // 2. Прямое значение из values
        Object direct = values.get(key);
        if (direct != null) return direct;

        // 3. COMPUTED-поле с формулой
        FieldDefinition field = fieldsByKey.get(key);
        if (field != null && field.type() == FieldType.COMPUTED
                && field.formula() != null && !field.formula().isBlank()) {
            return getFieldValueAsString(key);
        }

        // 4. Системное вычисляемое поле
        if (computedFieldsLibrary.isRegistered(key)) {
            return computedFieldsLibrary.evaluate(key, values);
        }

        return null;
    }

    public boolean hasFieldDefinition(String key) {
        return key != null && fieldsByKey.containsKey(key);
    }

    // ==========================================================
    // ВНУТРЕННЯЯ ЛОГИКА
    // ==========================================================

    private String resolve(String key) {
        // 1. Метаданные шаблона (typeDesignation, typeName, seriesName, speciesName)
        if (metadataValues.containsKey(key)) {
            String value = metadataValues.get(key);
            return value != null && !value.isEmpty() ? value : null;
        }

        // 2. COMPUTED-поле с формулой
        FieldDefinition field = fieldsByKey.get(key);
        if (field != null
                && field.type() == FieldType.COMPUTED
                && field.formula() != null
                && !field.formula().isBlank()) {
            return formulaEngine.evaluateFieldDefinition(field, values);
        }

        // 3. Прямое значение из values
        Object direct = values.get(key);
        if (direct != null) {
            return FormulaFunctions.toStringValue(direct);
        }

        // 4. Системное вычисляемое поле
        if (computedFieldsLibrary.isRegistered(key)) {
            return computedFieldsLibrary.evaluate(key, values);
        }

        // 5. Значение отсутствует
        return null;
    }
}

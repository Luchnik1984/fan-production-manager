package com.fanproduction.template.model;

import com.fanproduction.template.enums.FieldType;

import java.util.List;
import java.util.Map;

/**
 * Определение поля в шаблоне карточки вентилятора.
 * <p>
 * Расширенная модель (см. FR4.5.2). Сериализуется в JSONB.
 * <p>
 * Поле {@code formula} используется только для полей типа
 * {@link FieldType#COMPUTED}. Формула задаётся на DSL
 * (см. FR4.5.4.2) и исполняется через {@code FormulaEngine}.
 */
public record FieldDefinition(
        String key,
        String label,
        FieldType type,
        boolean required,
        String defaultValue,
        List<String> options,
        String hint,
        String referenceType,
        String targetFieldName,
        String role,
        Boolean addToProduct,
        Map<String, String> pullFrom,
        Condition visibleIf,
        Condition requiredIf,
        OnSelectAction onSelect,
        Integer displayOrder,
        String formula,
        /**
         * Группа (секция) формы, например «Основные», «Электрика», «Исполнение».
         * <p>
         * {@code null} — поле попадает в группу по умолчанию. Необязательное
         * свойство: старые шаблоны без него читаются как есть (US10.6).
         */
        String group
) {

    /**
     * Конструктор без группы — совместимость с существующим кодом и тестами.
     */
    public FieldDefinition(String key,
                           String label,
                           FieldType type,
                           boolean required,
                           String defaultValue,
                           List<String> options,
                           String hint,
                           String referenceType,
                           String targetFieldName,
                           String role,
                           Boolean addToProduct,
                           Map<String, String> pullFrom,
                           Condition visibleIf,
                           Condition requiredIf,
                           OnSelectAction onSelect,
                           Integer displayOrder,
                           String formula) {
        this(key, label, type, required, defaultValue, options, hint, referenceType,
                targetFieldName, role, addToProduct, pullFrom, visibleIf, requiredIf,
                onSelect, displayOrder, formula, null);
    }

    /**
     * Группа по умолчанию, если поле её не задаёт.
     */
    public static final String DEFAULT_GROUP = "Основные";

    /**
     * Название группы для отображения (никогда не {@code null}).
     */
    public String groupOrDefault() {
        return group == null || group.isBlank() ? DEFAULT_GROUP : group;
    }
}

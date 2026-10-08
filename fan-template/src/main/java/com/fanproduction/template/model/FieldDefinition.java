package com.fanproduction.template.model;

import com.fanproduction.template.enums.FieldType;

import java.util.List;
import java.util.Map;

/**
 * Определение поля в шаблоне карточки вентилятора.
 * <p>
 * Расширенная модель (см. FR4.5.2). Сериализуется в JSONB.
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
        Integer displayOrder
) {
}

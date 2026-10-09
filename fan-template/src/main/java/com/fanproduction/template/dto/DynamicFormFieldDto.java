package com.fanproduction.template.dto;

import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.model.OnSelectAction;

import java.util.List;
import java.util.Map;

/**
 * DTO поля формы для GUI.
 * <p>
 * Это облегчённая версия {@link com.fanproduction.template.model.FieldDefinition} —
 * только то, что нужно клиенту для отрисовки формы.
 * <p>
 * В отличие от {@code FieldDefinition}, здесь нет {@code formula} (COMPUTED-поля
 * вычисляются на сервере) и нет {@code displayOrder} (сортировка уже выполнена).
 */
public record DynamicFormFieldDto(
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
        OnSelectAction onSelect
) {
}

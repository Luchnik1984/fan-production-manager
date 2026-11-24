package com.fanproduction.template.dto;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;

/**
 * Переносимый шаблон для экспорта/импорта (US10.6).
 * <p>
 * Содержит только содержимое шаблона — без id, привязки к серии и служебных
 * полей. При импорте создаётся новый шаблон в выбранной серии.
 *
 * @param name        наименование шаблона
 * @param description описание (необязательно)
 * @param fields      поля версии
 * @param markingRule правило маркировки
 */
public record TemplateExchangeDto(
        String name,
        String description,
        List<FieldDefinition> fields,
        MarkingRule markingRule
) {
}

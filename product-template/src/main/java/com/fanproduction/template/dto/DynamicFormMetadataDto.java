package com.fanproduction.template.dto;

import com.fanproduction.template.enums.TemplateStatus;

import java.util.List;

/**
 * DTO метаданных формы карточки вентилятора.
 * <p>
 * Возвращается клиенту (GUI) для динамической отрисовки формы.
 * Содержит:
 * <ul>
 *   <li>Идентификацию версии шаблона.</li>
 *   <li>Информацию о типе и серии (для заголовка формы).</li>
 *   <li>Список полей в порядке отображения.</li>
 * </ul>
 */
public record DynamicFormMetadataDto(
        Long templateId,
        Long templateVersionId,
        Integer version,
        TemplateStatus status,
        String templateName,

        // Информация о типе и серии (для заголовка)
        Long typeId,
        String typeName,
        String typeDesignation,

        Long speciesId,
        String speciesName,

        Long seriesId,
        String seriesName,

        List<DynamicFormFieldDto> fields
) {
}

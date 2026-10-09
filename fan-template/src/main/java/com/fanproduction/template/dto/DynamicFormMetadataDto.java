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
 *   <li>Список полей в порядке отображения.</li>
 *   <li>Правило маркировки (для предпросмотра).</li>
 * </ul>
 */
public record DynamicFormMetadataDto(
        Long templateId,
        Long templateVersionId,
        Integer version,
        TemplateStatus status,
        String templateName,
        List<DynamicFormFieldDto> fields
) {
}

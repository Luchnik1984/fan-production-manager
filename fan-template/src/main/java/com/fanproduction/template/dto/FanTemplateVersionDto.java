package com.fanproduction.template.dto;

import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO версии шаблона.
 */
public record FanTemplateVersionDto(
        Long id,
        Long templateId,
        Integer version,
        TemplateStatus status,
        List<FieldDefinition> fieldsJson,
        MarkingRule markingRuleJson,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime publishedAt,
        String publishedBy
) {
}

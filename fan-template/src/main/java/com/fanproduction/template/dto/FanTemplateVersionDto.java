package com.fanproduction.template.dto;

import com.fanproduction.template.enums.TemplateStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO версии шаблона.
 * <p>
 * Поля {@code fieldsJson} и {@code markingRuleJson} — пока как {@code Map}.
 * В подшаге 7.8 будут заменены на типизированные структуры.
 */
public record FanTemplateVersionDto(
        Long id,
        Long templateId,
        Integer version,
        TemplateStatus status,
        Map<String, Object> fieldsJson,
        Map<String, Object> markingRuleJson,
        LocalDateTime createdAt,
        String createdBy,
        LocalDateTime publishedAt,
        String publishedBy
) {
}

package com.fanproduction.template.dto;

import com.fanproduction.template.model.FieldDefinition;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO пресета полей шаблона.
 */
public record FieldPresetDto(
        Long id,
        String name,
        String description,
        List<FieldDefinition> fields,
        Boolean isSystem,
        LocalDateTime createdAt,
        String createdBy
) {
}

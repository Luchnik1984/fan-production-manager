package com.fanproduction.template.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO типа продукции.
 * <p>
 * Поле {@code species} заполняется только в {@code getTree()} и
 * {@code getSectionTree()}. В {@code getAllTypes()} оно равно {@code null}.
 */
public record ProductTypeDto(
        Long id,
        String name,
        String designation,
        String displayName,
        Boolean isSystem,
        Long sectionId,
        String codePrefix,
        String systemCardType,
        LocalDateTime createdAt,
        String createdBy,
        List<ProductSpeciesDto> species
) {
}

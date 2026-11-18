package com.fanproduction.template.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO типа вентилятора.
 * <p>
 * Поле {@code species} заполняется только в {@code getTree()}.
 * В {@code getAllTypes()} оно равно {@code null}.
 */
public record ProductTypeDto(
        Long id,
        String name,
        String designation,
        String displayName,
        Boolean isSystem,
        LocalDateTime createdAt,
        String createdBy,
        List<ProductSpeciesDto> species
) {
}

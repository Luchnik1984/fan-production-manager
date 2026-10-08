package com.fanproduction.template.dto;


import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO вида вентилятора.
 * <p>
 * Если {@code name} = null, {@code displayName} = "No_species".
 * <p>
 * Поле {@code series} заполняется только в {@code getTree()}.
 */
public record FanSpeciesDto(
        Long id,
        Long typeId,
        String name,
        String displayName,
        Boolean isSystem,
        LocalDateTime createdAt,
        String createdBy,
        List<FanSeriesDto> series
) {
}

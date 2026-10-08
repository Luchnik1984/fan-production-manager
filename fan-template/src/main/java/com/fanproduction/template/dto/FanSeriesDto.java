package com.fanproduction.template.dto;

import java.time.LocalDateTime;

/**
 * DTO серии вентилятора.
 * <p>
 * Если {@code name} = null, {@code displayName} = "No_series".
 */
public record FanSeriesDto(
        Long id,
        Long speciesId,
        String name,
        String displayName,
        Boolean isSystem,
        LocalDateTime createdAt,
        String createdBy
) {
}

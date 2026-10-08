package com.fanproduction.template.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO логического шаблона.
 * <p>
 * Поле {@code versions} может быть {@code null} в операциях, где список версий не нужен
 * (например, в списке всех шаблонов).
 */
public record FanTemplateDto(
        Long id,
        Long seriesId,
        String name,
        String description,
        Long currentVersionId,
        LocalDateTime createdAt,
        String createdBy,
        List<FanTemplateVersionDto> versions
) {
}

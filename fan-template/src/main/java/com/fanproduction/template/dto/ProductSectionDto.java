package com.fanproduction.template.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO раздела продукции.
 * <p>
 * Используется в двух режимах:
 * <ul>
 *   <li>плоский список ({@code getAllSections()}) — {@code types} и
 *       {@code sections} пустые;</li>
 *   <li>дерево ({@code getSectionTree()}) — {@code sections} содержит
 *       подразделы, {@code types} — типы продукции этого раздела
 *       (с видами и сериями).</li>
 * </ul>
 */
public record ProductSectionDto(
        Long id,
        Long parentId,
        String name,
        Integer sortOrder,
        Boolean isSystem,
        LocalDateTime createdAt,
        String createdBy,
        List<ProductTypeDto> types,
        List<ProductSectionDto> sections
) {
}

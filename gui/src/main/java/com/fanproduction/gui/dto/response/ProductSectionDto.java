package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO раздела продукции.
 * <p>
 * Используется в двух режимах: плоский список (без вложенности) и дерево —
 * тогда {@code types} содержит типы раздела, а {@code sections} — подразделы.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSectionDto {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sortOrder;
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<ProductTypeDto> types;
    private List<ProductSectionDto> sections;
}

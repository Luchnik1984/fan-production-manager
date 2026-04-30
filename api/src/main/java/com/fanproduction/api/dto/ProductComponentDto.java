package com.fanproduction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для связи продукции с компонентом.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductComponentDto {
    private Long id;
    private Long productCardId;
    private Long componentId;
    private String componentName;   // для отображения
    private String componentClass;   // для отображения
    private String unitCode;         // для отображения
    private Double quantity;
    private String note;
}

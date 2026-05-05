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
    private String componentName;
    private String componentClass;
    private String vendorCode;
    private String unitCode;
    private Double quantity;
    private String description;
    private String position;
    private String note;
}

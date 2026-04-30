package com.fanproduction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для компонента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDto {
    private Long id;
    private Long classId;
    private String className;      // для отображения (denormalized)
    private String name;
    private String vendorCode;
    private Long unitId;
    private String unitCode;       // для отображения (денормализованное)
    private String unitName;       // для отображения
    private Double quantityPerUnit;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}

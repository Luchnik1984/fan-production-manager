package com.fanproduction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для единицы измерения.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureDto {
    private Long id;
    private String code;
    private String name;
    private String symbol;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private String createdBy;
}

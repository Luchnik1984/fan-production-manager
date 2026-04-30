package com.fanproduction.gui.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
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
package com.fanproduction.gui.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDto {
    private Long id;
    private Long classId;
    private String className;
    private String name;
    private String vendorCode;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private Double quantityPerUnit;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}

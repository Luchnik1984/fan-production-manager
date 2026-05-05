package com.fanproduction.gui.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

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
    private String description;
    private Map<String, Object> technicalSpecs;
    private Double weightKg;
    private String material;
    private LocalDateTime createdAt;
    private String createdBy;
}

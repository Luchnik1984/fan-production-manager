package com.fanproduction.gui.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {
    private Long id;
    private Long classId;
    private String className;
    private String name;
    private String standard;
    private String specification;
    private String materialType;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private Double density;
    private String vendorCode;
    private Double minOrder;
    private String description;
    private Map<String, Object> technicalSpecs;
    private LocalDateTime createdAt;
    private String createdBy;
}

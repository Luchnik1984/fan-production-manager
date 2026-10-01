package com.fanproduction.gui.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateComponentRequest {
    private Long classId;
    private String name;
    private String designation;
    private String vendorCode;
    private Long unitId;
    private Double weightKg;
    private String material;
    private String description;
    private Map<String, Object> technicalSpecs;
}
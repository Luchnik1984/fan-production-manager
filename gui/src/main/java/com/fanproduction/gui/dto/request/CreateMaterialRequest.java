package com.fanproduction.gui.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialRequest {
    private Long classId;
    private String name;
    private String designation;
    private String standard;
    private String specification;
    private String materialType;
    private Long unitId;
    private Double density;
    private String vendorCode;
    private Double minOrder;
    private String description;
    private Map<String, Object> technicalSpecs;

}

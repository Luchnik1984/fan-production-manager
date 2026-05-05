package com.fanproduction.gui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductComponentItemDto {
    private Long productComponentId;
    private Long componentId;
    private String name;
    private String className;
    private String vendorCode;
    private String unitCode;
    private Double quantity;
    private String description;
    private String position;
    private String note;
}

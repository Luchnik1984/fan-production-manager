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
    private String designation;
    private String className;
    private String vendorCode;
    private String unitCode;
    private Double quantity;
    private String description;
    private String position;
    private String note;

    public String getDisplayName() {
        String display = name != null ? name : "";
        if (designation != null && !designation.isEmpty() && !designation.equals(name)) {
            display += " (" + designation + ")";
        }
        return display;
    }
}

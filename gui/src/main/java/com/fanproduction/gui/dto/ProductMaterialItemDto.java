package com.fanproduction.gui.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductMaterialItemDto {
    private Long productMaterialId;
    private Long materialId;
    private String name;
    private String designation;
    private String className;
    private String standard;
    private String specification;
    private String materialType;
    private String vendorCode;
    private String unitCode;
    private Double quantityPerUnit;
    private String note;

    public String getDisplayName() {
        String display = name != null ? name : "";
        if (designation != null && !designation.isEmpty() && !designation.equals(name)) {
            display += " (" + designation + ")";
        }
        return display;
    }
}
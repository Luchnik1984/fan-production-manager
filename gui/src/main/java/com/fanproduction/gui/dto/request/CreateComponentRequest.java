package com.fanproduction.gui.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateComponentRequest {
    private Long classId;
    private String name;
    private String vendorCode;
    private Long unitId;
    private Double quantityPerUnit;
    private String description;
}
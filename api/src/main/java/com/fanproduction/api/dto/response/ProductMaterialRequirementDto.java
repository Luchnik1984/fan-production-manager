package com.fanproduction.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductMaterialRequirementDto {
    private Long id;
    private Long productCardId;
    private Long materialId;
    private String materialName;
    private String materialClass;
    private String unitCode;
    private Double quantityPerUnit;
    private String note;
}

package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO типа FAN (с вложенными видами и сериями из /tree).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductTypeDto {
    private Long id;
    private String name;
    private String designation;
    private String displayName;
    private Boolean isSystem;
    /** Раздел продукции, к которому относится тип (null — не распределён). */
    private Long sectionId;
    /** Префикс номера изделия (ED, MW, RW, AW, VRK). */
    private String codePrefix;
    /** Хардкод-тип: MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL; null — динамический. */
    private String systemCardType;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<ProductSpeciesDto> species;
}

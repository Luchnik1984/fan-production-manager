package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO метаданных формы карточки FAN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFormMetadataDto {
    private Long templateId;
    private Long templateVersionId;
    private Integer version;
    private String status;
    private String templateName;

    private Long typeId;
    private String typeName;
    private String typeDesignation;

    private Long speciesId;
    private String speciesName;

    private Long seriesId;
    private String seriesName;

    private List<DynamicFormFieldDto> fields;
}

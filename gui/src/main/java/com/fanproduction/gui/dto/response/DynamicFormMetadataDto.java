package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO метаданных формы карточки FAN.
 * <p>
 * Соответствует {@code com.fanproduction.template.dto.DynamicFormMetadataDto}.
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
    private List<DynamicFormFieldDto> fields;
}

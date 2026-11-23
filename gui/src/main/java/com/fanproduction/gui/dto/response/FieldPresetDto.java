package com.fanproduction.gui.dto.response;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO пресета полей шаблона (US10.6).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldPresetDto {
    private Long id;
    private String name;
    private String description;
    private List<FieldDefinitionDto> fields;
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private String createdBy;
}

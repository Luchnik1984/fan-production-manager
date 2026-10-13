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
public class FanTypeDto {
    private Long id;
    private String name;
    private String designation;
    private String displayName;
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<FanSpeciesDto> species;
}

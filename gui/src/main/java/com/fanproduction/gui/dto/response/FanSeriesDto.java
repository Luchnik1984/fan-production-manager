package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO серии FAN.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanSeriesDto {
    private Long id;
    private Long speciesId;
    private String name;
    private String displayName;
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private String createdBy;
}

package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO вида FAN (с вложенными сериями, если получено из /tree).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanSpeciesDto {
    private Long id;
    private Long typeId;
    private String name;
    private String displayName;
    private Boolean isSystem;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<FanSeriesDto> series;
}

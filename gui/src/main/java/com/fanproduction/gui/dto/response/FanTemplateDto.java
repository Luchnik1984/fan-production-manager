package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO логического шаблона FAN (клиентская копия).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanTemplateDto {
    private Long id;
    private Long seriesId;
    private String name;
    private String description;
    private Long currentVersionId;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<FanTemplateVersionDto> versions;
}

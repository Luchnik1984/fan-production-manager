package com.fanproduction.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для класса компонента.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentClassDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}

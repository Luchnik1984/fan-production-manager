package com.fanproduction.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentClassDto {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private LocalDateTime createdAt;
    private String createdBy;
}

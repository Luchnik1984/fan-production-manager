package com.fanproduction.gui.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
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

package com.fanproduction.gui.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComponentCategoryDto {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String path;
    private Integer sortOrder;
    private String description;
    private List<ComponentCategoryDto> children;
    private LocalDateTime createdAt;
    private String createdBy;
}

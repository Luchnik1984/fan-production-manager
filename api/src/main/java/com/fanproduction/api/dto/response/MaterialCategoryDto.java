package com.fanproduction.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialCategoryDto {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String path;
    private Integer sortOrder;
    private List<MaterialCategoryDto> children;
    private LocalDateTime createdAt;
    private String createdBy;
}

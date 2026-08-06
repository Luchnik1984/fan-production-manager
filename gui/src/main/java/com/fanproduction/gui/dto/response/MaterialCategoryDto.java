package com.fanproduction.gui.dto.response;

import com.fanproduction.core.dto.Displayable;
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
public class MaterialCategoryDto implements Displayable {
    private Long id;
    private Long parentId;
    private String name;
    private Integer level;
    private String path;
    private Integer sortOrder;
    private String description;
    private List<MaterialCategoryDto> children;
    private LocalDateTime createdAt;
    private String createdBy;

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}

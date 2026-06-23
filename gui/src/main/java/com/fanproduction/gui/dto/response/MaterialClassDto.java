package com.fanproduction.gui.dto.response;

import com.fanproduction.core.dto.Displayable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialClassDto implements Displayable {
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

    @Override
    public String getDisplayName() {
        return name;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}

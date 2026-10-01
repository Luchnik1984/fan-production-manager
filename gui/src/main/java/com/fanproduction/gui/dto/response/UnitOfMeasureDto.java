package com.fanproduction.gui.dto.response;

import com.fanproduction.gui.component.HasCategory;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitOfMeasureDto implements HasCategory {
    private Long id;
    private String code;
    private String name;
    private String symbol;
    private String category;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private String createdBy;

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getDisplayName() {
        return code + " - " + name;
    }
}
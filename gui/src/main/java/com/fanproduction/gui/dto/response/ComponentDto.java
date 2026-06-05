package com.fanproduction.gui.dto.response;

import com.fanproduction.core.dto.Displayable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComponentDto implements Displayable {
    private Long id;
    private Long classId;
    private String className;
    private String name;
    private String designation;
    private String vendorCode;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private String description;
    private Map<String, Object> technicalSpecs;
    private Double weightKg;
    private String material;
    private LocalDateTime createdAt;
    private String createdBy;

    @Override
    public String getDisplayName() {
        String display = name;
        if (designation != null && !designation.isEmpty()) {
            display += " " + designation;
        }
        return display;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}

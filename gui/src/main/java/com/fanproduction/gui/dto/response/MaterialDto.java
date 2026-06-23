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
public class MaterialDto implements Displayable {
    private Long id;
    private Long classId;
    private String className;
    private String name;
    private String designation;
    private String standard;
    private String specification;
    private String materialType;
    private Long unitId;
    private String unitCode;
    private String unitName;
    private Double density;
    private String vendorCode;
    private Double minOrder;
    private String description;
    private Map<String, Object> technicalSpecs;
    private LocalDateTime createdAt;
    private String createdBy;

    @Override
    public String getDisplayName() {
        String display = name != null ? name : "";

        String fullDesignation = buildFullDesignation(designation, specification);

        if (!fullDesignation.isEmpty()) {
            display += " (" + fullDesignation + ")";
        }

        return display;
    }

    private String buildFullDesignation(String designation, String specification) {
        // designation всегда not null в БД, но может быть пустой строкой
        if (designation == null || designation.trim().isEmpty()) {
            return specification != null ? specification.trim() : "";
        }

        if (specification == null || specification.trim().isEmpty()) {
            return designation.trim();
        }

        String d = designation.trim();
        String s = specification.trim();

        // Если одно содержит другое — возвращаем более полное
        if (s.contains(d)) return s;
        if (d.contains(s)) return d;

        // Иначе — объединяем через дефис
        return d + "-" + s;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}

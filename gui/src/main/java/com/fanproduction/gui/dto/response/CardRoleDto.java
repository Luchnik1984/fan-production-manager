package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Роль продукции (US10.7.2): назначение изделия при комплектации.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardRoleDto {
    private Long id;
    private String code;
    private String name;
    private Boolean isSystem;
    private Integer sortOrder;

    /** В выпадающем списке показываем название, а не код. */
    @Override
    public String toString() {
        return name != null && !name.isBlank() ? name : code;
    }
}

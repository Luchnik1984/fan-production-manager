package com.fanproduction.gui.dto.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO side-effect, выполняемого при выборе значения в SELECTABLE-поле.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnSelectActionDto {
    private String action;
    private String role;
    private Double quantity;
}

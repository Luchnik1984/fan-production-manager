package com.fanproduction.gui.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO side-effect, выполняемого при выборе значения в SELECTABLE-поле.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OnSelectActionDto {
    private String action;
    private String role;
    private Double quantity;
}

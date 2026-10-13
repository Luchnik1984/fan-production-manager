package com.fanproduction.gui.dto.response;

import com.fanproduction.gui.dto.config.ConditionDto;
import com.fanproduction.gui.dto.config.OnSelectActionDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO поля формы FAN, полученного с сервера.
 * <p>
 * Соответствует {@code com.fanproduction.template.dto.DynamicFormFieldDto}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DynamicFormFieldDto {
    private String key;
    private String label;
    private String type;
    private boolean required;
    private String defaultValue;
    private List<String> options;
    private String hint;
    private String referenceType;
    private String targetFieldName;
    private String role;
    private Boolean addToProduct;
    private Map<String, String> pullFrom;
    private ConditionDto visibleIf;
    private ConditionDto requiredIf;
    private OnSelectActionDto onSelect;
}

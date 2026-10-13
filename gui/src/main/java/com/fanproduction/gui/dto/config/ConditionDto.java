package com.fanproduction.gui.dto.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO условия (visibleIf / requiredIf), полученного с сервера.
 * <p>
 * Соответствует {@code com.fanproduction.template.model.Condition},
 * но на клиентской стороне. Создан отдельно, чтобы GUI не зависел
 * от модуля {@code fan-template}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConditionDto {
    private String field;
    private String operator;
    private Object value;
    private List<Object> values;
}

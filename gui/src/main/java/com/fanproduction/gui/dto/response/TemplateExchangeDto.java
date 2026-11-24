package com.fanproduction.gui.dto.response;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Переносимый шаблон для экспорта/импорта (US10.6).
 * <p>
 * Соответствует серверному {@code com.fanproduction.template.dto.TemplateExchangeDto}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TemplateExchangeDto {
    private String name;
    private String description;
    private List<FieldDefinitionDto> fields;
    private MarkingRuleDto markingRule;
}

package com.fanproduction.gui.dto.response;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO версии шаблона FAN (клиентская копия).
 * <p>
 * Содержит поля {@code fieldsJson}, {@code markingRuleJson} —
 * они нужны для редакторов полей и правила маркировки.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FanTemplateVersionDto {
    private Long id;
    private Long templateId;
    private Integer version;
    private String status;
    private List<FieldDefinitionDto> fieldsJson;
    private MarkingRuleDto markingRuleJson;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime publishedAt;
    private String publishedBy;
}
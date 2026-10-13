package com.fanproduction.gui.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO версии шаблона FAN (клиентская копия).
 * <p>
 * Поля {@code fieldsJson}, {@code markingRuleJson} игнорируются —
 * они не нужны для выбора версии. При необходимости грузятся отдельно
 * через {@code DynamicFormMetadataDto}.
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
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime publishedAt;
    private String publishedBy;
}
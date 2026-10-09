package com.fanproduction.api.dto.request.fan;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;

/**
 * Запрос на создание новой DRAFT-версии шаблона.
 * <p>
 * Если fields/markingRule не переданы — используются пустые.
 */
public record CreateFanTemplateVersionRequest(
        List<FieldDefinition> fields,

        MarkingRule markingRule
) {
}

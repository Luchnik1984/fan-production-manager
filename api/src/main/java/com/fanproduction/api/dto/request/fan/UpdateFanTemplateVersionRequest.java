package com.fanproduction.api.dto.request.fan;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;

/**
 * Запрос на обновление DRAFT-версии шаблона.
 * <p>
 * Если поле не передано (null) — оно не изменяется.
 */
public record UpdateFanTemplateVersionRequest(
        List<FieldDefinition> fields,

        MarkingRule markingRule
) {
}

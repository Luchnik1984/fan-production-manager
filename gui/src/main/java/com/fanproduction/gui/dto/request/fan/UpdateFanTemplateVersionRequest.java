package com.fanproduction.gui.dto.request.fan;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleDto;

import java.util.List;

/**
 * Запрос на обновление DRAFT-версии шаблона.
 * <p>
 * Если {@code fields} или {@code markingRule} = null — соответствующая
 * часть не меняется.
 */
public record UpdateFanTemplateVersionRequest(
        List<FieldDefinitionDto> fields,
        MarkingRuleDto markingRule
) {
}

package com.fanproduction.gui.dto.request.fan;

import com.fanproduction.gui.dto.fan.FieldDefinitionDto;
import com.fanproduction.gui.dto.fan.MarkingRuleDto;

import java.util.List;

/**
 * Запрос на создание новой DRAFT-версии шаблона.
 * <p>
 * Если {@code fields} или {@code markingRule} = null — сервер скопирует
 * из предыдущей версии (см. FanTemplateServiceAutocloneTest).
 */
public record CreateFanTemplateVersionRequest(
        List<FieldDefinitionDto> fields,
        MarkingRuleDto markingRule
) {
}

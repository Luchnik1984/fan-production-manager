package com.fanproduction.api.dto.request.fan;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Запрос на создание шаблона.
 * <p>
 * Создаётся логический шаблон + первая DRAFT-версия.
 */
public record CreateFanTemplateRequest(
        @NotNull(message = "ID серии обязателен")
        Long seriesId,

        @NotBlank(message = "Наименование шаблона обязательно")
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        @Size(max = 500, message = "Описание не длиннее 500 символов")
        String description,

        List<FieldDefinition> fields,

        MarkingRule markingRule
) {
}

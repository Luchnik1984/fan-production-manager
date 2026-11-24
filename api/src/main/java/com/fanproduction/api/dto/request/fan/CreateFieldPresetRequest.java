package com.fanproduction.api.dto.request.fan;

import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Запрос на создание пресета полей.
 *
 * @param name        название (уникальное)
 * @param description описание (необязательно)
 * @param fields      набор полей в формате версии шаблона
 */
public record CreateFieldPresetRequest(
        @NotBlank(message = "Название пресета обязательно")
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String name,

        @Size(max = 500, message = "Описание не длиннее 500 символов")
        String description,

        @NotEmpty(message = "Пресет должен содержать хотя бы одно поле")
        List<FieldDefinition> fields,

        /** Правило маркировки (US10.7.3), необязательно. */
        MarkingRule markingRule
) {
}

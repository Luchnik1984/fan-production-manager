package com.fanproduction.api.dto.request.fan;

import com.fanproduction.template.model.FieldDefinition;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Запрос на изменение пресета полей. Незаполненные поля не меняются.
 */
public record UpdateFieldPresetRequest(
        @Size(max = 200, message = "Название не длиннее 200 символов")
        String name,

        @Size(max = 500, message = "Описание не длиннее 500 символов")
        String description,

        List<FieldDefinition> fields
) {
}

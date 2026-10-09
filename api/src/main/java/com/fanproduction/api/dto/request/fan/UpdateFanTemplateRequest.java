package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.Size;

/**
 * Запрос на обновление метаданных шаблона.
 * <p>
 * Не затрагивает версии — только name/description.
 */
public record UpdateFanTemplateRequest(
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        @Size(max = 500, message = "Описание не длиннее 500 символов")
        String description
) {
}

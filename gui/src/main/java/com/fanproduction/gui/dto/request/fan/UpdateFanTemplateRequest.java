package com.fanproduction.gui.dto.request.fan;

import jakarta.validation.constraints.Size;

public record UpdateFanTemplateRequest(
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        @Size(max = 500, message = "Описание не длиннее 500 символов")
        String description
) {
}

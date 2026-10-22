package com.fanproduction.gui.dto.request.fan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CloneFanTemplateRequest(
        @NotNull(message = "ID целевой серии обязателен")
        Long targetSeriesId,

        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String newName
) {
}

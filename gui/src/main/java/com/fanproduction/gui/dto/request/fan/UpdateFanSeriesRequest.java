package com.fanproduction.gui.dto.request.fan;

import jakarta.validation.constraints.Size;

public record UpdateFanSeriesRequest(
        @Size(max = 100, message = "Наименование не длиннее 100 символов")
        String name
) {
}

package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.Size;

/**
 * Запрос на обновление серии вентилятора.
 */
public record UpdateFanSeriesRequest(
        @Size(max = 100, message = "Наименование не длиннее 100 символов")
        String name
) {
}

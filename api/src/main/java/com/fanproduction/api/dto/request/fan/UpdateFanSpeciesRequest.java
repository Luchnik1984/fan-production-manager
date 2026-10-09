package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.Size;

/**
 * Запрос на обновление вида вентилятора.
 */
public record UpdateFanSpeciesRequest(
        @Size(max = 100, message = "Наименование не длиннее 100 символов")
        String name
) {
}

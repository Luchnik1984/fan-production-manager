package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание серии вентилятора.
 */
public record CreateFanSeriesRequest(
        @NotNull(message = "ID вида обязателен")
        Long speciesId,

        @Size(max = 100, message = "Наименование не длиннее 100 символов")
        String name
) {
}

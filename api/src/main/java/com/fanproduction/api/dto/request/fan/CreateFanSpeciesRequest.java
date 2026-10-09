package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание вида вентилятора.
 */
public record CreateFanSpeciesRequest(
        @NotNull(message = "ID типа обязателен")
        Long typeId,

        @Size(max = 100, message = "Наименование не длиннее 100 символов")
        String name
) {
}

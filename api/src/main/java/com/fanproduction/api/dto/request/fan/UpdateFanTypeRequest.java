package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.Size;

/**
 * Запрос на обновление типа вентилятора.
 */
public record UpdateFanTypeRequest(
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        @Size(max = 50, message = "Обозначение не длиннее 50 символов")
        String designation
) {
}

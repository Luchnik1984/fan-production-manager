package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание типа вентилятора.
 */
public record CreateFanTypeRequest(
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        @NotBlank(message = "Обозначение типа обязательно")
        @Size(max = 50, message = "Обозначение не длиннее 50 символов")
        String designation
) {
}

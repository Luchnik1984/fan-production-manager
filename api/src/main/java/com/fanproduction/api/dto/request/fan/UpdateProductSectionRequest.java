package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на обновление раздела продукции.
 *
 * @param name      новое наименование
 * @param sortOrder новый порядок отображения ({@code null} — не менять)
 */
public record UpdateProductSectionRequest(
        @NotBlank(message = "Наименование раздела обязательно")
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        Integer sortOrder
) {
}

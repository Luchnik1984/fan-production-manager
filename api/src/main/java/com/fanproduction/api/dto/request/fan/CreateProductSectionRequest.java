package com.fanproduction.api.dto.request.fan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Запрос на создание раздела продукции.
 *
 * @param name      наименование раздела (обязательное, уникальное среди соседей)
 * @param parentId  родительский раздел ({@code null} — корневой)
 * @param sortOrder порядок отображения ({@code null} — в конец)
 */
public record CreateProductSectionRequest(
        @NotBlank(message = "Наименование раздела обязательно")
        @Size(max = 200, message = "Наименование не длиннее 200 символов")
        String name,

        Long parentId,

        Integer sortOrder
) {
}

package com.fanproduction.api.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Материал в составе атомарного запроса сохранения карточки.
 */
public record ProductMaterialItemRequest(
        @NotNull(message = "materialId обязателен")
        Long materialId,

        Double quantityPerUnit,

        String note
) {
}

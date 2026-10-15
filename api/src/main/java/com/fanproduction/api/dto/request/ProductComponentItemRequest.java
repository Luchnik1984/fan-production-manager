package com.fanproduction.api.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Компонент в составе атомарного запроса сохранения карточки.
 */
public record ProductComponentItemRequest(
        @NotNull(message = "componentId обязателен")
        Long componentId,

        Double quantity,

        String position,

        String note
) {
}

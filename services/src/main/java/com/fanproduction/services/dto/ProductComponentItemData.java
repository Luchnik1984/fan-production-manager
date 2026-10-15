package com.fanproduction.services.dto;

/**
 * Данные компонента для атомарного сохранения.
 * <p>
 * Внутренний DTO сервисного слоя — не зависит от API или GUI.
 */
public record ProductComponentItemData(
        Long componentId,
        Double quantity,
        String position,
        String note
) {
}

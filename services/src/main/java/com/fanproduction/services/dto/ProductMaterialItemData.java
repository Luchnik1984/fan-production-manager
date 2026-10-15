package com.fanproduction.services.dto;

/**
 * Данные материала для атомарного сохранения.
 */
public record ProductMaterialItemData(
        Long materialId,
        Double quantityPerUnit,
        String note
) {
}

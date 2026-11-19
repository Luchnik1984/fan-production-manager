package com.fanproduction.gui.dto.request.fan;

/**
 * Запрос на создание раздела продукции.
 */
public record CreateProductSectionRequest(
        String name,
        Long parentId,
        Integer sortOrder
) {
}

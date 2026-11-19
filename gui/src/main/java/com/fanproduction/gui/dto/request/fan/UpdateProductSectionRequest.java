package com.fanproduction.gui.dto.request.fan;

/**
 * Запрос на переименование раздела и/или изменение порядка.
 */
public record UpdateProductSectionRequest(
        String name,
        Integer sortOrder
) {
}

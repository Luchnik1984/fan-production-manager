package com.fanproduction.api.dto.request.fan;

/**
 * Запрос на перемещение раздела продукции.
 *
 * @param parentId новый родительский раздел ({@code null} — в корень)
 */
public record MoveProductSectionRequest(
        Long parentId
) {
}

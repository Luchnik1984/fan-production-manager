package com.fanproduction.gui.dto.request.fan;

/**
 * Запрос на перемещение раздела в другого родителя.
 */
public record MoveProductSectionRequest(
        Long parentId
) {
}

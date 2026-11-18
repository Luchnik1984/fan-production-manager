package com.fanproduction.services.dto;

import com.fanproduction.core.entity.product.DynamicCardEntity;

/**
 * Результат создания или обновления FAN-карточки.
 * <p>
 * Содержит саму сущность и вычисленную маркировку.
 */
public record DynamicCardCreationResult(
        DynamicCardEntity card,
        String computedMarking
) {
}

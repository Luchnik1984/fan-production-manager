package com.fanproduction.services.dto;

import com.fanproduction.core.entity.product.FanCardEntity;

/**
 * Результат создания или обновления FAN-карточки.
 * <p>
 * Содержит саму сущность и вычисленную маркировку.
 */
public record FanCardCreationResult(
        FanCardEntity card,
        String computedMarking
) {
}

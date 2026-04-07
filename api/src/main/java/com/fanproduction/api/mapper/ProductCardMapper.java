package com.fanproduction.api.mapper;

import com.fanproduction.api.dto.ProductCardResponse;
import com.fanproduction.core.entity.*;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Маппер для преобразования сущностей карточек в DTO.
 * Использует рефлексию для извлечения всех полей сущности.
 */
@Component
public class ProductCardMapper {

    /**
     * Преобразует сущность карточки в DTO с динамическими полями
     */
    public ProductCardResponse toResponse(BaseProductCard card) {
        if (card == null) {
            return null;
        }

        return ProductCardResponse.builder()
                .id(card.getId())
                .name(card.getName())
                .code(card.getCode())
                .cardType(card.getCardType())
                .createdAt(card.getCreatedAt())
                .updatedAt(card.getUpdatedAt())
                .createdBy(card.getCreatedBy())
                .fields(extractFields(card))
                .build();
    }

    /**
     * Извлекает все специфичные поля сущности (кроме базовых)
     */
    private Map<String, Object> extractFields(BaseProductCard card) {
        Map<String, Object> fields = new HashMap<>();
        Class<?> clazz = card.getClass();

        // Проходим по всем полям класса (включая родительские, но исключая BaseProductCard)
        while (clazz != null && clazz != BaseProductCard.class && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(card);
                    if (value != null) {
                        fields.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    // Игнорируем
                }
            }
            clazz = clazz.getSuperclass();
        }

        return fields;
    }
}

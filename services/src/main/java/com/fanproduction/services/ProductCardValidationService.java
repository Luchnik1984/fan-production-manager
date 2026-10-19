package com.fanproduction.services;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.enums.CardTemplateType;

import java.util.Map;

/**
 * Сервис валидации обязательных полей карточек продукции.
 * <p>
 * Проверяет, что перед сохранением карточки заданы все обязательные
 * для её типа поля. Защищает от создания «нулевых» карточек (как
 * было с MOTOR без {@code mountingType}).
 * <p>
 * <b>FAN не валидируется здесь</b> — обязательность полей определяется
 * шаблоном ({@code FieldDefinition.required}) и проверяется в
 * {@code FanCardService}.
 */
public interface ProductCardValidationService {

    /**
     * Проверить обязательные поля для создания карточки.
     *
     * @param cardType тип карточки
     * @param fields   поля для создания
     * @throws IllegalArgumentException с описанием первого невалидного поля
     */
    void validateForCreate(CardTemplateType cardType, Map<String, Object> fields);

    /**
     * Проверить обязательные поля для обновления карточки.
     * <p>
     * Валидация применяется к итоговому состоянию: значения из
     * {@code newFields} перекрывают значения из {@code existingCard}.
     *
     * @param cardType     тип карточки
     * @param existingCard существующая карточка
     * @param newFields    обновляемые поля
     */
    void validateForUpdate(CardTemplateType cardType,
                           BaseProductCard existingCard,
                           Map<String, Object> newFields);
}

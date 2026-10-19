package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.services.ProductCardValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Реализация сервиса валидации обязательных полей карточек.
 * <p>
 * Обязательные поля заданы в статическом конфиге {@link #REQUIRED_FIELDS}.
 * При добавлении нового типа или изменении требований — правится только
 * этот конфиг.
 */
@Slf4j
@Service
public class ProductCardValidationServiceImpl implements ProductCardValidationService {

    /**
     * Обязательные поля для каждого типа карточки.
     * <p>
     * FAN — не валидируется здесь (валидация в FanCardService).
     * RADIAL_WHEEL, AXIAL_WHEEL, CUP, ACCESSORY — на первом этапе
     * не валидируются (усложнено вариантами использования).
     */
    private static final Map<CardTemplateType, List<String>> REQUIRED_FIELDS = Map.of(
            CardTemplateType.MOTOR, List.of(
                    "series",
                    "motorType",
                    "poles",
                    "powerKw",
                    "mountingType"
            ),
            CardTemplateType.MOTOR_WHEEL, List.of(
                    "manufacturerMarking",
                    "poles",
                    "voltageCode",
                    "powerKw",
                    "size"
            )
    );

    // ==========================================================
    // CREATE
    // ==========================================================

    @Override
    public void validateForCreate(CardTemplateType cardType, Map<String, Object> fields) {
        List<String> required = REQUIRED_FIELDS.get(cardType);
        if (required == null || required.isEmpty()) {
            return;  // нет требований — пропускаем
        }

        for (String fieldName : required) {
            Object value = fields != null ? fields.get(fieldName) : null;
            if (isEmpty(value)) {
                throw new IllegalArgumentException(
                        "Обязательное поле не заполнено: '" + fieldName
                                + "' для карточки типа " + cardType);
            }
        }
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @Override
    public void validateForUpdate(CardTemplateType cardType,
                                  BaseProductCard existingCard,
                                  Map<String, Object> newFields) {
        List<String> required = REQUIRED_FIELDS.get(cardType);
        if (required == null || required.isEmpty()) {
            return;
        }

        if (existingCard == null) {
            throw new IllegalArgumentException(
                    "existingCard не может быть null для валидации обновления");
        }

        // Собираем итоговое состояние: существующие + новые
        Map<String, Object> merged = mergeExistingAndNew(existingCard, newFields);

        for (String fieldName : required) {
            Object value = merged.get(fieldName);
            if (isEmpty(value)) {
                throw new IllegalArgumentException(
                        "Обязательное поле не заполнено: '" + fieldName
                                + "' для карточки типа " + cardType);
            }
        }
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    /**
     * Собрать итоговое состояние полей: значения из {@code newFields}
     * перекрывают значения из {@code existingCard}.
     * <p>
     * Читаем поля {@code existingCard} через reflection (по именам Java-полей).
     * Если поле не найдено — игнорируем.
     */
    private Map<String, Object> mergeExistingAndNew(BaseProductCard existingCard,
                                                    Map<String, Object> newFields) {
        Map<String, Object> merged = new HashMap<>();

        // 1. Существующие значения
        Class<?> clazz = existingCard.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    merged.put(field.getName(), field.get(existingCard));
                } catch (IllegalAccessException ignored) {
                    // пропускаем
                }
            }
            clazz = clazz.getSuperclass();
        }

        // 2. Новые значения перекрывают
        if (newFields != null) {
            merged.putAll(newFields);
        }

        return merged;
    }

    /**
     * Проверить, что значение «пустое»: {@code null}, пустая строка,
     * пустая коллекция.
     */
    private boolean isEmpty(Object value) {
        if (value == null) return true;
        if (value instanceof String s) return s.isBlank();
        if (value instanceof java.util.Collection<?> c) return c.isEmpty();
        return false;
    }
}

package com.fanproduction.services.factory;

import com.fanproduction.core.entity.*;
import com.fanproduction.core.enums.CardTemplateType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

/**
 * Фабрика для создания карточек продукции.
 * Использует EnumMap для регистрации поставщиков карточек.
 * Добавление нового типа карточки не требует изменения кода фабрики.
 */
@Component
public class ProductCardFactory {

    private final Map<CardTemplateType, CardSupplier> cardSuppliers = new EnumMap<>(CardTemplateType.class);

    public ProductCardFactory() {
        registerSuppliers();
    }

    /**
     * Регистрация всех поставщиков карточек
     */
    private void registerSuppliers() {
        cardSuppliers.put(CardTemplateType.MOTOR, MotorCardEntity::new);
        cardSuppliers.put(CardTemplateType.MOTOR_WHEEL, MotorWheelCardEntity::new);
        cardSuppliers.put(CardTemplateType.RADIAL_WHEEL, RadialWheelCardEntity::new);
        cardSuppliers.put(CardTemplateType.AXIAL_FAN, AxialFanCardEntity::new);
        cardSuppliers.put(CardTemplateType.RADIAL_FAN, RadialFanCardEntity::new);
        cardSuppliers.put(CardTemplateType.DUCT_FAN, DuctFanCardEntity::new);
        cardSuppliers.put(CardTemplateType.CUP, CupCardEntity::new);
        cardSuppliers.put(CardTemplateType.ACCESSORY, AccessoryCardEntity::new);
    }

    /**
     * Создаёт карточку продукции по типу и данным.
     * O(1) - константное время выполнения.
     *
     * @param cardType тип карточки (из CardTemplateType)
     * @param fields   Map с полями и их значениями
     * @return созданная сущность (наследник BaseProductCard)
     */
    public BaseProductCard createCard(CardTemplateType cardType, Map<String, Object> fields) {
        CardSupplier supplier = cardSuppliers.get(cardType);

        if (supplier == null) {
            throw new IllegalArgumentException("Unknown card type: " + cardType);
        }

        BaseProductCard card = supplier.get();
        card.setCardType(cardType.name());

        // Заполняем поля из переданной Map
        setFields(card, fields);

        return card;
    }

    /**
     * Заполняет поля сущности из Map.
     * Использует рефлексию для установки значений.
     */
    private void setFields(BaseProductCard card, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            return;
        }

        Class<?> clazz = card.getClass();

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (value == null) {
                continue;
            }

            try {
                Field field = findField(clazz, fieldName);
                if (field != null) {
                    field.setAccessible(true);
                    Object convertedValue = convertValue(value, field.getType());
                    field.set(card, convertedValue);
                }
            } catch (Exception e) {
                System.err.println("Failed to set field: " + fieldName + " - " + e.getMessage());
            }
        }
    }

    /**
     * Рекурсивно ищет поле в классе и его родителях
     */
    private Field findField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return findField(superclass, fieldName);
            }
            return null;
        }
    }

    /**
     * Преобразует значение к нужному типу
     */
    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }

        if (targetType == Long.class || targetType == long.class) {
            if (value instanceof String) {
                return Long.parseLong((String) value);
            }
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
        }

        if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof String) {
                return Integer.parseInt((String) value);
            }
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }

        if (targetType == Double.class || targetType == double.class) {
            if (value instanceof String) {
                return Double.parseDouble((String) value);
            }
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
        }

        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof String) {
                return Boolean.parseBoolean((String) value);
            }
        }

        return value;
    }
}
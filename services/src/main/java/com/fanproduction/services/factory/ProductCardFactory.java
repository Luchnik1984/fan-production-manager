package com.fanproduction.services.factory;

import com.fanproduction.core.entity.product.*;
import com.fanproduction.core.enums.CardTemplateType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Фабрика для создания карточек продукции.
 * <p>
 * Использует {@link EnumMap} для регистрации поставщиков карточек.
 * <p>
 * Для системных типов ({@code MOTOR}, {@code MOTOR_WHEEL}, {@code RADIAL_WHEEL},
 * {@code AXIAL_WHEEL}, {@code CUP}, {@code ACCESSORY}) создаются конкретные сущности.
 * <p>
 * Для {@code FAN} используется единая сущность {@link FanCardEntity} —
 * все вентиляторы создаются через конструктор шаблонов (модуль {@code fan-template})
 * и сервис {@code FanCardService}. В {@link #createCard} ветка FAN не доходит
 * до этой фабрики — обрабатывается раньше в {@code ProductCardServiceImpl}.
 * Регистрация {@code FanCardEntity::new} оставлена для совместимости.
 * <p>
 * Метод {@link #applyFields} публичен: используется для обновления существующей
 * карточки (не создаёт новую сущность с тем же id).
 */
@Component
public class ProductCardFactory {

    private final Map<CardTemplateType, CardSupplier> cardSuppliers = new EnumMap<>(CardTemplateType.class);

    public ProductCardFactory() {
        registerSuppliers();
    }

    /**
     * Регистрация всех поставщиков карточек.
     */
    private void registerSuppliers() {
        cardSuppliers.put(CardTemplateType.MOTOR, MotorCardEntity::new);
        cardSuppliers.put(CardTemplateType.MOTOR_WHEEL, MotorWheelCardEntity::new);
        cardSuppliers.put(CardTemplateType.RADIAL_WHEEL, RadialWheelCardEntity::new);
        cardSuppliers.put(CardTemplateType.AXIAL_WHEEL, AxialWheelCardEntity::new);
        cardSuppliers.put(CardTemplateType.FAN, FanCardEntity::new);
        cardSuppliers.put(CardTemplateType.CUP, CupCardEntity::new);
        cardSuppliers.put(CardTemplateType.ACCESSORY, AccessoryCardEntity::new);
    }

    /**
     * Создаёт карточку продукции по типу и данным.
     * O(1) — константное время выполнения.
     *
     * @param cardType тип карточки (из {@link CardTemplateType})
     * @param fields   Map с полями и их значениями
     * @return созданная сущность (наследник {@link BaseProductCard})
     */
    public BaseProductCard createCard(CardTemplateType cardType, Map<String, Object> fields) {
        CardSupplier supplier = cardSuppliers.get(cardType);

        if (supplier == null) {
            throw new IllegalArgumentException("Unknown card type: " + cardType);
        }

        BaseProductCard card = supplier.get();
        card.setCardType(cardType.name());

        applyFields(card, fields);

        return card;
    }

    /**
     * Применить поля к существующей карточке.
     * <p>
     * В отличие от {@link #createCard}, не создаёт новую сущность — работает
     * с уже загруженной. Используется при обновлении карточек, чтобы не потерять
     * системные поля ({@code createdAt}, {@code createdBy}, {@code code}).
     * <p>
     * Поля, которых нет в {@code fields}, <b>не</b> сбрасываются — семантика
     * «частичное обновление». Для FAN-карточек используется другой путь
     * ({@code FanCardService.updateFromValues}), где семантика иная
     * (форма отправляется целиком).
     *
     * @param card   существующая карточка
     * @param fields поля для применения
     */
    public void applyFields(BaseProductCard card, Map<String, Object> fields) {
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
     * Рекурсивно ищет поле в классе и его родителях.
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

    private final Map<Class<?>, Function<Object, Object>> converters = new HashMap<>();

    {
        converters.put(Long.class, v -> v instanceof String ? Long.parseLong((String) v) : ((Number) v).longValue());
        converters.put(long.class, v -> v instanceof String ? Long.parseLong((String) v) : ((Number) v).longValue());
        converters.put(Integer.class, v -> v instanceof String ? Integer.parseInt((String) v) : ((Number) v).intValue());
        converters.put(int.class, v -> v instanceof String ? Integer.parseInt((String) v) : ((Number) v).intValue());
        converters.put(Double.class, v -> v instanceof String ? Double.parseDouble((String) v) : ((Number) v).doubleValue());
        converters.put(double.class, v -> v instanceof String ? Double.parseDouble((String) v) : ((Number) v).doubleValue());
        converters.put(Boolean.class, v -> v instanceof String ? Boolean.parseBoolean((String) v) : v);
        converters.put(boolean.class, v -> v instanceof String ? Boolean.parseBoolean((String) v) : v);
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) return value;
        Function<Object, Object> converter = converters.get(targetType);
        if (converter != null) {
            try {
                return converter.apply(value);
            } catch (Exception e) {
                return value;
            }
        }
        return value;
    }
}
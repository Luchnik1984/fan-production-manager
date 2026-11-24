package com.fanproduction.core.enums;

import lombok.Getter;

/**
 * Классы карточек продукции.
 * <p>
 * Два хардкод-типа (MOTOR и MOTOR_WHEEL) имеют жёстко заданный набор полей
 * и собственную сущность (JOINED-наследование).
 * <p>
 * {@link #DYNAMIC} — все карточки, создаваемые по шаблону: вентиляторы,
 * клапаны, стаканы, козырьки, а с US10.7 — и колёса (осевые, радиальные).
 * Поля и правило маркировки определяются шаблоном, а конкретный вид
 * продукции — ссылкой {@code product_type_id} в {@code dynamic_card}.
 */
@Getter
public enum CardTemplateType {
    MOTOR("Электродвигатель", "motor"),
    MOTOR_WHEEL("Мотор-колесо", "motorWheel"),
    DYNAMIC("Изделие по шаблону", "dynamic");

    private final String displayName;
    private final String entityName;

    CardTemplateType(String displayName, String entityName) {
        this.displayName = displayName;
        this.entityName = entityName;
    }

    public static CardTemplateType fromDisplayName(String displayName) {
        for (CardTemplateType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }

    public static CardTemplateType fromEntityName(String entityName) {
        for (CardTemplateType type : values()) {
            if (type.entityName.equals(entityName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown entity name: " + entityName);
    }
}

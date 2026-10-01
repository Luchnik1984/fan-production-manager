package com.fanproduction.core.enums;

import lombok.Getter;

/**
 * Типы карточек продукции.
 * <p>
 * Для системных карточек (MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, CUP, ACCESSORY)
 * используется жёстко заданный набор полей и логика в конфигураторах.
 * <p>
 * Для FAN — все вентиляторы (осевые, радиальные, канальные, крышные, струйные и др.)
 * создаются через конструктор шаблонов. Поля и правило маркировки определяются
 * шаблоном, привязанным к конкретной серии.
 */
@Getter
public enum CardTemplateType {
    MOTOR("Электродвигатель", "motor"),
    MOTOR_WHEEL("Мотор-колесо", "motorWheel"),
    RADIAL_WHEEL("Колесо радиальное", "radialWheel"),
    AXIAL_WHEEL("Колесо осевое", "axialWheel"),
    FAN("Вентилятор", "fan"),
    CUP("Стакан", "cup"),
    ACCESSORY("Комплектующее", "accessory");

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

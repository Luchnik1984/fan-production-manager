package com.fanproduction.core.enums;

import lombok.Getter;

/**
 * Типы карточек продукции.
 */
@Getter
public enum CardTemplateType {
    MOTOR("Электродвигатель", "motor"),
    MOTOR_WHEEL("Мотор-колесо", "motorWheel"),
    RADIAL_WHEEL("Радиальное колесо", "radialWheel"),
    AXIAL_FAN("Осевой вентилятор", "axialFan"),
    RADIAL_FAN("Радиальный вентилятор", "radialFan"),
    DUCT_FAN("Канальный вентилятор", "ductFan"),
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

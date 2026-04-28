package com.fanproduction.core.enums;

import lombok.Getter;

/**
 * Типы карточек продукции.
 */
@Getter
public enum CardTemplateType {
    MOTOR("Электродвигатель", "motor"),
    MOTOR_WHEEL("Мотор-колесо", "motorWheel"),
    RADIAL_WHEEL("Колесо радиальное", "radialWheel"),
    AXIAL_WHEEL("Колесо осевое", "axialWheel"),
    AXIAL_FAN("Вентилятор осевой", "axialFan"),
    RADIAL_FAN("Вентилятор радиальный", "radialFan"),
    DUCT_FAN("Вентилятор канальный", "ductFan"),
    ROOF_LOW_PROFILE_FAN("Вентилятор крышный низкопрофильный", "roofLowProfileFan"),
    ROOF_RADIAL_FAN("Вентилятор крышный радиальный", "roofRadialFan"),
    ROOF_AXIAL_FAN("Вентилятор крышный осевой", "roofAxialFan"),
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

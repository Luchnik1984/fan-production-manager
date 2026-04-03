package com.fanproduction.core.enums;

/**
 * Типы карточек продукции.
 */
public enum CardTemplateType {
    MOTOR("Электродвигатель"),
    AXIAL_FAN("Осевой вентилятор"),
    RADIAL_FAN("Радиальный вентилятор"),
    DUCT_FAN("Канальный вентилятор"),
    CUP("Стакан"),
    ACCESSORY("Комплектующее");

    private final String displayName;

    CardTemplateType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static CardTemplateType fromDisplayName(String displayName) {
        for (CardTemplateType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown display name: " + displayName);
    }
}

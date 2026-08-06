package com.fanproduction.core.enums;

import lombok.Getter;

import java.util.List;

/**
 * Типы полей в таблице fan_card, которые ссылаются на сборочные узлы
 * (крупные детали: мотор-колесо, радиальное колесо, осевое колесо, электродвигатель)
 */
@Getter
public enum AssemblyUnitFieldType {

    MOTOR("MOTOR", "motor_id"),
    MOTOR_WHEEL("MOTOR_WHEEL", "motor_wheel_id"),
    RADIAL_WHEEL("RADIAL_WHEEL", "radial_wheel_id"),
    AXIAL_WHEEL("AXIAL_WHEEL", "axial_wheel_id");

    private final String unitType;
    private final String fieldName;

    AssemblyUnitFieldType(String unitType, String fieldName) {
        this.unitType = unitType;
        this.fieldName = fieldName;
    }

    public static AssemblyUnitFieldType fromUnitType(String unitType) {
        for (AssemblyUnitFieldType value : values()) {
            if (value.unitType.equals(unitType)) {
                return value;
            }
        }
        return null;
    }

    /**
     * Получить все типы вентиляторов, которые могут использовать данный сборочный узел
     */
    public static List<String> getFanCardTypesForUnit(String unitType) {
        return switch (unitType) {
            case "MOTOR" -> List.of("DUCT_FAN", "RADIAL_FAN", "AXIAL_FAN",
                    "ROOF_LOW_PROFILE_FAN", "ROOF_RADIAL_FAN", "ROOF_AXIAL_FAN");
            case "MOTOR_WHEEL" -> List.of("DUCT_FAN", "ROOF_LOW_PROFILE_FAN");
            case "RADIAL_WHEEL" -> List.of("DUCT_FAN", "RADIAL_FAN", "ROOF_RADIAL_FAN");
            case "AXIAL_WHEEL" -> List.of("AXIAL_FAN", "ROOF_AXIAL_FAN");
            default -> List.of();
        };
    }
}

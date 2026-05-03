package com.fanproduction.core.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Единый источник отображаемых названий типов карточек.
 */
public class CardTypeDisplay {

    private static final Map<String, String> DISPLAY_MAP = new HashMap<>();
    private static final Map<String, String> CODE_MAP = new HashMap<>();

    static {
        // Прямое соответствие: код -> отображаемое название
        DISPLAY_MAP.put("MOTOR", "Электродвигатель");
        DISPLAY_MAP.put("MOTOR_WHEEL", "Мотор-колесо");
        DISPLAY_MAP.put("RADIAL_WHEEL", "Колесо радиальное");
        DISPLAY_MAP.put("AXIAL_WHEEL", "Колесо осевое");
        DISPLAY_MAP.put("AXIAL_FAN", "Вентилятор осевой");
        DISPLAY_MAP.put("RADIAL_FAN", "Вентилятор радиальный");
        DISPLAY_MAP.put("DUCT_FAN", "Вентилятор канальный");
        DISPLAY_MAP.put("CUP", "Стакан");
        DISPLAY_MAP.put("ACCESSORY", "Комплектующее");

        // Обратное соответствие: отображаемое название -> код
        for (Map.Entry<String, String> entry : DISPLAY_MAP.entrySet()) {
            CODE_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Возвращает отображаемое название для типа карточки
     * @param cardType код типа (MOTOR, AXIAL_WHEEL и т.д.)
     * @return отображаемое название на русском
     */
    public static String getDisplayName(String cardType) {
        return DISPLAY_MAP.getOrDefault(cardType, cardType);
    }

    /**
     * Возвращает карту всех соответствий (код -> название)
     */
    public static Map<String, String> getDisplayMap() {
        Map<String, String> filtered = new HashMap<>();
        for (Map.Entry<String, String> entry : DISPLAY_MAP.entrySet()) {
            if (!"ACCESSORY".equals(entry.getKey())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    /**
     * Возвращает код типа по отображаемому названию
     * @param displayName отображаемое название (например, "Электродвигатель")
     * @return код типа (например, "MOTOR") или null, если не найдено
     */
    public static String getCodeByDisplayName(String displayName) {
        return CODE_MAP.get(displayName);
    }
}

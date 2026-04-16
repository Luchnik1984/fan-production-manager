package com.fanproduction.gui.configurator;

import javafx.scene.control.Control;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика для получения конфигуратора специальных полей карточки.
 */
public class CardFormConfigurator {

    private static final Map<String, CardFieldConfigurator> configurators = new HashMap<>();

    static {
        configurators.put("MOTOR", new MotorCardConfigurator());
        configurators.put("MOTOR_WHEEL", new MotorWheelCardConfigurator());
        configurators.put("RADIAL_WHEEL", new RadialWheelCardConfigurator());
        configurators.put("AXIAL_WHEEL", new AxialWheelCardConfigurator());
        // TODO: добавить другие типы карточек
    }

    /**
     * Настраивает специальные поля для карточки
     * @param cardType тип карточки
     * @param fieldControls карта контролов
     * @param existingCardExists есть ли уже существующая карточка
     */
    public static void configure(String cardType, Map<String, Control> fieldControls, boolean existingCardExists) {
        CardFieldConfigurator configurator = configurators.get(cardType);
        if (configurator != null) {
            configurator.setupFields(fieldControls, existingCardExists);
        }
    }
}

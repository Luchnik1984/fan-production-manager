package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.Map;

/**
 * Фабрика конфигураторов специальных полей карточки.
 * <p>
 * Для системных карточек (MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL)
 * используются жёстко заданные конфигураторы.
 * <p>
 * Для FAN (все вентиляторы) — {@link FanCardConfigurator}, который
 * загружает поля и правило маркировки из шаблона (через REST).
 */
public class CardFormConfigurator {

    private static final Map<String, CardFieldConfigurator> configurators = new HashMap<>();

    static {
        configurators.put("MOTOR", new MotorCardConfigurator());
        configurators.put("MOTOR_WHEEL", new MotorWheelCardConfigurator());
        configurators.put("RADIAL_WHEEL", new RadialWheelCardConfigurator());
        configurators.put("AXIAL_WHEEL", new AxialWheelCardConfigurator());
        configurators.put("FAN", new FanCardConfigurator());
    }

    /**
     * Настраивает специальные поля для карточки.
     */
    public static void configure(String cardType,
                                 Map<String, Node> fieldControls,
                                 Map<String, Label> fieldLabels,
                                 Map<String, Label> fieldHints,
                                 boolean existingCardExists) {
        CardFieldConfigurator configurator = configurators.get(cardType);
        if (configurator != null) {
            configurator.setupFields(fieldControls, fieldLabels, fieldHints, existingCardExists);
        }
    }

    /**
     * Возвращает конфигуратор для указанного типа карточки.
     */
    public static CardFieldConfigurator getConfigurator(String cardType) {
        return configurators.get(cardType);
    }
}

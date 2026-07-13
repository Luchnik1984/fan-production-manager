package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

/**
 * Конфигуратор для логики огнестойкости.
 * Используется в карточках, где есть галочка "Огнестойкость".
 */
public class FireproofConfigurator {

    private final CardFieldConfigurator parent;

    public FireproofConfigurator(CardFieldConfigurator parent) {
        this.parent = parent;
    }

    /**
     * Настраивает слушатели для полей огнестойкости
     */
    public void setup(Map<String, Node> fieldControls,
                      Map<String, Label> fieldLabels,
                      Map<String, Label> fieldHints,
                      Runnable onUpdate) {
        parent.addTextFieldListener(fieldControls, "fireproofTime", () -> updateMarking(fieldControls, onUpdate));
        parent.addTextFieldListener(fieldControls, "maxTemperature", () -> updateMarking(fieldControls, onUpdate));
        parent.addCheckBoxListener(fieldControls, "fireproof", () -> updateMarking(fieldControls, onUpdate));
        parent.addTextFieldListener(fieldControls, "fireproofMarking", onUpdate);

        // Начальное обновление
        updateMarking(fieldControls, onUpdate);
    }

    /**
     * Обновляет маркировку огнестойкости
     */
    public void updateMarking(Map<String, Node> fieldControls, Runnable onUpdate) {
        TextField fireproofMarkingField = parent.getTextField(fieldControls, "fireproofMarking");
        if (fireproofMarkingField == null) return;

        boolean isFireproof = parent.isSelected(fieldControls, "fireproof");

        if (!isFireproof) {
            fireproofMarkingField.setText("");
            if (onUpdate != null) onUpdate.run();
            return;
        }

        String fireproofTime = parent.getFieldValue(fieldControls, "fireproofTime");
        String maxTemperature = parent.getFieldValue(fieldControls, "maxTemperature");

        StringBuilder marking = new StringBuilder("F");
        if (!fireproofTime.isEmpty()) {
            marking.append("-").append(fireproofTime);
        }
        String temp = maxTemperature.isEmpty() ? "400" : maxTemperature;
        marking.append("/").append(temp);

        fireproofMarkingField.setText(marking.toString());
        if (onUpdate != null) onUpdate.run();
    }
}

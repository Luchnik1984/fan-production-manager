package com.fanproduction.gui.configurator;

import com.fanproduction.gui.util.CheckboxUtils;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

/**
 * Хелпер для управления исполнениями (огнестойкость, взрывозащита)
 * в карточках продукции.
 * Обеспечивает:
 * - Условную видимость полей
 * - Взаимоисключение галочек
 * - Формирование маркировки исполнения
 * - Формирование маркировки огнестойкости из времени и температуры
 */
public class ExecutionMarkingHelper {

    // ========== КОНСТАНТЫ ==========

    /**
     * Маркировка "Общего применения" по умолчанию
     */
    public static final String DEFAULT_GENERAL_PURPOSE = "C";

    /**
     * Маркировка огнестойкости по умолчанию
     */
    public static final String DEFAULT_FIREPROOF = "F/400";

    /**
     * Маркировка взрывозащиты по умолчанию
     */
    public static final String DEFAULT_EXPLOSION = "1Ex d IIC T4 Gb";

    /**
     * Предельная температура по умолчанию (°C)
     */
    public static final String DEFAULT_TEMPERATURE = "400";

    /**
     * Поля, связанные с огнестойкостью
     */
    private static final String[] FIREPROOF_FIELDS = {
            "fireproofMarking", "fireproofTime", "maxTemperature"
    };

    /**
     * Поля, связанные с взрывозащитой
     */
    private static final String[] EXPLOSION_FIELDS = {
            "explosionMarking"
    };

    // ========== ПОЛЯ ХЕЛПЕРА ==========

    private final Map<String, Node> fieldControls;
    private final Map<String, Label> fieldLabels;
    private final Map<String, Label> fieldHints;
    private final Runnable onUpdate;

    // ========== КОНСТРУКТОРЫ ==========

    public ExecutionMarkingHelper(Map<String, Node> fieldControls,
                                  Map<String, Label> fieldLabels,
                                  Map<String, Label> fieldHints,
                                  Runnable onUpdate) {
        this.fieldControls = fieldControls;
        this.fieldLabels = fieldLabels;
        this.fieldHints = fieldHints;
        this.onUpdate = onUpdate;
    }

    public ExecutionMarkingHelper(Map<String, Node> fieldControls, Runnable onUpdate) {
        this(fieldControls, null, null, onUpdate);
    }

    public ExecutionMarkingHelper(Map<String, Node> fieldControls) {
        this(fieldControls, null, null, null);
    }

    // ========== ОСНОВНЫЕ МЕТОДЫ ==========

    /**
     * Настраивает условную видимость и взаимоисключение
     */
    public void setup() {
        setupConditionalVisibility();
        setupExclusiveSelection();
    }

    /**
     * Возвращает маркировку исполнения на основе состояния галочек
     */
    public String getExecutionMarking() {
        if (isSelected("generalPurpose")) {
            return DEFAULT_GENERAL_PURPOSE;
        }
        if (isSelected("fireproof")) {
            String marking = buildFireproofMarking();
            return !marking.isEmpty() ? marking : DEFAULT_FIREPROOF;
        }
        if (isSelected("explosionProof")) {
            String marking = getFieldValue("explosionMarking");
            return !marking.isEmpty() ? marking : DEFAULT_EXPLOSION;
        }
        return "";
    }

    /**
     * Формирует маркировку огнестойкости на основе полей
     */
    public String buildFireproofMarking() {
        boolean isFireproof = isSelected("fireproof");
        if (!isFireproof) {
            return "";
        }

        String fireproofTime = getFieldValue("fireproofTime");
        String maxTemperature = getFieldValue("maxTemperature");

        StringBuilder marking = new StringBuilder("F");

        if (!fireproofTime.isEmpty()) {
            marking.append("-").append(fireproofTime);
        }

        String temp = !maxTemperature.isEmpty()
                ? maxTemperature
                : DEFAULT_TEMPERATURE;
        marking.append("/").append(temp);

        return marking.toString();
    }

    /**
     * Проверяет, выбрано ли какое-либо исполнение
     */
    public boolean hasExecution() {
        return isSelected("generalPurpose") ||
                isSelected("fireproof") ||
                isSelected("explosionProof");
    }

    /**
     * Получает текущее выбранное исполнение в виде enum
     */
    public ExecutionType getCurrentExecution() {
        if (isSelected("generalPurpose")) {
            return ExecutionType.GENERAL_PURPOSE;
        }
        if (isSelected("fireproof")) {
            return ExecutionType.FIREPROOF;
        }
        if (isSelected("explosionProof")) {
            return ExecutionType.EXPLOSION_PROOF;
        }
        return ExecutionType.NONE;
    }

    /**
     * Сбрасывает все исполнения (снимает все галочки)
     */
    public void clearAll() {
        CheckboxUtils.clearAll(
                getCheckBox("generalPurpose"),
                getCheckBox("fireproof"),
                getCheckBox("explosionProof")
        );
    }

    // ========== ПРИВАТНЫЕ МЕТОДЫ ==========

    private void setupConditionalVisibility() {
        // Огнестойкость
        CheckBox fireproofCheck = getCheckBox("fireproof");
        if (fireproofCheck != null) {
            boolean isFireproof = fireproofCheck.isSelected();
            for (String fieldName : FIREPROOF_FIELDS) {
                setVisible(fieldName, isFireproof);
            }

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                for (String fieldName : FIREPROOF_FIELDS) {
                    setVisible(fieldName, val);
                }
                if (onUpdate != null) {
                    onUpdate.run();
                }
            });
        }

        // Взрывозащита
        CheckBox explosionCheck = getCheckBox("explosionProof");
        if (explosionCheck != null) {
            boolean isExplosion = explosionCheck.isSelected();
            for (String fieldName : EXPLOSION_FIELDS) {
                setVisible(fieldName, isExplosion);
            }

            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                for (String fieldName : EXPLOSION_FIELDS) {
                    setVisible(fieldName, val);
                }
                if (onUpdate != null) {
                    onUpdate.run();
                }
            });
        }

        // При изменении огнестойкости обновляем поле fireproofMarking
        if (fireproofCheck != null) {
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> updateFireproofMarkingField());
        }

        // При изменении времени или температуры обновляем поле fireproofMarking
        addFireproofFieldListeners();
    }

    private void setupExclusiveSelection() {
        CheckBox generalPurposeCheck = getCheckBox("generalPurpose");
        CheckBox fireproofCheck = getCheckBox("fireproof");
        CheckBox explosionCheck = getCheckBox("explosionProof");

        // Используем CheckboxUtils для настройки взаимного исключения
        CheckboxUtils.setupMutualExclusive(generalPurposeCheck, fireproofCheck, () -> {
            if (onUpdate != null) onUpdate.run();
            updateFireproofMarkingField();
        });

        CheckboxUtils.setupMutualExclusive(generalPurposeCheck, explosionCheck, () -> {
            if (onUpdate != null) onUpdate.run();
        });

        CheckboxUtils.setupMutualExclusive(fireproofCheck, explosionCheck, () -> {
            if (onUpdate != null) onUpdate.run();
            updateFireproofMarkingField();
        });
    }

    private void updateFireproofMarkingField() {
        TextField fireproofMarkingField = getTextField("fireproofMarking");
        if (fireproofMarkingField == null) {
            return;
        }

        boolean isFireproof = isSelected("fireproof");
        if (!isFireproof) {
            fireproofMarkingField.setText("");
            return;
        }

        String marking = buildFireproofMarking();
        fireproofMarkingField.setText(marking);
    }

    private void addFireproofFieldListeners() {
        TextField fireproofTimeField = getTextField("fireproofTime");
        TextField maxTemperatureField = getTextField("maxTemperature");

        if (fireproofTimeField != null) {
            fireproofTimeField.textProperty().addListener((obs, old, val) -> {
                updateFireproofMarkingField();
                if (onUpdate != null) onUpdate.run();
            });
        }

        if (maxTemperatureField != null) {
            maxTemperatureField.textProperty().addListener((obs, old, val) -> {
                updateFireproofMarkingField();
                if (onUpdate != null) onUpdate.run();
            });
        }
    }

    // ========== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ==========

    private CheckBox getCheckBox(String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof CheckBox ? (CheckBox) control : null;
    }

    private TextField getTextField(String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof TextField ? (TextField) control : null;
    }

    private void setVisible(String fieldName, boolean visible) {
        Node control = fieldControls.get(fieldName);
        Label label = fieldLabels != null ? fieldLabels.get(fieldName) : null;
        Label hint = fieldHints != null ? fieldHints.get(fieldName) : null;

        if (control != null) {
            control.setVisible(visible);
            control.setManaged(visible);
            // Также управляем родительским контейнером (VBox/HBox)
            if (control.getParent() != null) {
                control.getParent().setVisible(visible);
                control.getParent().setManaged(visible);
            }
        }
        if (label != null) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
        if (hint != null) {
            hint.setVisible(visible);
            hint.setManaged(visible);
        }
    }

    private boolean isSelected(String fieldName) {
        CheckBox checkBox = getCheckBox(fieldName);
        return checkBox != null && checkBox.isSelected();
    }

    private String getFieldValue(String fieldName) {
        TextField textField = getTextField(fieldName);
        return textField != null ? textField.getText().trim() : "";
    }

    // ========== ENUM ==========

    /**
     * Типы исполнения
     */
    public enum ExecutionType {
        NONE,
        GENERAL_PURPOSE,
        FIREPROOF,
        EXPLOSION_PROOF
    }
}
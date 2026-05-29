package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

/**
 * Интерфейс для настройки специальных полей карточек продукции.
 * Содержит default методы для доступа к контролам и их значениям,
 * чтобы избежать дублирования кода в классах-наследниках.
 */
public interface CardFieldConfigurator {

    /**
     * Настраивает специальные поля для карточки
     *
     * @param fieldControls      карта контролов (name -> Node)
     * @param fieldLabels        карта лейблов (name -> Label)
     * @param fieldHints         карта подсказок (name -> Label)
     * @param existingCardExists true если редактируем существующую карточку
     */
    void setupFields(Map<String, Node> fieldControls,
                     Map<String, Label> fieldLabels,
                     Map<String, Label> fieldHints,
                     boolean existingCardExists);

    // ==========================================
    // БАЗОВЫЕ МЕТОДЫ ДЛЯ ПОЛУЧЕНИЯ КОНТРОЛОВ
    // ==========================================

    /**
     * Безопасное получение TextField из карты контролов
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return TextField или null, если контрол не того типа
     */
    default TextField getTextField(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof TextField ? (TextField) control : null;
    }

    /**
     * Безопасное получение ComboBox<String> из карты контролов
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return ComboBox или null, если контрол не того типа
     */
    @SuppressWarnings("unchecked")
    default ComboBox<String> getComboBox(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof ComboBox ? (ComboBox<String>) control : null;
    }

    /**
     * Безопасное получение CheckBox из карты контролов
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return CheckBox или null, если контрол не того типа
     */
    default CheckBox getCheckBox(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        return control instanceof CheckBox ? (CheckBox) control : null;
    }

    // ==========================================
    // МЕТОДЫ ДЛЯ ПОЛУЧЕНИЯ ЗНАЧЕНИЙ
    // ==========================================

    /**
     * Получает строковое значение из контрола по имени поля
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return строковое значение или пустую строку
     */
    default String getFieldValue(Map<String, Node> fieldControls, String fieldName) {
        Node control = fieldControls.get(fieldName);
        if (control == null) return "";

        if (control instanceof TextField) {
            return ((TextField) control).getText().trim();
        }
        if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

    /**
     * Проверяет, выбран ли CheckBox
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return true если CheckBox выбран и не null
     */
    default boolean isSelected(Map<String, Node> fieldControls, String fieldName) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        return checkBox != null && checkBox.isSelected();
    }

    /**
     * Получает целочисленное значение из TextField
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return Integer значение или null
     */
    default Integer getIntValue(Map<String, Node> fieldControls, String fieldName) {
        String value = getFieldValue(fieldControls, fieldName);
        if (value.isEmpty()) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Получает дробное значение из TextField
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @return Double значение или null
     */
    default Double getDoubleValue(Map<String, Node> fieldControls, String fieldName) {
        String value = getFieldValue(fieldControls, fieldName);
        if (value.isEmpty()) return null;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ==========================================
    // МЕТОДЫ ДЛЯ УСТАНОВКИ ЗНАЧЕНИЙ
    // ==========================================

    /**
     * Устанавливает текстовое значение в TextField
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @param value         новое значение
     */
    default void setFieldValue(Map<String, Node> fieldControls, String fieldName, String value) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null && value != null) {
            textField.setText(value);
        }
    }

    /**
     * Устанавливает числовое значение в TextField
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @param value         новое значение
     */
    default void setFieldValue(Map<String, Node> fieldControls, String fieldName, Number value) {
        if (value != null) {
            setFieldValue(fieldControls, fieldName, value.toString());
        }
    }

    // ==========================================
    // МЕТОДЫ ДЛЯ РАСЧЁТА НОМИНАЛЬНОЙ СКОРОСТИ
    // ==========================================

    /**
     * Рассчитывает номинальную скорость по количеству полюсов.
     * Формула: 6000 / poles
     */
    default void updateRatedSpeed(ComboBox<String> polesCombo, TextField ratedSpeedField) {
        String value = polesCombo.getValue();
        if (value != null && !value.isEmpty()) {
            try {
                int poles = Integer.parseInt(value);
                int ratedSpeed = 6000 / poles;
                ratedSpeedField.setText(String.valueOf(ratedSpeed));
            } catch (NumberFormatException e) {
                ratedSpeedField.setText("");
            }
        } else {
            ratedSpeedField.setText("");
        }
    }

    /**
     * Настраивает автоматический расчёт номинальной скорости при изменении количества полюсов.
     *
     * @param fieldControls карта контролов
     * @param callback      действие после пересчёта (например, обновление полной маркировки)
     */
    default void setupRatedSpeedCalculation(Map<String, Node> fieldControls, Runnable callback) {
        ComboBox<String> polesCombo = getComboBox(fieldControls, "poles");
        TextField ratedSpeedField = getTextField(fieldControls, "ratedSpeedRpm");

        if (polesCombo != null && ratedSpeedField != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> {
                updateRatedSpeed(polesCombo, ratedSpeedField);
                if (callback != null) {
                    callback.run();
                }
            });
            updateRatedSpeed(polesCombo, ratedSpeedField);
        }
    }

    // ==========================================
    // МЕТОДЫ ДЛЯ ВЗАИМОИСКЛЮЧАЮЩИХ ГАЛОЧЕК
    // ==========================================

    /**
     * Настраивает взаимоисключающие галочки для трёх опций:
     * - Общего применения (generalPurpose)
     * - Огнестойкость (fireproof)
     * - Взрывозащита (explosionProof)
     *
     * @param fieldControls карта контролов
     * @param callback      действие после изменения (например, обновление полной маркировки)
     */
    default void setupExclusiveSelection(Map<String, Node> fieldControls, Runnable callback) {
        CheckBox generalPurposeCheck = getCheckBox(fieldControls, "generalPurpose");
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");

        if (generalPurposeCheck != null) {
            generalPurposeCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                if (callback != null) callback.run();
            });
        }

        if (fireproofCheck != null) {
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                if (callback != null) callback.run();
            });
        }

        if (explosionCheck != null) {
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                }
                if (callback != null) callback.run();
            });
        }
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // ==========================================

    /**
     * Автоматическое заполнение поля "Наименование" значением по умолчанию
     *
     * @param fieldControls     карта контролов
     * @param defaultName       значение по умолчанию
     * @param existingCardExists true если редактируем существующую карточку
     */
    default void autoFillName(Map<String, Node> fieldControls, String defaultName, boolean existingCardExists) {
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText(defaultName);
            }
        }
    }

    /**
     * Добавляет слушатель изменения текста на TextField
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @param callback      действие при изменении
     */
    default void addTextFieldListener(Map<String, Node> fieldControls, String fieldName, Runnable callback) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Добавляет слушатель изменения состояния на CheckBox
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @param callback      действие при изменении
     */
    default void addCheckBoxListener(Map<String, Node> fieldControls, String fieldName, Runnable callback) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        if (checkBox != null) {
            checkBox.selectedProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Добавляет слушатель изменения значения на ComboBox
     *
     * @param fieldControls карта контролов
     * @param fieldName     имя поля
     * @param callback      действие при изменении
     */
    default void addComboBoxListener(Map<String, Node> fieldControls, String fieldName, Runnable callback) {
        ComboBox<String> comboBox = getComboBox(fieldControls, fieldName);
        if (comboBox != null) {
            comboBox.valueProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Управляет видимостью контрола и его лейбла.
     *
     * @param fieldControls карта контролов
     * @param fieldLabels   карта лейблов
     * @param fieldName     имя поля
     * @param visible       видимость
     */
    default void setVisible(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            String fieldName,
                            boolean visible) {
        Node control = fieldControls.get(fieldName);
        Label label = fieldLabels.get(fieldName);

        if (control != null) {
            control.setVisible(visible);
            control.setManaged(visible);
        }
        if (label != null) {
            label.setVisible(visible);
            label.setManaged(visible);
        }
    }

    /**
     * Настраивает условную видимость полей для огнестойкости и взрывозащиты
     *
     * @param fieldControls карта контролов
     * @param fieldLabels карта лейблов
     * @param updateCallback действие при изменении (обычно обновление полной маркировки)
     */
    default void setupConditionalVisibility(Map<String, Node> fieldControls,
                                            Map<String, Label> fieldLabels,
                                            Runnable updateCallback) {
        // Огнестойкость -> поле маркировки огнестойкости и предельной температуры
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        if (fireproofCheck != null) {
            setVisible(fieldControls, fieldLabels, "fireproofMarking", fireproofCheck.isSelected());
            setVisible(fieldControls, fieldLabels, "maxTemperature", fireproofCheck.isSelected());

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                setVisible(fieldControls, fieldLabels, "fireproofMarking", val);
                setVisible(fieldControls, fieldLabels, "maxTemperature", val);
                if (updateCallback != null) {
                    updateCallback.run();
                }
            });
        }

        // Взрывозащита -> поле маркировки взрывозащиты
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");
        if (explosionCheck != null) {
            setVisible(fieldControls, fieldLabels, "explosionMarking", explosionCheck.isSelected());

            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                setVisible(fieldControls, fieldLabels, "explosionMarking", val);
                if (updateCallback != null) {
                    updateCallback.run();
                }
            });
        }
    }
}
package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.Map;

public class DuctFanCardConfigurator implements CardFieldConfigurator {

    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Node> fieldControls;
    private Map<String, Label> fieldLabels;

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    private TextField getTextField(Map<String, Node> controls, String name) {
        Node c = controls.get(name);
        return c instanceof TextField ? (TextField) c : null;
    }

    @SuppressWarnings("unchecked")
    private ComboBox<String> getComboBox(Map<String, Node> controls, String name) {
        Node c = controls.get(name);
        return c instanceof ComboBox ? (ComboBox<String>) c : null;
    }

    private CheckBox getCheckBox(Map<String, Node> controls, String name) {
        Node c = controls.get(name);
        return c instanceof CheckBox ? (CheckBox) c : null;
    }

    private boolean isSelected(String fieldName) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        return checkBox != null && checkBox.isSelected();
    }

    private String getFieldValue(String fieldName) {
        Node control = fieldControls.get(fieldName);
        if (control == null) return "";
        if (control instanceof TextField) return ((TextField) control).getText().trim();
        if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

    private void setFieldValue(String fieldName, String value) {
       Node control = fieldControls.get(fieldName);
        if (control instanceof TextField && value != null) {
            ((TextField) control).setText(value);
        }
    }

    private void addTextFieldListener(String fieldName, Runnable callback) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void addComboBoxListener(String fieldName, Runnable callback) {
        ComboBox<String> comboBox = getComboBox(fieldControls, fieldName);
        if (comboBox != null) {
            comboBox.valueProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void addCheckBoxListener(String fieldName, Runnable callback) {
        CheckBox checkBox = getCheckBox(fieldControls, fieldName);
        if (checkBox != null) {
            checkBox.selectedProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void setVisible(String fieldName, boolean visible) {
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

    // ==================== ОСНОВНОЙ МЕТОД ====================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {
        this.fieldControls = fieldControls;
        this.fieldLabels = fieldLabels;

        fullMarkingField = getTextField(fieldControls, "fullMarking");

        // Настройка выбора типа колеса
        setupTypeSelection();

        // Настройка слушателей для обновления полной маркировки
        setupFullMarkingListeners();

        setupExclusiveSelection();

        // Принудительное обновление полной маркировки
        updateFullMarking();


        // Автозаполнение наименования для новой карточки
        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Вентилятор канальный");
            }
        }
    //        debugFullMarking();
    }

    // ==================== НАСТРОЙКА ТИПА КОЛЕСА ====================

    private void setupTypeSelection() {
        ComboBox<String> typeCombo = getComboBox(fieldControls, "ductFanType");
        if (typeCombo == null) return;

        // Сохраняем значение из карточки (если редактируем)
        String savedType = getFieldValue("ductFanType");

        // Устанавливаем русские названия
        typeCombo.getItems().clear();
        typeCombo.getItems().addAll("Мотор-колесо", "Радиальное колесо");

        // Восстанавливаем сохранённое значение
        if ("MOTOR_WHEEL".equals(savedType)) {
            typeCombo.setValue("Мотор-колесо");
            updateFieldsVisibility(true, false);
        } else if ("RADIAL_WHEEL".equals(savedType)) {
            typeCombo.setValue("Радиальное колесо");
            updateFieldsVisibility(false, true);
        } else {
            typeCombo.setValue("Мотор-колесо");
            updateFieldsVisibility(true, false);
        }

        // Слушатель изменения типа
        typeCombo.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) {
                boolean isMotorWheel = "Мотор-колесо".equals(newVal);
                boolean isRadialWheel = "Радиальное колесо".equals(newVal);
                updateFieldsVisibility(isMotorWheel, isRadialWheel);
                updateFullMarking();
            }
        });
    }

    private void updateFieldsVisibility(boolean isMotorWheel, boolean isRadialWheel) {
        setVisible("motorWheelId", isMotorWheel);
        setVisible("radialWheelId", isRadialWheel);
        setVisible("motorId", isRadialWheel);
        setVisible("wheelSize", isRadialWheel);
    }

    // ==================== СЛУШАТЕЛИ ДЛЯ ПОЛНОЙ МАРКИРОВКИ ====================

    private void setupFullMarkingListeners() {
        if (fullMarkingField == null) return;

        // Основные поля
        addTextFieldListener("seriesName", this::updateFullMarking);
        addTextFieldListener("executionType", this::updateFullMarking);
        addTextFieldListener("ductSize", this::updateFullMarking);
        addTextFieldListener("poles", this::updateFullMarking);
        addTextFieldListener("voltage", this::updateFullMarking);
        addTextFieldListener("wheelSize", this::updateFullMarking);
        addComboBoxListener("ductFanType", this::updateFullMarking);

        // Исполнение по назначению
        addCheckBoxListener("generalPurpose", this::updateFullMarking);
        addCheckBoxListener("fireproof", this::updateFullMarking);
        addCheckBoxListener("explosionProof", this::updateFullMarking);
        addTextFieldListener("fireproofMarking", this::updateFullMarking);
        addTextFieldListener("explosionMarking", this::updateFullMarking);
    }

    // ==================== ФОРМИРОВАНИЕ ПОЛНОЙ МАРКИРОВКИ ====================

    private void updateFullMarking() {
        if (fullMarkingField == null) return;

        String seriesName = getFieldValue("seriesName");
        String executionType = getFieldValue("executionType");
        String ductSize = getFieldValue("ductSize");
        String poles = getFieldValue("poles");
        String voltage = getFieldValue("voltage");
        String wheelSize = getFieldValue("wheelSize");
        String ductFanType = getFieldValue("ductFanType");

        // Преобразуем русское название в английский код
        if ("Мотор-колесо".equals(ductFanType)) {
            ductFanType = "MOTOR_WHEEL";
        } else if ("Радиальное колесо".equals(ductFanType)) {
            ductFanType = "RADIAL_WHEEL";
        }

        boolean isGeneralPurpose = isSelected("generalPurpose");
        boolean isFireproof = isSelected("fireproof");
        boolean isExplosionProof = isSelected("explosionProof");

        String purposeMarking = "";
        if (isFireproof) {
            purposeMarking = getFieldValue("fireproofMarking");
        } else if (isExplosionProof) {
            purposeMarking = getFieldValue("explosionMarking");
        }

        StringBuilder fullMarking = new StringBuilder();

        if (seriesName != null && !seriesName.isEmpty()) {
            fullMarking.append(seriesName);
        }
        if (executionType != null && !executionType.isEmpty()) {
            fullMarking.append("-").append(executionType);
        }

        if ("MOTOR_WHEEL".equals(ductFanType)) {
            // Вариант с мотор-колесом: VRK-PatAIR-P-40-20-4-220
            if (ductSize != null && !ductSize.isEmpty()) {
                fullMarking.append("-").append(ductSize);
            }
            if (poles != null && !poles.isEmpty()) {
                fullMarking.append("-").append(poles);
            }
            if (voltage != null && !voltage.isEmpty()) {
                fullMarking.append("-").append(voltage);
            }
        } else if ("RADIAL_WHEEL".equals(ductFanType)) {
            // Вариант с радиальным колесом: VRK-PatAIR-PKV-50-30/25.2D
            if (ductSize != null && !ductSize.isEmpty()) {
                fullMarking.append("-").append(ductSize);
            }
            if (wheelSize != null && !wheelSize.isEmpty()) {
                try {
                    double ws = Double.parseDouble(wheelSize);
                    fullMarking.append("/").append((int) Math.round(ws * 10));
                } catch (NumberFormatException e) {
                    fullMarking.append("/").append(wheelSize);
                }
            }
            if (poles != null && !poles.isEmpty()) {
                fullMarking.append(".").append(poles);
            }
            String voltageCode = getVoltageCode(voltage);
            if (!voltageCode.isEmpty()) {
                fullMarking.append(voltageCode);
            }
        }

        // Добавляем исполнение по назначению (если не общее применение)
        if (!isGeneralPurpose && purposeMarking != null && !purposeMarking.isEmpty()) {
            fullMarking.append("-").append(purposeMarking);
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
            System.out.println("  >>> FIELD UPDATED to: [" + newMarking + "]");
        }
    }

    private String getVoltageCode(String voltage) {
        if (voltage == null) return "";
        if (voltage.equals("220")) return "E";
        if (voltage.equals("380")) return "D";
        return "";
    }

    private void debugFullMarking() {
        System.out.println("=== DuctFanCardConfigurator DEBUG ===");
        System.out.println("fullMarkingField = " + fullMarkingField);
        System.out.println("fullMarkingField value = " + (fullMarkingField != null ? fullMarkingField.getText() : "null"));
        System.out.println("fieldControls contains 'fullMarking'? " + fieldControls.containsKey("fullMarking"));
        System.out.println("======================================");
    }

    private void setupExclusiveSelection() {
        CheckBox generalPurposeCheck = getCheckBox(fieldControls, "generalPurpose");
        CheckBox fireproofCheck = getCheckBox(fieldControls, "fireproof");
        CheckBox explosionCheck = getCheckBox(fieldControls, "explosionProof");

        if (generalPurposeCheck != null) {
            generalPurposeCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                updateFullMarking();
            });
        }

        if (fireproofCheck != null) {
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (explosionCheck != null) explosionCheck.setSelected(false);
                }
                updateFullMarking();
            });
        }

        if (explosionCheck != null) {
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val) {
                    if (generalPurposeCheck != null) generalPurposeCheck.setSelected(false);
                    if (fireproofCheck != null) fireproofCheck.setSelected(false);
                }
                updateFullMarking();
            });
        }
    }
}
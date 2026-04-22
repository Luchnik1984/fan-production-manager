package com.fanproduction.gui.configurator;

import com.fanproduction.gui.service.ReferenceDataService;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.Map;

public class DuctFanCardConfigurator implements CardFieldConfigurator {

    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Control> fieldControls;
    private Map<String, Label> fieldLabels;  // ← добавлено

    private TextField getTextField(Map<String, Control> controls, String name) {
        Control c = controls.get(name);
        return c instanceof TextField ? (TextField) c : null;
    }

    private ComboBox<String> getComboBox(Map<String, Control> controls, String name) {
        return CardFieldConfigurator.getComboBox(controls, name);
    }

    @Override
    public void setupFields(Map<String, Control> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {
        this.fieldControls = fieldControls;
        this.fieldLabels = fieldLabels;  // ← добавлено

        fullMarkingField = getTextField(fieldControls, "fullMarking");

        // Загружаем данные в ComboBox
        ComboBox<String> motorWheelCombo = getComboBox(fieldControls, "motorWheelId");
        if (motorWheelCombo != null) {
            motorWheelCombo.setPromptText("Выберите мотор-колесо");
            ReferenceDataService.loadMotorWheels(motorWheelCombo);
        }

        ComboBox<String> radialWheelCombo = getComboBox(fieldControls, "radialWheelId");
        if (radialWheelCombo != null) {
            radialWheelCombo.setPromptText("Выберите радиальное колесо");
            ReferenceDataService.loadRadialWheels(radialWheelCombo);
        }

        ComboBox<String> motorCombo = getComboBox(fieldControls, "motorId");
        if (motorCombo != null) {
            motorCombo.setPromptText("Выберите электродвигатель");
            ReferenceDataService.loadMotors(motorCombo);
        }

        setupTypeSelection();
        setupFullMarkingGeneration();

        if (!existingCardExists) {
            TextField nameField = getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Вентилятор канальный");
            }
        }
    }

    private void setupTypeSelection() {
        ComboBox<String> typeCombo = getComboBox(fieldControls, "ductFanType");

        if (typeCombo != null) {
            // Сохраняем текущее значение (код) во временную переменную
            String currentValue = typeCombo.getValue();

            // Очищаем и добавляем русские названия
            typeCombo.getItems().clear();
            typeCombo.getItems().addAll("Мотор-колесо", "Радиальное колесо");

            // Восстанавливаем выбранное значение (конвертируем код в русское название)
            if ("MOTOR_WHEEL".equals(currentValue)) {
                typeCombo.setValue("Мотор-колесо");
            } else if ("RADIAL_WHEEL".equals(currentValue)) {
                typeCombo.setValue("Радиальное колесо");
            } else {
                typeCombo.setValue("Мотор-колесо");
            }

            // Добавляем слушатель
            typeCombo.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null) {
                    String realValue = "Мотор-колесо".equals(newVal) ? "MOTOR_WHEEL" : "RADIAL_WHEEL";
                    typeCombo.setUserData(realValue);

                    boolean isMotorWheel = "Мотор-колесо".equals(newVal);
                    boolean isRadialWheel = "Радиальное колесо".equals(newVal);
                    updateFieldsVisibility(isMotorWheel, isRadialWheel);
                    updateFullMarking();
                }
            });

            // Устанавливаем начальную видимость
            String initialValue = typeCombo.getValue();
            boolean isMotorWheel = "Мотор-колесо".equals(initialValue);
            boolean isRadialWheel = "Радиальное колесо".equals(initialValue);
            updateFieldsVisibility(isMotorWheel, isRadialWheel);

            // Устанавливаем реальное значение в userData
            if (typeCombo.getValue() != null) {
                String realValue = "Мотор-колесо".equals(typeCombo.getValue()) ? "MOTOR_WHEEL" : "RADIAL_WHEEL";
                typeCombo.setUserData(realValue);
            }
        }
    }

    private void updateFieldsVisibility(boolean isMotorWheel, boolean isRadialWheel) {
        Control motorWheelField = fieldControls.get("motorWheelId");
        Control radialWheelField = fieldControls.get("radialWheelId");
        Control motorField = fieldControls.get("motorId");
        Control wheelSizeField = fieldControls.get("wheelSize");

        Label motorWheelLabel = fieldLabels != null ? fieldLabels.get("motorWheelId") : null;
        Label radialWheelLabel = fieldLabels != null ? fieldLabels.get("radialWheelId") : null;
        Label motorLabel = fieldLabels != null ? fieldLabels.get("motorId") : null;
        Label wheelSizeLabel = fieldLabels != null ? fieldLabels.get("wheelSize") : null;

        if (motorWheelField != null) {
            motorWheelField.setVisible(isMotorWheel);
            motorWheelField.setManaged(isMotorWheel);
        }
        if (motorWheelLabel != null) {
            motorWheelLabel.setVisible(isMotorWheel);
            motorWheelLabel.setManaged(isMotorWheel);
        }

        if (radialWheelField != null) {
            radialWheelField.setVisible(isRadialWheel);
            radialWheelField.setManaged(isRadialWheel);
        }
        if (radialWheelLabel != null) {
            radialWheelLabel.setVisible(isRadialWheel);
            radialWheelLabel.setManaged(isRadialWheel);
        }

        if (motorField != null) {
            motorField.setVisible(isRadialWheel);
            motorField.setManaged(isRadialWheel);
        }
        if (motorLabel != null) {
            motorLabel.setVisible(isRadialWheel);
            motorLabel.setManaged(isRadialWheel);
        }

        if (wheelSizeField != null) {
            wheelSizeField.setVisible(isRadialWheel);
            wheelSizeField.setManaged(isRadialWheel);
        }
        if (wheelSizeLabel != null) {
            wheelSizeLabel.setVisible(isRadialWheel);
            wheelSizeLabel.setManaged(isRadialWheel);
        }
    }

    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        addTextFieldListener("seriesName", this::updateFullMarking);
        addTextFieldListener("executionType", this::updateFullMarking);
        addTextFieldListener("ductSize", this::updateFullMarking);
        addTextFieldListener("poles", this::updateFullMarking);
        addTextFieldListener("voltage", this::updateFullMarking);
        addTextFieldListener("wheelSize", this::updateFullMarking);

        updateFullMarking();
    }

    private void addTextFieldListener(String fieldName, Runnable callback) {
        TextField textField = getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    private void updateFullMarking() {
        if (fullMarkingField == null) return;

        String seriesName = getFieldValue("seriesName");
        String executionType = getFieldValue("executionType");
        String ductSize = getFieldValue("ductSize");
        String poles = getFieldValue("poles");
        String voltage = getFieldValue("voltage");
        String wheelSize = getFieldValue("wheelSize");

        String ductFanType = "";
        Control typeControl = fieldControls.get("ductFanType");
        if (typeControl instanceof ComboBox) {
            Object userData = ((ComboBox<?>) typeControl).getUserData();
            ductFanType = userData != null ? userData.toString() : "";
        }

        StringBuilder fullMarking = new StringBuilder();

        if (seriesName != null && !seriesName.isEmpty()) {
            fullMarking.append(seriesName);
        }

        if (executionType != null && !executionType.isEmpty()) {
            fullMarking.append("-").append(executionType);
        }

        if ("MOTOR_WHEEL".equals(ductFanType)) {
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
            if (ductSize != null && !ductSize.isEmpty()) {
                fullMarking.append("-").append(ductSize);
            }
            if (wheelSize != null && !wheelSize.isEmpty()) {
                fullMarking.append("/").append(wheelSize);
            }
            if (poles != null && !poles.isEmpty()) {
                fullMarking.append(".").append(poles);
            }
            String voltageCode = getVoltageCode(voltage);
            if (voltageCode != null && !voltageCode.isEmpty()) {
                fullMarking.append(voltageCode);
            }
        }

        String newMarking = fullMarking.toString();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    private String getVoltageCode(String voltage) {
        if (voltage == null) return "";
        if (voltage.equals("220")) return "E";
        if (voltage.equals("380")) return "D";
        return "";
    }

    private String getFieldValue(String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control == null) return "";
        if (control instanceof TextField) return ((TextField) control).getText().trim();
        if (control instanceof ComboBox) {
            ComboBox<String> combo = (ComboBox<String>) control;
            if ("ductFanType".equals(fieldName)) {
                Object userData = combo.getUserData();
                return userData != null ? userData.toString() : "";
            }
            Object value = combo.getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

}
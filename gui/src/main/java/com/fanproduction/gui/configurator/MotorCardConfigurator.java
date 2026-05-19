package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.*;

import java.util.Map;

/**
 * Настройка специальных полей для карточки электродвигателя.
 */
public class MotorCardConfigurator implements CardFieldConfigurator {

    private TextField ratedSpeedField;
    private TextField fullMarkingField;
    private String lastAutoMarking = "";
    private Map<String, Node> fieldControls;

    @Override
    public void setupFields(Map<String, Node> fieldControls, Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints, boolean existingCardExists) {
        this.fieldControls = fieldControls;

        // Сохраняем ссылки на специальные поля
        ratedSpeedField = CardFieldConfigurator.getTextField(fieldControls, "ratedSpeedRpm");
        fullMarkingField = CardFieldConfigurator.getTextField(fieldControls, "fullMarking");

        // Настройка условного отображения полей (огнестойкий, взрывозащищённый)
        setupConditionalVisibility();

        // Настройка автоматического расчёта номинальной скорости
        setupRatedSpeedCalculation();

        // Настройка автоматического формирования полной маркировки
        setupFullMarkingGeneration();
        // Выбор одной галочки
        setupExclusiveSelection();

        // Автоматическое заполнение наименования
        if (!existingCardExists) {
            TextField nameField = CardFieldConfigurator.getTextField(fieldControls, "name");
            if (nameField != null && nameField.getText().isEmpty()) {
                nameField.setText("Электродвигатель");
            }
        }
    }

    /**
     * Настройка условного отображения полей
     */
    private void setupConditionalVisibility() {
        // Огнестойкость -> поле маркировки огнестойкости и предельной температуры
        CheckBox fireproofCheck = CardFieldConfigurator.getCheckBox(fieldControls, "fireproof");
        Node fireproofMarkingField = fieldControls.get("fireproofMarking");
        Node tempField = fieldControls.get("maxTemperature");

        if (fireproofCheck != null) {
            boolean isVisible = fireproofCheck.isSelected();

            if (fireproofMarkingField != null) {
                fireproofMarkingField.setVisible(isVisible);
                fireproofMarkingField.setManaged(isVisible);
            }
            if (tempField != null) {
                tempField.setVisible(isVisible);
                tempField.setManaged(isVisible);
            }

            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (fireproofMarkingField != null) {
                    fireproofMarkingField.setVisible(val);
                    fireproofMarkingField.setManaged(val);
                }
                if (tempField != null) {
                    tempField.setVisible(val);
                    tempField.setManaged(val);
                }
                updateFullMarking();
            });
        }

        // Взрывозащита -> поле маркировки взрывозащиты
        CheckBox explosionCheck = CardFieldConfigurator.getCheckBox(fieldControls, "explosionProof");
        Node explosionMarkingField = fieldControls.get("explosionMarking");

        if (explosionCheck != null && explosionMarkingField != null) {
            boolean isVisible = explosionCheck.isSelected();
            explosionMarkingField.setVisible(isVisible);
            explosionMarkingField.setManaged(isVisible);

            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                explosionMarkingField.setVisible(val);
                explosionMarkingField.setManaged(val);
                updateFullMarking();
            });
        }
    }

    /**
     * Настройка автоматического расчёта номинальной скорости
     */
    private void setupRatedSpeedCalculation() {
        ComboBox<String> polesCombo = CardFieldConfigurator.getComboBox(fieldControls, "poles");
        if (polesCombo != null && ratedSpeedField != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> {
                updateRatedSpeed(polesCombo);
                updateFullMarking(); // при изменении полюсов обновляем маркировку
            });
            updateRatedSpeed(polesCombo);
        }
    }

    /**
     * Обновляет номинальную скорость на основе количества полюсов
     */
    private void updateRatedSpeed(ComboBox<String> polesCombo) {
        if (ratedSpeedField == null || polesCombo == null) return;

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
     * Настройка автоматического формирования полной маркировки
     */
    private void setupFullMarkingGeneration() {
        if (fullMarkingField == null) return;

        // Добавляем слушатели на поля, влияющие на маркировку
        addTextFieldListener("series", this::updateFullMarking);
        addTextFieldListener("motorType", this::updateFullMarking);
        addTextFieldListener("climateType", this::updateFullMarking);
        addTextFieldListener("fireproofMarking", this::updateFullMarking);
        addTextFieldListener("explosionMarking", this::updateFullMarking);
        addTextFieldListener("mountingType", this::updateFullMarking);

        addCheckBoxListener("generalPurpose", this::updateFullMarking);
        addCheckBoxListener("fireproof", this::updateFullMarking);
        addCheckBoxListener("explosionProof", this::updateFullMarking);

        ComboBox<String> polesCombo = CardFieldConfigurator.getComboBox(fieldControls, "poles");
        if (polesCombo != null) {
            polesCombo.valueProperty().addListener((obs, old, val) -> updateFullMarking());
        }

        updateFullMarking();
    }

    /**
     * Обновляет полную маркировку на основе заполненных полей
     */
    private void updateFullMarking() {
        if (fullMarkingField == null) return;

        String series = getFieldValue("series");
        String motorType = getFieldValue("motorType");
        String poles = getFieldValue("poles");
        String climateType = getFieldValue("climateType");
        String mountingType = getFieldValue("mountingType");

        boolean isGeneralPurpose = isSelected("generalPurpose");
        boolean isFireproof = isSelected("fireproof");
        boolean isExplosionProof = isSelected("explosionProof");

        String fireproofMarking = isFireproof ? getFieldValue("fireproofMarking") : "";
        String explosionMarking = isExplosionProof ? getFieldValue("explosionMarking") : "";

        StringBuilder fullMarking = new StringBuilder();

        // Серия
        if (series != null && !series.isEmpty()) {
            fullMarking.append(series).append(" ");
        }

        // Маркировка огнестойкости или взрывозащиты (вставляется после серии)
        if (isFireproof && fireproofMarking != null && !fireproofMarking.isEmpty()) {
            fullMarking.append(fireproofMarking).append(" ");
        } else if (isExplosionProof && explosionMarking != null && !explosionMarking.isEmpty()) {
            fullMarking.append(explosionMarking).append(" ");
        }

        // Тип двигателя
        if (motorType != null && !motorType.isEmpty()) {
            fullMarking.append(motorType);
        }

        // Количество полюсов
        if (poles != null && !poles.isEmpty()) {
            fullMarking.append(poles);
        }

        // Монтажное исполнение
        if (mountingType != null && !mountingType.isEmpty()) {
            fullMarking.append(" ").append(mountingType);
        }

        // Климатическое исполнение
        if (climateType != null && !climateType.isEmpty()) {
            fullMarking.append(" ").append(climateType);
        }

        String newMarking = fullMarking.toString().trim();
        String currentMarking = fullMarkingField.getText();

        // Обновляем только если пользователь не редактировал поле вручную
        if (currentMarking == null || currentMarking.isEmpty() || currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    /**
     * Добавляет слушатель на текстовое поле
     */
    private void addTextFieldListener(String fieldName, Runnable callback) {
        TextField textField = CardFieldConfigurator.getTextField(fieldControls, fieldName);
        if (textField != null) {
            textField.textProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Добавляет слушатель на CheckBox
     */
    private void addCheckBoxListener(String fieldName, Runnable callback) {
        CheckBox checkBox = CardFieldConfigurator.getCheckBox(fieldControls, fieldName);
        if (checkBox != null) {
            checkBox.selectedProperty().addListener((obs, old, val) -> callback.run());
        }
    }

    /**
     * Проверяет, выбрана ли галочка
     */
    private boolean isSelected(String fieldName) {
        CheckBox checkBox = CardFieldConfigurator.getCheckBox(fieldControls, fieldName);
        return checkBox != null && checkBox.isSelected();
    }

    /**
     * Получает значение поля по имени
     */
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

    private CheckBox getCheckBox(Map<String, Node> controls, String name) {
        return CardFieldConfigurator.getCheckBox(controls, name);
    }
}
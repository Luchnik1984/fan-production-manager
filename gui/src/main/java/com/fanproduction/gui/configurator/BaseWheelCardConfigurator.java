package com.fanproduction.gui.configurator;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;

/**
 * Базовый конфигуратор для карточек колёс (радиального и осевого).
 * Содержит общую логику для управления видимостью, очистки полей и обновления fullMarking.
 */
public abstract class BaseWheelCardConfigurator implements CardFieldConfigurator {

    protected String lastAutoFullMarking = "";
    protected String lastAutoWheelFormula = "";
    protected Map<String, String> initialValues = new HashMap<>();
    protected boolean initialIsPartner = false;
    protected boolean initialIsOwn = false;
    protected String initialExecutionMarking = "";

    // ==========================================================
    // АБСТРАКТНЫЕ МЕТОДЫ ДЛЯ ДОЧЕРНИХ КЛАССОВ
    // ==========================================================

    /**
     * Возвращает список полей для партнёрского колеса
     */
    protected abstract String[] getPartnerFields();

    /**
     * Возвращает список полей для фирменного колеса
     */
    protected abstract String[] getOwnFields();

    /**
     * Возвращает список полей для проверки изменений fullMarking
     */
    protected abstract String[] getFieldsToCheckForFullMarking(boolean isPartner);

    /**
     * Возвращает список полей для инициализации начальных значений
     */
    protected abstract String[] getInitializationFields();

    /**
     * Формирует полную маркировку для фирменного колеса
     */
    protected abstract String buildOwnFullMarking(Map<String, Node> fieldControls);

    /**
     * Обновляет формулу колеса (если есть)
     */
    protected abstract void updateWheelFormula(Map<String, Node> fieldControls);

    /**
     * Настраивает специфичные для дочернего класса слушатели
     */
    protected abstract void setupSpecificListeners(Map<String, Node> fieldControls, boolean existingCardExists);

    // ==========================================================
    // ОБЩИЕ МЕТОДЫ ДЛЯ ВСЕХ КОЛЁС
    // ==========================================================

    /**
     * Очищает указанные поля
     */
    protected void clearFields(Map<String, Node> fieldControls, String[] fieldNames) {
        for (String fieldName : fieldNames) {
            Node control = fieldControls.get(fieldName);
            if (control instanceof TextField) {
                ((TextField) control).clear();
            } else if (control instanceof ComboBox) {
                ((ComboBox<?>) control).setValue(null);
            }
        }
    }

    /**
     * Очищает поля партнёрского колеса
     */
    protected void clearPartnerFields(Map<String, Node> fieldControls) {
        clearFields(fieldControls, getPartnerFields());
    }

    /**
     * Очищает поля фирменного колеса
     */
    protected void clearOwnFields(Map<String, Node> fieldControls) {
        clearFields(fieldControls, getOwnFields());
        // Сбрасываем галочки типа изготовления (если есть)
        CheckBox assembledCheck = getCheckBox(fieldControls, "isAssembledFromComponents");
        CheckBox weldedCheck = getCheckBox(fieldControls, "isWeldedFromMaterials");
        if (assembledCheck != null) assembledCheck.setSelected(false);
        if (weldedCheck != null) weldedCheck.setSelected(false);
    }

    /**
     * Сохраняет начальные значения полей
     */
    protected void storeInitialValues(Map<String, Node> fieldControls) {
        String[] fieldNames = getInitializationFields();
        initialValues = new HashMap<>();
        for (String fieldName : fieldNames) {
            initialValues.put(fieldName, getFieldValue(fieldControls, fieldName));
        }
        initialIsPartner = isSelected(fieldControls, "isPartnerWheel");
        initialIsOwn = isSelected(fieldControls, "isOwnProduction");
        initialExecutionMarking = getExecutionMarking(fieldControls);
    }

    /**
     * Настраивает взаимоисключение галочек "Партнёрское/Фирменное"
     * (оригинальная версия, без дополнительных Runnable)
     */
    protected void setupWheelTypeExclusiveSelection(Map<String, Node> fieldControls,
                                                    Map<String, Label> fieldLabels,
                                                    Map<String, Label> fieldHints) {
        CheckBox partnerCheck = getCheckBox(fieldControls, "isPartnerWheel");
        CheckBox ownCheck = getCheckBox(fieldControls, "isOwnProduction");

        if (partnerCheck != null) {
            partnerCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && ownCheck != null) {
                    ownCheck.setSelected(false);
                }
                if (!val) {
                    clearPartnerFields(fieldControls);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls);
                updateWheelFormula(fieldControls);
            });
        }

        if (ownCheck != null) {
            ownCheck.selectedProperty().addListener((obs, old, val) -> {
                if (val && partnerCheck != null) {
                    partnerCheck.setSelected(false);
                }
                if (!val) {
                    clearOwnFields(fieldControls);
                }
                setupVisibilityLogic(fieldControls, fieldLabels, fieldHints);
                updateFullMarking(fieldControls);
                updateWheelFormula(fieldControls);
            });
        }
    }

    /**
     * Управление видимостью полей в зависимости от типа колеса
     */
    protected void setupVisibilityLogic(Map<String, Node> fieldControls,
                                        Map<String, Label> fieldLabels,
                                        Map<String, Label> fieldHints) {
        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");

        setVisible(fieldControls, fieldLabels, fieldHints, "marking", isPartner);

        for (String fieldName : getOwnFields()) {
            setVisible(fieldControls, fieldLabels, fieldHints, fieldName, isOwn);
        }
    }

    /**
     * Обновляет полную маркировку (общая логика)
     */
    protected void updateFullMarking(Map<String, Node> fieldControls) {
        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        boolean isPartner = isSelected(fieldControls, "isPartnerWheel");
        boolean isOwn = isSelected(fieldControls, "isOwnProduction");

        if (!isPartner && !isOwn) {
            return;
        }

        // Получаем текущие значения
        String currentMarkingValue = getFieldValue(fieldControls, "marking");
        String currentExecutionMarking = getExecutionMarking(fieldControls);

        // Проверяем изменения
        boolean fieldsChanged = false;

        if (isPartner) {
            String initialMarking = initialValues.getOrDefault("marking", "");
            if (!currentMarkingValue.equals(initialMarking)) {
                fieldsChanged = true;
            }
        } else if (isOwn) {
            String[] fieldsToCheck = getFieldsToCheckForFullMarking(false);
            fieldsChanged = hasAnyFieldChanged(fieldControls, initialValues, fieldsToCheck);
            if (!fieldsChanged) {
                String initialExecution = initialExecutionMarking != null ? initialExecutionMarking : "";
                if (!currentExecutionMarking.equals(initialExecution)) {
                    fieldsChanged = true;
                }
            }
        }

        if (!fieldsChanged) {
            return;
        }

        String newFullMarking;
        if (isPartner) {
            newFullMarking = currentMarkingValue;
        } else {
            newFullMarking = buildOwnFullMarking(fieldControls);
        }

        String currentMarking = fullMarkingField.getText();
        if (currentMarking == null || currentMarking.isEmpty() ||
                currentMarking.equals(lastAutoFullMarking)) {
            fullMarkingField.setText(newFullMarking);
            lastAutoFullMarking = newFullMarking;
            storeInitialValues(fieldControls);
        }
    }
}
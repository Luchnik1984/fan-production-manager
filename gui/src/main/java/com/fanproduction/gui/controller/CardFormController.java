package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.FieldMetadataDto;
import com.fanproduction.gui.dto.ProductCardDto;
import com.fanproduction.gui.service.FieldMetadataService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

/**
 * Контроллер для формы создания/редактирования карточки продукции.
 */
public class CardFormController {

    private final Stage stage;
    private final String cardType;
    private final ProductCardDto existingCard;
    private final Runnable onSaveCallback;
    private final FieldMetadataService metadataService = new FieldMetadataService();

    private final Map<String, Control> fieldControls = new HashMap<>();
    private final Map<String, FieldMetadataDto> fieldMetadata = new HashMap<>();
    private final Map<String, TextField> referenceFields = new HashMap<>();

    // Специальные поля для электродвигателя
    private TextField ratedSpeedField;
    private TextField fullMarkingField;
    private String lastAutoMarking = "";

    private final Map<String, Integer> fieldRows = new HashMap<>();
    private final Map<String, Label> fieldLabels = new HashMap<>();
    private final Map<String, Label> fieldHints = new HashMap<>();

    public CardFormController(Stage owner, String cardType, ProductCardDto existingCard, Runnable onSaveCallback) {
        this.stage = new Stage();
        this.stage.initModality(Modality.WINDOW_MODAL);
        this.stage.initOwner(owner);
        this.cardType = cardType;
        this.existingCard = existingCard;
        this.onSaveCallback = onSaveCallback;

        initUI();
    }

    private void initUI() {
        String title = existingCard == null ? "Создание карточки" : "Редактирование карточки";
        stage.setTitle(title + " - " + getTypeDisplayName(cardType));

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f5f5;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        GridPane formGrid = createFormGrid();

        ScrollPane scrollPane = new ScrollPane(formGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);

        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        final Button finalSaveButton = saveButton;

        saveButton.setOnAction(e -> saveCard(finalSaveButton));
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        mainLayout.getChildren().addAll(titleLabel, scrollPane, buttonBox);

        Scene scene = new Scene(mainLayout, 700, 600);
        stage.setScene(scene);
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        List<FieldMetadataDto> fields = metadataService.getFieldsForType(cardType);

        int row = 0;

        // Поле "Наименование" (есть у всех карточек)
        Label nameLabel = new Label("Наименование:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        TextField nameField = new TextField();
        if (existingCard != null && existingCard.getName() != null) {
            nameField.setText(existingCard.getName());
        }
        nameField.setPromptText("Введите наименование");
        grid.add(nameLabel, 0, row);
        grid.add(nameField, 1, row);
        fieldControls.put("name", nameField);
        row++;

        // Динамические поля - добавляем ВСЕ поля (и видимые, и скрытые)
        for (FieldMetadataDto field : fields) {
            Label label = new Label(field.getLabel() + (field.isRequired() ? " *" : ":"));
            label.setStyle("-fx-font-weight: bold;");

            Control control = createControlForField(field);

            if (control != null) {
                grid.add(label, 0, row);
                grid.add(control, 1, row);
                fieldControls.put(field.getName(), control);
                fieldMetadata.put(field.getName(), field);
                fieldLabels.put(field.getName(), label);
                fieldRows.put(field.getName(), row);

                // Устанавливаем видимость в соответствии с метаданными
                boolean isVisible = field.isVisible();
                label.setVisible(isVisible);
                label.setManaged(isVisible);
                control.setVisible(isVisible);
                control.setManaged(isVisible);

                // Подсказка
                if (field.getHint() != null && !field.getHint().isEmpty()) {
                    Label hintLabel = new Label(field.getHint());
                    hintLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
                    grid.add(hintLabel, 1, row + 1);
                    fieldHints.put(field.getName(), hintLabel);
                    hintLabel.setVisible(isVisible);
                    hintLabel.setManaged(isVisible);
                    row++;
                }
            }
            row++;
        }

        // Автоматическое заполнение наименования для электродвигателя
        if ("MOTOR".equals(cardType) && existingCard == null) {
            TextField nameFieldCtrl = (TextField) fieldControls.get("name");
            if (nameFieldCtrl != null && nameFieldCtrl.getText().isEmpty()) {
                nameFieldCtrl.setText("Электродвигатель");
            }
        }

        // Настройка специальных полей для электродвигателя
        if ("MOTOR".equals(cardType)) {
            setupMotorSpecificFields();
        }

        return grid;
    }

    private Control createControlForField(FieldMetadataDto field) {
        Object existingValue = null;
        if (existingCard != null && existingCard.getFields() != null) {
            existingValue = existingCard.getFields().get(field.getName());
        }

        switch (field.getType()) {
            case "text":
                TextField textField = new TextField();
                if (existingValue != null) textField.setText(String.valueOf(existingValue));
                if (field.getDefaultValue() != null && existingValue == null) textField.setText(field.getDefaultValue());
                textField.setPromptText(field.getHint());
                return textField;

            case "number":
                TextField numberField = new TextField();
                if (existingValue != null) numberField.setText(String.valueOf(existingValue));
                if (field.getDefaultValue() != null && existingValue == null) numberField.setText(field.getDefaultValue());
                numberField.setPromptText("Введите число");
                return numberField;

            case "double":
                TextField doubleField = new TextField();
                if (existingValue != null) doubleField.setText(String.valueOf(existingValue));
                if (field.getDefaultValue() != null && existingValue == null) doubleField.setText(field.getDefaultValue());
                doubleField.setPromptText("Введите число (например: 5,5)");
                return doubleField;

            case "combobox":
                ComboBox<String> comboBox = new ComboBox<>();
                if (field.getOptions() != null) {
                    comboBox.getItems().addAll(field.getOptions());
                }
                if (existingValue != null) comboBox.setValue(String.valueOf(existingValue));
                if (field.getDefaultValue() != null && existingValue == null) comboBox.setValue(field.getDefaultValue());
                comboBox.setPromptText(field.getHint());
                return comboBox;

            case "boolean":
                CheckBox checkBox = new CheckBox();
                if (existingValue instanceof Boolean) checkBox.setSelected((Boolean) existingValue);
                if (field.getDefaultValue() != null && existingValue == null) {
                    checkBox.setSelected(Boolean.parseBoolean(field.getDefaultValue()));
                }
                return checkBox;

            case "reference":
                TextField refField = new TextField();
                refField.setEditable(false);
                refField.setPromptText("Не выбран");
                if (existingValue != null) {
                    refField.setText("ID: " + existingValue);
                    refField.setUserData(existingValue);
                }
                referenceFields.put(field.getName(), refField);
                return refField;

            default:
                TextField defaultField = new TextField();
                if (existingValue != null) defaultField.setText(String.valueOf(existingValue));
                return defaultField;
        }
    }

    /**
     * Настройка специальных полей для карточки электродвигателя
     */
    private void setupMotorSpecificFields() {
        // Сохраняем ссылки на специальные поля
        if (fieldControls.containsKey("ratedSpeedRpm")) {
            ratedSpeedField = (TextField) fieldControls.get("ratedSpeedRpm");
            ratedSpeedField.setEditable(true);
        }

        if (fieldControls.containsKey("fullMarking")) {
            fullMarkingField = (TextField) fieldControls.get("fullMarking");
            fullMarkingField.setEditable(true);
        }

        // Настройка условного отображения полей
        setupConditionalVisibility();

        // Добавляем слушатели для автоматического обновления номинальной скорости
        Control polesControl = fieldControls.get("poles");
        if (polesControl instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> polesCombo = (ComboBox<String>) polesControl;
            polesCombo.valueProperty().addListener((obs, old, val) -> {
                updateRatedSpeed();
                updateFullMarking();
            });
            updateRatedSpeed();
        }

        // Слушатели для обновления полной маркировки
        TextField seriesField = (TextField) fieldControls.get("series");
        if (seriesField != null) {
            seriesField.textProperty().addListener((obs, old, val) -> updateFullMarking());
        }

        TextField motorTypeField = (TextField) fieldControls.get("motorType");
        if (motorTypeField != null) {
            motorTypeField.textProperty().addListener((obs, old, val) -> updateFullMarking());
        }

        Control climateControl = fieldControls.get("climateType");
        if (climateControl instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> climateCombo = (ComboBox<String>) climateControl;
            climateCombo.valueProperty().addListener((obs, old, val) -> updateFullMarking());
        } else if (climateControl instanceof TextField) {
            ((TextField) climateControl).textProperty().addListener((obs, old, val) -> updateFullMarking());
        }

        updateFullMarking();
    }

    /**
     * Настройка условного отображения полей (огнестойкий, взрывозащищённый)
     */
    private void setupConditionalVisibility() {
        // Огнестойкий -> поле предельной температуры
        CheckBox fireproofCheck = (CheckBox) fieldControls.get("fireproof");
        Control tempField = fieldControls.get("maxTemperature");
        Label tempLabel = fieldLabels.get("maxTemperature");
        Label tempHint = fieldHints.get("maxTemperature");

        if (fireproofCheck != null && tempField != null) {
            // Устанавливаем начальную видимость
            boolean isVisible = fireproofCheck.isSelected();
            if (tempLabel != null) {
                tempLabel.setVisible(isVisible);
                tempLabel.setManaged(isVisible);
            }
            tempField.setVisible(isVisible);
            tempField.setManaged(isVisible);
            if (tempHint != null) {
                tempHint.setVisible(isVisible);
                tempHint.setManaged(isVisible);
            }

            // Слушатель изменения галочки
            fireproofCheck.selectedProperty().addListener((obs, old, val) -> {
                if (tempLabel != null) {
                    tempLabel.setVisible(val);
                    tempLabel.setManaged(val);
                }
                tempField.setVisible(val);
                tempField.setManaged(val);
                if (tempHint != null) {
                    tempHint.setVisible(val);
                    tempHint.setManaged(val);
                }
            });
        }

        // Взрывозащищённый -> поле маркировки взрывозащиты
        CheckBox explosionCheck = (CheckBox) fieldControls.get("explosionProof");
        Control markingField = fieldControls.get("explosionMarking");
        Label markingLabel = fieldLabels.get("explosionMarking");
        Label markingHint = fieldHints.get("explosionMarking");

        if (explosionCheck != null && markingField != null) {
            // Устанавливаем начальную видимость
            boolean isVisible = explosionCheck.isSelected();
            if (markingLabel != null) {
                markingLabel.setVisible(isVisible);
                markingLabel.setManaged(isVisible);
            }
            markingField.setVisible(isVisible);
            markingField.setManaged(isVisible);
            if (markingHint != null) {
                markingHint.setVisible(isVisible);
                markingHint.setManaged(isVisible);
            }

            // Слушатель изменения галочки
            explosionCheck.selectedProperty().addListener((obs, old, val) -> {
                if (markingLabel != null) {
                    markingLabel.setVisible(val);
                    markingLabel.setManaged(val);
                }
                markingField.setVisible(val);
                markingField.setManaged(val);
                if (markingHint != null) {
                    markingHint.setVisible(val);
                    markingHint.setManaged(val);
                }
            });
        }
    }

    /**
     * Обновляет номинальную скорость на основе количества полюсов
     */
    private void updateRatedSpeed() {
        if (ratedSpeedField == null) return;

        Control polesControl = fieldControls.get("poles");
        if (polesControl instanceof ComboBox) {
            @SuppressWarnings("unchecked")
            ComboBox<String> polesCombo = (ComboBox<String>) polesControl;
            String value = polesCombo.getValue();
            if (value != null) {
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
        } else if (polesControl instanceof TextField) {
            String text = ((TextField) polesControl).getText().trim();
            if (!text.isEmpty()) {
                try {
                    int poles = Integer.parseInt(text);
                    int ratedSpeed = 6000 / poles;
                    ratedSpeedField.setText(String.valueOf(ratedSpeed));
                } catch (NumberFormatException e) {
                    ratedSpeedField.setText("");
                }
            } else {
                ratedSpeedField.setText("");
            }
        }
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

        StringBuilder marking = new StringBuilder();
        if (series != null && !series.isEmpty()) {
            marking.append(series).append(" ");
        }
        if (motorType != null && !motorType.isEmpty()) {
            marking.append(motorType);
        }
        if (poles != null && !poles.isEmpty()) {
            marking.append(poles);
        }
        if (climateType != null && !climateType.isEmpty()) {
            marking.append(" ").append(climateType);
        }

        String newMarking = marking.toString().trim();
        String currentMarking = fullMarkingField.getText();

        if (currentMarking == null || currentMarking.isEmpty() ||
                currentMarking.equals(lastAutoMarking)) {
            fullMarkingField.setText(newMarking);
            lastAutoMarking = newMarking;
        }
    }

    /**
     * Получает значение поля по имени
     */
    private String getFieldValue(String fieldName) {
        Control control = fieldControls.get(fieldName);
        if (control == null) return "";

        if (control instanceof TextField) {
            return ((TextField) control).getText().trim();
        } else if (control instanceof ComboBox) {
            Object value = ((ComboBox<?>) control).getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

    private Object getControlValue(Control control, String fieldName, FieldMetadataDto metadata) {
        if (control instanceof TextField) {
            String text = ((TextField) control).getText().trim();
            if (text.isEmpty()) return null;

            if (metadata != null) {
                if ("number".equals(metadata.getType())) {
                    try {
                        return Integer.parseInt(text);
                    } catch (NumberFormatException e) {
                        return text;
                    }
                } else if ("double".equals(metadata.getType())) {
                    try {
                        return Double.parseDouble(text.replace(',', '.'));
                    } catch (NumberFormatException e) {
                        return text;
                    }
                }
            }
            return text;
        } else if (control instanceof ComboBox) {
            return ((ComboBox<?>) control).getValue();
        } else if (control instanceof CheckBox) {
            return ((CheckBox) control).isSelected();
        }
        return null;
    }

    private void saveCard(Button saveButton) {
        Map<String, Object> fields = new HashMap<>();

        for (Map.Entry<String, Control> entry : fieldControls.entrySet()) {
            String fieldName = entry.getKey();
            Control control = entry.getValue();
            FieldMetadataDto metadata = fieldMetadata.get(fieldName);

            Object value = getControlValue(control, fieldName, metadata);

            if (value != null) {
                fields.put(fieldName, value);
            }
        }

        // Добавляем значения из reference полей
        for (Map.Entry<String, TextField> entry : referenceFields.entrySet()) {
            String fieldName = entry.getKey();
            TextField refField = entry.getValue();
            Object value = refField.getUserData();
            if (value != null) {
                fields.put(fieldName, value);
            }
        }

        String name = "";
        Control nameControl = fieldControls.get("name");
        if (nameControl instanceof TextField) {
            name = ((TextField) nameControl).getText().trim();
        }

        final String finalName = name;
        final Map<String, Object> finalFields = fields;
        final Long finalId = existingCard != null ? existingCard.getId() : null;

        if (finalName.isEmpty()) {
            showAlert("Ошибка", "Наименование обязательно для заполнения", Alert.AlertType.ERROR);
            return;
        }

        for (Map.Entry<String, FieldMetadataDto> entry : fieldMetadata.entrySet()) {
            FieldMetadataDto field = entry.getValue();
            if (field.isRequired()) {
                Object value = finalFields.get(field.getName());
                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                    showAlert("Ошибка", "Поле '" + field.getLabel() + "' обязательно для заполнения", Alert.AlertType.ERROR);
                    return;
                }
            }
        }

        saveButton.setDisable(true);
        saveButton.setText("Сохранение...");

        new Thread(() -> {
            try {
                ApiResponse<ProductCardDto> response;
                if (existingCard == null) {
                    response = ProductCardClient.createCard(cardType, finalName, finalFields);
                } else {
                    response = ProductCardClient.updateCard(finalId, finalName, finalFields);
                }

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showAlert("Успешно", "Карточка " + (existingCard == null ? "создана" : "обновлена"), Alert.AlertType.INFORMATION);
                        if (onSaveCallback != null) {
                            onSaveCallback.run();
                        }
                        stage.close();
                    } else {
                        saveButton.setDisable(false);
                        saveButton.setText("Сохранить");
                        showAlert("Ошибка", response.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    saveButton.setDisable(false);
                    saveButton.setText("Сохранить");
                    showAlert("Ошибка", "Ошибка сохранения: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private String getTypeDisplayName(String cardType) {
        Map<String, String> displayMap = Map.of(
                "MOTOR", "Электродвигатель",
                "MOTOR_WHEEL", "Мотор-колесо",
                "RADIAL_WHEEL", "Радиальное колесо",
                "AXIAL_FAN", "Осевой вентилятор",
                "RADIAL_FAN", "Радиальный вентилятор",
                "DUCT_FAN", "Канальный вентилятор",
                "CUP", "Стакан",
                "ACCESSORY", "Комплектующее"
        );
        return displayMap.getOrDefault(cardType, cardType);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void show() {
        stage.showAndWait();
    }
}
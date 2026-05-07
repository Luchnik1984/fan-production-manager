package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.configurator.CardFormConfigurator;
import com.fanproduction.gui.dto.SelectableItem;
import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.service.FieldMetadataService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public class CardFormController {

    private final Stage stage;
    private final String cardType;
    private final ProductCardDto existingCard;
    private final Runnable onSaveCallback;
    private final FieldMetadataService metadataService = new FieldMetadataService();
    private ProductComponentsController componentsTabController;
    private ProductMaterialsController materialsTabController;

    private final Map<String, Control> fieldControls = new HashMap<>();
    private final Map<String, FieldMetadataDto> fieldMetadata = new HashMap<>();
    private final Map<String, TextField> referenceFields = new HashMap<>();
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

        // ==========================================
        // СОЗДАЁМ TabPane ДЛЯ ВКЛАДОК
        // ==========================================
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(500);

        // --- Вкладка 1: Основные поля (форма) ---
        Tab mainTab = new Tab("Основные поля");
        mainTab.setClosable(false);

        // Создаём форму (существующий метод)
        GridPane formGrid = createFormGrid();

        // Оборачиваем форму в ScrollPane для прокрутки
        ScrollPane scrollPane = new ScrollPane(formGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);

        mainTab.setContent(scrollPane);
        tabPane.getTabs().add(mainTab);

        // --- Вкладка 2: Комплектующие ---
        Tab componentsTab = createComponentsTab();
        tabPane.getTabs().add(componentsTab);

        // --- Вкладка 3: материалы ---
        Tab materialsTab = createMaterialsTab();
        tabPane.getTabs().add(materialsTab);

        // ==========================================
        // БЛОК С КНОПКАМИ
        // ==========================================
        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");

        saveButton.setOnAction(e -> saveCard(saveButton));
        cancelButton.setOnAction(e -> stage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        // Добавляем всё в главный layout
        mainLayout.getChildren().addAll(titleLabel, tabPane, buttonBox);

        Scene scene = new Scene(mainLayout, 1000, 600);
        stage.setScene(scene);
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        List<FieldMetadataDto> fields = metadataService.getFieldsForType(cardType);

        int row = 0;

        // Поле "Наименование"
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

        for (FieldMetadataDto field : fields) {
            Label label = new Label(field.getLabel() + (field.isRequired() ? " *" : ":"));
            label.setStyle("-fx-font-weight: bold;");

            Control control = createControlForField(field);

            grid.add(label, 0, row);
            grid.add(control, 1, row);
            fieldControls.put(field.getName(), control);
            fieldMetadata.put(field.getName(), field);
            fieldLabels.put(field.getName(), label);

            boolean isVisible = field.isVisible();
            label.setVisible(isVisible);
            label.setManaged(isVisible);
            control.setVisible(isVisible);
            control.setManaged(isVisible);

            if (field.getHint() != null && !field.getHint().isEmpty()) {
                Label hintLabel = new Label(field.getHint());
                hintLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
                grid.add(hintLabel, 1, row + 1);
                fieldHints.put(field.getName(), hintLabel);
                hintLabel.setVisible(isVisible);
                hintLabel.setManaged(isVisible);
                row++;
            }
            row++;
        }

        CardFormConfigurator.configure(cardType, fieldControls, fieldLabels, fieldHints, existingCard != null);

        return grid;
    }

    private Control createControlForField(FieldMetadataDto field) {
        Object existingValue = null;
        if (existingCard != null && existingCard.getFields() != null) {
            existingValue = existingCard.getFields().get(field.getName());
        }

        return switch (field.getType()) {
            case "text" -> createTextField(field, existingValue);
            case "number" -> createNumberField(field, existingValue);
            case "double" -> createDoubleField(field, existingValue);
            case "combobox" -> createComboBox(field, existingValue);
            case "boolean" -> createCheckBox(field, existingValue);
            case "selectable" -> createSelectableComboBox(field, existingValue);
            case "hidden" -> {
                TextField hiddenField = new TextField();
                hiddenField.setVisible(false);
                hiddenField.setManaged(false);
                if (existingValue != null) {
                    hiddenField.setText(String.valueOf(existingValue));
                }
                yield hiddenField;
            }
            default -> createDefaultField(field, existingValue);
        };
    }

    private Control createSelectableComboBox(FieldMetadataDto field, Object existingValue) {
        String refType = field.getReferenceType();

        System.out.println("=== createSelectableComboBox ===");
        System.out.println("field: " + field.getName());
        System.out.println("refType: " + refType);

        ComboBox<SelectableItem> comboBox = new ComboBox<>();
        comboBox.setPromptText(field.getHint() != null ? field.getHint() : "Выберите");

        Long existingId = null;
        if (existingValue instanceof Number) {
            existingId = ((Number) existingValue).longValue();
        } else if (existingValue instanceof String) {
            try {
                existingId = Long.parseLong((String) existingValue);
            } catch (NumberFormatException ignored) {}
        }

        loadReferenceData(comboBox, refType, existingId);

        comboBox.valueProperty().addListener((obs, old, newVal) -> {
            if (newVal != null && newVal.getId() != null) {
                autoFillFromSelection(refType, newVal.getId());
            }
        });

        return comboBox;
    }

    private TextField createTextField(FieldMetadataDto field, Object existingValue) {
        TextField textField = new TextField();
        if (existingValue != null) textField.setText(String.valueOf(existingValue));
        if (field.getDefaultValue() != null && existingValue == null) textField.setText(field.getDefaultValue());
        textField.setPromptText(field.getHint());
        return textField;
    }

    private TextField createNumberField(FieldMetadataDto field, Object existingValue) {
        TextField numberField = new TextField();
        if (existingValue != null) numberField.setText(String.valueOf(existingValue));
        if (field.getDefaultValue() != null && existingValue == null) numberField.setText(field.getDefaultValue());
        numberField.setPromptText("Введите число");
        return numberField;
    }

    private TextField createDoubleField(FieldMetadataDto field, Object existingValue) {
        TextField doubleField = new TextField();
        if (existingValue != null) doubleField.setText(String.valueOf(existingValue));
        if (field.getDefaultValue() != null && existingValue == null) doubleField.setText(field.getDefaultValue());
        doubleField.setPromptText("Введите число (например: 5,5)");
        return doubleField;
    }

    private Control createComboBox(FieldMetadataDto field, Object existingValue) {
        String refType = field.getReferenceType();
        boolean isReference = refType != null && !refType.isEmpty();

        if (isReference) {
            System.out.println("=== Creating REFERENCE ComboBox for " + field.getName());
            System.out.println("refType: " + refType);
            System.out.println("existingValue: " + existingValue);
            ComboBox<SelectableItem> refComboBox = new ComboBox<>();
            refComboBox.setPromptText(field.getHint() != null ? field.getHint() : "Выберите");

            Long existingId = null;
            if (existingValue instanceof Number) {
                existingId = ((Number) existingValue).longValue();
            } else if (existingValue instanceof String) {
                try {
                    existingId = Long.parseLong((String) existingValue);
                } catch (NumberFormatException e) {
                    // Игнорируем
                }
            }

            loadReferenceData(refComboBox, refType, existingId);

            refComboBox.valueProperty().addListener((obs, old, newVal) -> {
                if (newVal != null && newVal.getId() != null) {
                    autoFillFromSelection(refType, newVal.getId());
                }
            });

            return refComboBox;
        } else {
            ComboBox<String> comboBox = new ComboBox<>();
            if (field.getOptions() != null) {
                comboBox.getItems().addAll(field.getOptions());
            }
            if (existingValue != null) {
                comboBox.setValue(String.valueOf(existingValue));
            }
            if (field.getDefaultValue() != null && existingValue == null) {
                comboBox.setValue(field.getDefaultValue());
            }
            if (field.getHint() != null && !field.getHint().isEmpty()) {
                comboBox.setPromptText(field.getHint());
            }
            return comboBox;
        }
    }

    private CheckBox createCheckBox(FieldMetadataDto field, Object existingValue) {
        CheckBox checkBox = new CheckBox();
        if (existingValue instanceof Boolean) checkBox.setSelected((Boolean) existingValue);
        if (field.getDefaultValue() != null && existingValue == null) {
            checkBox.setSelected(Boolean.parseBoolean(field.getDefaultValue()));
        }
        return checkBox;
    }

    private TextField createDefaultField(FieldMetadataDto field, Object existingValue) {
        TextField defaultField = new TextField();
        if (existingValue != null) defaultField.setText(String.valueOf(existingValue));
        return defaultField;
    }

    private void loadReferenceData(ComboBox<SelectableItem> comboBox, String referenceType, Long existingId) {
        System.out.println("=== loadReferenceData ===");
        System.out.println("referenceType: " + referenceType);
        System.out.println("existingId: " + existingId);

        comboBox.getItems().clear();
        comboBox.getItems().add(new SelectableItem(null, "Загрузка..."));

        new Thread(() -> {
            try {
                List<ProductCardDto> items = switch (referenceType) {
                    case "MOTOR_WHEEL" -> ProductCardClient.getCardsByType("MOTOR_WHEEL");
                    case "RADIAL_WHEEL" -> ProductCardClient.getCardsByType("RADIAL_WHEEL");
                    case "MOTOR" -> ProductCardClient.getCardsByType("MOTOR");
                    case "AXIAL_WHEEL" -> ProductCardClient.getCardsByType("AXIAL_WHEEL");
                    default -> new ArrayList<>();
                };

                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    if (!items.isEmpty()) {
                        SelectableItem selectedItem = null;
                        for (ProductCardDto dto : items) {
                            // Используем полную маркировку для отображения
                            String fullMarking = (String) dto.getFields().get("fullMarking");
                            String displayName;
                            if (fullMarking != null && !fullMarking.isEmpty()) {
                                displayName = fullMarking;
                            } else {
                                displayName = dto.getName() + " (" + dto.getCode() + ")";
                            }
                            SelectableItem item = new SelectableItem(dto.getId(), displayName);
                            comboBox.getItems().add(item);
                            if (existingId != null && existingId.equals(dto.getId())) {
                                selectedItem = item;
                            }
                        }
                        if (selectedItem != null) {
                            comboBox.setValue(selectedItem);
                        }
                    } else {
                        comboBox.getItems().add(new SelectableItem(null, "Нет данных"));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    comboBox.getItems().clear();
                    comboBox.getItems().add(new SelectableItem(null, "Ошибка загрузки"));
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void autoFillFromSelection(String referenceType, Long selectedId) {
        new Thread(() -> {
            try {
                ApiResponse<ProductCardDto> response = ProductCardClient.getCardById(selectedId);
                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        ProductCardDto dto = response.getData();
                        Map<String, Object> fields = dto.getFields();

                        // Полная маркировка выбранного компонента
                        String fullMarking = (String) fields.get("fullMarking");
                        if (fullMarking == null) {
                            fullMarking = dto.getName();
                        }

                        switch (referenceType) {
                            case "MOTOR_WHEEL":
                                setFieldValue("poles", fields.get("poles"));
                                setFieldValue("voltage", fields.get("voltage"));
                                setFieldValue("voltageCode", fields.get("voltageCode"));
                                setFieldValue("powerKw", fields.get("powerKw"));
                                setFieldValue("ratedSpeedRpm", fields.get("ratedSpeedRpm"));
                                setFieldValue("actualSpeedRpm", fields.get("actualSpeedRpm"));
                                // Сохраняем полную маркировку мотор-колеса
                                setFieldValue("motorWheelFullMarking", fullMarking);
                                break;
                            case "RADIAL_WHEEL":
                                setFieldValue("wheelSize", fields.get("size"));
                                // Сохраняем полную маркировку радиального колеса
                                setFieldValue("radialWheelFullMarking", fullMarking);
                                break;
                            case "AXIAL_WHEEL":
                                // Пока нет полей для автозаполнения
                                break;
                            case "MOTOR":
                                setFieldValue("poles", fields.get("poles"));
                                setFieldValue("voltage", fields.get("voltage"));
                                // Используем отдельный метод для кода напряжения
                                setFieldValue("voltageCode", getVoltageCode(fields.get("voltage")));
                                setFieldValue("powerKw", fields.get("powerKw"));
                                setFieldValue("ratedSpeedRpm", fields.get("ratedSpeedRpm"));
                                setFieldValue("actualSpeedRpm", fields.get("actualSpeedRpm"));
                                setFieldValue("motorFullMarking", fullMarking);
                                break;
                        }
                        updateFullMarking();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setFieldValue(String fieldName, Object value) {
        Control control = fieldControls.get(fieldName);
        if (control instanceof TextField && value != null) {
            ((TextField) control).setText(value.toString());
        }
    }

    private void updateFullMarking() {
        // Полная маркировка обновится через слушатели в конфигураторе
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
        } else if (control instanceof ComboBox<?> combo) {
            Object value = combo.getValue();
            if (value instanceof SelectableItem) {
                return ((SelectableItem) value).getId();
            }
            return value;
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
                        Long savedId = response.getData() != null ? response.getData().getId() : null;


                        // ОБНОВЛЯЕМ ВКЛАДКУ КОМПЛЕКТУЮЩИХ ПОСЛЕ СОХРАНЕНИЯ
                        if (componentsTabController != null && savedId != null) {
                            componentsTabController.refresh(savedId);
                        }

                        // ОБНОВЛЯЕМ ВКЛАДКУ МАТЕРИАЛОВ ПОСЛЕ СОХРАНЕНИЯ
                        if (materialsTabController != null && savedId != null) {
                            materialsTabController.refresh(savedId);
                        }

                        showAlert("Успешно", "Карточка " + (existingCard == null ? "создана" : "обновлена"),
                                Alert.AlertType.INFORMATION);
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
                "RADIAL_WHEEL", "Колесо радиальное",
                "AXIAL_WHEEL", "Колесо осевое",
                "AXIAL_FAN", "Вентилятор осевой",
                "RADIAL_FAN", "Вентилятор радиальный",
                "DUCT_FAN", "Вентилятор канальный",
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

    /**
     * Преобразует напряжение в код напряжения
     * @param voltage напряжение (220, 380, null)
     * @return код напряжения (E, D, или пустая строка)
     */
    private String getVoltageCode(Object voltage) {
        if (voltage == null) return "";
        String voltageStr = voltage.toString();
        if (voltageStr.equals("220")) return "E";
        if (voltageStr.equals("380")) return "D";
        return "";
    }

    private Tab createComponentsTab() {
        Tab tab = new Tab("Комплектующие");
        tab.setClosable(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ProductComponentsView.fxml"));
            Parent content = loader.load();

            ProductComponentsController controller = loader.getController();

            // Сохраняем ссылку на контроллер (теперь поле используется)
            this.componentsTabController = controller;

            // Если редактируем существующую карточку — передаём ID для загрузки компонентов
            if (existingCard != null && existingCard.getId() != null) {
                controller.refresh(existingCard.getId());
            } else {
                // Для новой карточки показываем сообщение, что нужно сначала сохранить
                controller.showNotSavedMessage();
            }

            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20px;");
            errorBox.getChildren().add(new Label("Ошибка загрузки комплектующих: " + e.getMessage()));
            tab.setContent(errorBox);
        }

        return tab;
    }

    private Tab createMaterialsTab() {
        Tab tab = new Tab("Материалы");
        tab.setClosable(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ProductMaterialsView.fxml"));
            Parent content = loader.load();

            ProductMaterialsController controller = loader.getController();
            this.materialsTabController = controller;

            if (existingCard != null && existingCard.getId() != null) {
                controller.refresh(existingCard.getId());
            } else {
                controller.showNotSavedMessage();
            }

            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20px;");
            errorBox.getChildren().add(new Label("Ошибка загрузки материалов: " + e.getMessage()));
            tab.setContent(errorBox);
        }

        return tab;
    }

}
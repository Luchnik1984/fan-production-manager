package com.fanproduction.gui.controller;

import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.configurator.CardFormConfigurator;
import com.fanproduction.gui.dto.metadata.FieldMetadataDto;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ComponentDto;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.factory.FieldControlFactory;
import com.fanproduction.gui.service.FieldMetadataService;
import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class CardFormController {

    private final Stage stage;
    private final String cardType;
    private final ProductCardDto existingCard;
    private final Runnable onSaveCallback;
    private final FieldMetadataService metadataService = new FieldMetadataService();
    private final FieldControlFactory controlFactory;
    // Контроллеры вкладок
    private ProductComponentsController componentsTabController;
    private ProductMaterialsController materialsTabController;
    // Для временных карточек
    private final Map<String, Long> temporaryCardIds = new HashMap<>();
    private Long currentTemporaryCardId;
    private CompletableFuture<Long> temporaryCardFuture = null;

    private final Map<String, Node> fieldControls = new HashMap<>();
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
        this.controlFactory = new FieldControlFactory(this.stage, this::autoFillFromSelection);

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

        // 1. СОЗДАЁМ TabPane
        TabPane tabPane = new TabPane();
        tabPane.setPrefHeight(500);

        // --- Вкладка 1: Основные поля ---
        Tab mainTab = new Tab("Основные поля");
        mainTab.setClosable(false);
        GridPane formGrid = createFormGrid();
        ScrollPane scrollPane = new ScrollPane(formGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(450);
        mainTab.setContent(scrollPane);
        tabPane.getTabs().add(mainTab);

        // --- Вкладка 2: Компоненты ---
        Tab componentsTab = createComponentsTab();
        tabPane.getTabs().add(componentsTab);

        // --- Вкладка 3: Материалы ---
        Tab materialsTab = createMaterialsTab();
        tabPane.getTabs().add(materialsTab);

        // 2. КНОПКИ
        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        // Удаление временной карточки при отмене
        cancelButton.setOnAction(e -> {
            deleteTemporaryCard();
            stage.close();
        });

        // Удаление временной карточки при закрытии окна
        stage.setOnCloseRequest(event -> deleteTemporaryCard());

        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> saveCard(saveButton));

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton);

        // 3. СБОРКА ГЛАВНОГО ЛЕЙАУТА
        mainLayout.getChildren().addAll(titleLabel, tabPane, buttonBox);

        Scene scene = new Scene(mainLayout, 900, 700);
        stage.setScene(scene);

        // 4. СОЗДАЁМ ВРЕМЕННУЮ КАРТОЧКУ (ТОЛЬКО ДЛЯ НОВОЙ)
        if (existingCard == null && getTemporaryCardId() == null) {
            createTemporaryCardAsync();
            waitForTemporaryCardAndOpenTabs();
        } else if (existingCard == null && getTemporaryCardId() != null) {
            currentTemporaryCardId = getTemporaryCardId();
            refreshTemporaryCardInTabs(currentTemporaryCardId);
        }

        // ========== 5. ЗАГРУЖАЕМ ДОПОЛНИТЕЛЬНЫЕ ДАННЫЕ ДЛЯ РЕДАКТИРОВАНИЯ ==========
        loadAdditionalData();
    }

    private GridPane createFormGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));

        // Устанавливаем процентную ширину колонок
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        col1.setHgrow(Priority.NEVER);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        col2.setHgrow(Priority.ALWAYS);

        grid.getColumnConstraints().addAll(col1, col2);

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
            if ("separator".equals(field.getType())) {
                Separator separator = new Separator();
                separator.setPadding(new Insets(10, 0, 5, 0));
                grid.add(separator, 0, row, 2, 1);
                row++;

                Label titleLabel = new Label(field.getLabel());
                titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #555; -fx-font-size: 12px;");
                grid.add(titleLabel, 0, row, 2, 1);
                row++;
                continue;
            }

            Label label = new Label(field.getLabel() + (field.isRequired() ? " *" : ":"));
            label.setStyle("-fx-font-weight: bold;");

            Node control = createControlForField(field,
                    existingCard != null ? existingCard.getFields().get(field.getName()) : null);

            // Контейнер для поля и подсказки
            VBox fieldContainer = new VBox(2);
            fieldContainer.getChildren().add(control);

            if (field.getHint() != null && !field.getHint().isEmpty()) {
                Label hintLabel = new Label(field.getHint());
                hintLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888;");
                fieldContainer.getChildren().add(hintLabel);
                fieldHints.put(field.getName(), hintLabel);
            }

            grid.add(label, 0, row);
            grid.add(fieldContainer, 1, row);
            fieldControls.put(field.getName(), control);
            fieldMetadata.put(field.getName(), field);
            fieldLabels.put(field.getName(), label);

            boolean isVisible = field.isVisible();
            label.setVisible(isVisible);
            label.setManaged(isVisible);
            fieldContainer.setVisible(isVisible);
            fieldContainer.setManaged(isVisible);

            row++;
        }

        CardFormConfigurator.configure(cardType, fieldControls, fieldLabels, fieldHints, existingCard != null);
        return grid;
    }

    private Node createControlForField(FieldMetadataDto field, Object existingValue) {
        return controlFactory.createControl(field, existingValue, fieldControls);
    }

    private void autoFillFromSelection(String referenceType, Long selectedId) {
        new Thread(() -> {
            try {
                if ("COMPONENT".equals(referenceType)) {
                    // Загружаем компонент по ID
                    ComponentDto component = ComponentClient.getComponentById(selectedId);
                    Platform.runLater(() -> {
                        setFieldValue("hubName", component.getName());
                        setFieldValue("hubComponentId", selectedId);
                        addComponentToProduct(selectedId, 1.0, "Ступица");
                        updateFullMarking();
                    });
                } else {
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
                                    setFieldValue("motorWheelFullMarking", fullMarking);
                                    break;
                                case "RADIAL_WHEEL":
                                    setFieldValue("radialWheelFullMarking", fullMarking);
                                    setFieldValue("wheelSize", fields.get("size"));
                                    setFieldValue("poles", fields.get("poles"));

                                    // Добавляем компонент ступицы
                                    Object hubComponentId = fields.get("hubComponentId");
                                    if (hubComponentId instanceof Number) {
                                        Long componentId = ((Number) hubComponentId).longValue();
                                        addComponentToProduct(componentId, 1.0, "Ступица колеса");
                                    }
                                    break;
                                case "AXIAL_WHEEL":
                                    // Пока нет полей для автозаполнения
                                    break;
                                case "MOTOR":
                                    setFieldValue("poles", fields.get("poles"));
                                    setFieldValue("voltage", fields.get("voltage"));
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
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void setFieldValue(String fieldName, Object value) {
        Node control = fieldControls.get(fieldName);
        if (control instanceof TextField && value != null) {
            ((TextField) control).setText(value.toString());
        }
    }

    private void updateFullMarking() {
        // Полная маркировка обновится через слушатели в конфигураторе
    }

    private Object getControlValue(Node control, String fieldName, FieldMetadataDto metadata) {
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
        } else if (control instanceof HBox container) {
            if (container.getChildren().size() >= 2 && container.getChildren().get(1) instanceof Label label) {
                Object userData = label.getUserData();
                if (userData instanceof Number) {
                    return ((Number) userData).longValue();
                }
            }
        }
        return null;
    }

    private void saveCard(Button saveButton) {
        Map<String, Object> fields = new HashMap<>();

        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String fieldName = entry.getKey();
            Node control = entry.getValue();
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
        Node nameControl = fieldControls.get("name");
        if (nameControl instanceof TextField) {
            name = ((TextField) nameControl).getText().trim();
        }

        final String finalName = name;
        final Map<String, Object> finalFields = fields;
        final Long finalId = existingCard != null ? existingCard.getId() : null;
        final boolean isNewCard = (existingCard == null);
        final Long existingCardId = getCurrentCardId();

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
                if (isNewCard && existingCardId != null) {
                    // Обновляем временную карточку
                    response = ProductCardClient.updateCard(existingCardId, finalName, finalFields);
                    // Снимаем флаг временной
                    if (response.isSuccess()) {
                        removeTemporaryFlag(existingCardId);
                    }
                } else if (isNewCard) {
                    response = ProductCardClient.createCard(cardType, finalName, finalFields);
                } else {
                    response = ProductCardClient.updateCard(finalId, finalName, finalFields);
                }

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        removeTemporaryCardId();
                        showAlert("Успешно", "Карточка " + (isNewCard ? "создана" : "обновлена"),
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
        try {
            return CardTemplateType.valueOf(cardType).getDisplayName();
        } catch (IllegalArgumentException e) {
            return cardType;
        }
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
        return createTab("/com/fanproduction/gui/view/ProductComponentsView.fxml",
                "Компоненты",
                ProductComponentsController.class);
    }

    private Tab createMaterialsTab() {
        return createTab("/com/fanproduction/gui/view/ProductMaterialsView.fxml",
                "Материалы",
                ProductMaterialsController.class);
    }

    /**
     * Универсальный метод для создания вкладки компонентов или материалов
     * @param fxmlPath путь к FXML файлу
     * @param tabTitle название вкладки
     * @param controllerClass класс контроллера
     * @return настроенная вкладка
     */
    private <T> Tab createTab(String fxmlPath, String tabTitle, Class<T> controllerClass) {
        Tab tab = new Tab(tabTitle);
        tab.setClosable(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            T controller = loader.getController();

            // Сохраняем ссылку на контроллер в зависимости от типа
            if (controller instanceof ProductComponentsController) {
                this.componentsTabController = (ProductComponentsController) controller;
            } else if (controller instanceof ProductMaterialsController) {
                this.materialsTabController = (ProductMaterialsController) controller;
            }

            Long cardId = getCurrentCardId();

            if (cardId != null) {
                // Вызываем метод refresh в зависимости от типа
                if (controller instanceof ProductComponentsController) {
                    ((ProductComponentsController) controller).refresh(cardId);
                    ((ProductComponentsController) controller).enableControls();
                } else if (controller instanceof ProductMaterialsController) {
                    ((ProductMaterialsController) controller).refresh(cardId);
                    ((ProductMaterialsController) controller).enableControls();
                }
            } else {
                // Показываем сообщение о загрузке
                if (controller instanceof ProductComponentsController) {
                    ((ProductComponentsController) controller).showLoadingMessage();
                } else if (controller instanceof ProductMaterialsController) {
                    ((ProductMaterialsController) controller).showLoadingMessage();
                }
            }

            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20px;");
            errorBox.getChildren().add(new Label("Ошибка загрузки " + tabTitle + ": " + e.getMessage()));
            tab.setContent(errorBox);
        }

        return tab;
    }

    private void removeTemporaryFlag(Long cardId) {
        new Thread(() -> {
            try {
                Map<String, Boolean> request = new HashMap<>();
                request.put("isTemporary", false);
                ApiClient.put("/products/" + cardId + "/temporary", request,
                        new TypeReference<ApiResponse<Void>>() {});
                System.out.println("Temporary flag removed for card: " + cardId);
            } catch (Exception e) {
                System.err.println("Failed to remove temporary flag: " + e.getMessage());
            }
        }).start();
    }

    private void deleteTemporaryCard() {
        Long tempId = getTemporaryCardId();
        if (tempId != null) {
            try {
                ProductCardClient.deleteCardWithCheck(tempId);
                System.out.println("Temporary card deleted: " + cardType + ": " + tempId);
                removeTemporaryCardId();
            } catch (Exception e) {
                System.err.println("Failed to delete temporary card: " + e.getMessage());
            }
        }
    }

    private Long getCurrentCardId() {
        if (existingCard != null && existingCard.getId() != null) {
            return existingCard.getId();
        }
        return getTemporaryCardId();
    }

    private Long getTemporaryCardId() {
        return temporaryCardIds.get(cardType);
    }

    private void setTemporaryCardId(Long id) {
        temporaryCardIds.put(cardType, id);
        this.currentTemporaryCardId = id;
        System.out.println("Temporary card ID set for " + cardType + ": " + id);
    }

    private void removeTemporaryCardId() {
        temporaryCardIds.remove(cardType);
        this.currentTemporaryCardId = null;
    }

    private void refreshTemporaryCardInTabs(Long tempId) {
        if (componentsTabController != null) {
            componentsTabController.refresh(tempId);
            componentsTabController.enableControls();
        }
        if (materialsTabController != null) {
            materialsTabController.refresh(tempId);
            materialsTabController.enableControls();
        }
    }

    /**
     * Асинхронное создание временной карточки с обновлением UI
     */
    private void createTemporaryCardAsync() {
        // Если уже есть будущий результат и он ещё не завершён — не создаём повторно
        if (temporaryCardFuture != null && !temporaryCardFuture.isDone()) {
            System.out.println("Temporary card creation already in progress, waiting...");
            return;
        }

        // Создаём CompletableFuture для асинхронного создания карточки
        temporaryCardFuture = CompletableFuture.supplyAsync(() -> {
            try {
                ApiResponse<ProductCardDto> response = ProductCardClient.createTemporaryCard(cardType, "system");
                if (response.isSuccess() && response.getData() != null) {
                    Long newTempId = response.getData().getId();
                    setTemporaryCardId(newTempId);
                    System.out.println("Temporary card created for " + cardType + " with ID: " + newTempId);
                    return newTempId;
                } else {
                    System.err.println("Failed to create temporary card: " + response.getMessage());
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });

        // После завершения создания — обновляем UI
        temporaryCardFuture.thenAccept(tempId -> Platform.runLater(() -> {
            if (tempId != null) {
                currentTemporaryCardId = tempId;

                // Обновляем вкладку компонентов
                if (componentsTabController != null) {
                    componentsTabController.refresh(currentTemporaryCardId);
                    componentsTabController.enableControls();
                }

                // Обновляем вкладку материалов
                if (materialsTabController != null) {
                    materialsTabController.refresh(currentTemporaryCardId);
                    materialsTabController.enableControls();
                }
            } else {
                showAlert("Ошибка", "Не удалось создать временную карточку", Alert.AlertType.ERROR);
            }
        }));
    }

    private void waitForTemporaryCardAndOpenTabs() {
        if (temporaryCardFuture == null) {
            createTemporaryCardAsync();
            refreshTemporaryCardInTabs(currentTemporaryCardId);
        }

        temporaryCardFuture.thenAccept(tempId -> Platform.runLater(() -> {
            if (tempId != null) {
                currentTemporaryCardId = tempId;
                refreshTemporaryCardInTabs(tempId);
            }
        }));
    }

    /**
     * Добавляет компонент в карточку продукции (во вкладку "Компоненты")
     * @param componentId ID компонента
     * @param quantity количество
     * @param role роль компонента (для колонки "Место установки")
     */
    private void addComponentToProduct(Long componentId, Double quantity, String role) {
        Long cardId = getCurrentCardId();
        if (cardId == null) {
            System.out.println("Cannot add component: cardId is null");
            return;
        }

        new Thread(() -> {
            try {
                Map<String, Object> request = new HashMap<>();
                request.put("componentId", componentId);
                request.put("quantity", quantity != null ? quantity : 1.0);
                request.put("position", role);  // "Место установки" или "Роль в изделии"

                TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
                ApiResponse<Map<String, Object>> response = ApiClient.post(
                        "/components/product/" + cardId, request, typeRef);

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        System.out.println("Component added to product: " + componentId + " as " + role);
                        // Обновляем вкладку компонентов
                        if (componentsTabController != null) {
                            componentsTabController.refresh(cardId);
                        }
                    } else {
                        System.err.println("Failed to add component: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> System.err.println("Error adding component: " + e.getMessage()));
            }
        }).start();
    }

    /**
     * Загружает имя компонента по ID и сохраняет в скрытое поле hubName
     */
    private void loadComponentName(Long componentId) {
        new Thread(() -> {
            try {
                ComponentDto component = ComponentClient.getComponentById(componentId);
                Platform.runLater(() -> setFieldValue("hubName", component.getName()));
            } catch (Exception e) {
                System.err.println("Failed to load component name for ID: " + componentId);
                e.printStackTrace();
                Platform.runLater(() -> setFieldValue("hubName", "Компонент #" + componentId));
            }
        }).start();
    }

    /**
     * Загружает дополнительные данные для редактирования карточки
     * (например, имя компонента ступицы для радиального колеса)
     */
    private void loadAdditionalData() {
        // Только для редактирования существующей карточки
        if (existingCard == null) return;

        String cardType = existingCard.getCardType();
        Map<String, Object> fields = existingCard.getFields();

        // Для радиального колеса: загружаем имя ступицы
        if ("RADIAL_WHEEL".equals(cardType)) {
            Object hubComponentId = fields.get("hubComponentId");
            if (hubComponentId instanceof Number) {
                Long componentId = ((Number) hubComponentId).longValue();
                loadComponentName(componentId);
            }
        }
    }
}
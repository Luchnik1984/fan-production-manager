package com.fanproduction.gui.controller;

import com.fanproduction.core.enums.CardTemplateType;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.client.ComponentClient;
import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.configurator.CardFieldConfigurator;
import com.fanproduction.gui.configurator.CardFormConfigurator;
import com.fanproduction.gui.dto.ProductComponentItemDto;
import com.fanproduction.gui.dto.config.SelectableFieldConfig;
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

    private final List<ComponentChange> pendingComponentChanges = new ArrayList<>();

    public CardFormController(Stage owner, String cardType, ProductCardDto existingCard, Runnable onSaveCallback) {
        this.stage = new Stage();
        this.stage.initModality(Modality.WINDOW_MODAL);
        this.stage.initOwner(owner);
        this.cardType = cardType;
        this.existingCard = existingCard;
        this.onSaveCallback = onSaveCallback;
        this.controlFactory = new FieldControlFactory(
                this.stage,
                this::autoFillFromSelection);

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

        // ЗАГРУЖАЕМ ДАННЫЕ В ФОРМУ (С ФЛАГОМ SILENT)
        loadExistingCardData();

        // 2. КНОПКИ
        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");
        Button refreshMarkingButton = new Button("🔄 Восстановить маркировку");

        // ========== ОТМЕНА ==========
        cancelButton.setOnAction(e -> {
            pendingComponentChanges.clear();
            deleteTemporaryCard();
            stage.close();
        });

        // ========== ЗАКРЫТИЕ ==========
        stage.setOnCloseRequest(event -> {
            pendingComponentChanges.clear();
            deleteTemporaryCard();
        });

        // Сохранение карточки
        saveButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveButton.setOnAction(e -> saveCard(saveButton));

        // Кнопка "Восстановить маркировку"
        refreshMarkingButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        refreshMarkingButton.setOnAction(e -> handleRefreshMarkingButton());

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(saveButton, cancelButton, refreshMarkingButton);

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

//    /**
//     * УВЕДОМЛЕНИЕ ОБ ИЗМЕНЕНИИ ПОЛЯ
//     */
//
//    private void notifyFieldChanged(String fieldName) {
//        CardFieldConfigurator configurator = CardFormConfigurator.getConfigurator(cardType);
//        if (configurator == null) return;
//
//        // ========== СПРАШИВАЕМ У КОНФИГУРАТОРА ==========
//        List<String> markingFields = configurator.getMarkingAffectingFields();
//        if (!markingFields.contains(fieldName)) return;
//
//        configurator.refreshFullMarking(fieldControls);
//    }

    /**
     * Устанавливает значение поля
     * @param fieldName имя поля
     * @param value значение
     * @param silent если true - не вызывать notifyFieldChanged (для инициализации)
     */
    private void setFieldValue(String fieldName, Object value, boolean silent) {
        Node control = fieldControls.get(fieldName);
        if (control == null) return;

        if (control instanceof TextField textField) {
            String textValue = value != null ? value.toString() : "";
            if (!textField.getText().equals(textValue)) {
                textField.setText(textValue);
            }
        } else if (control instanceof Label) {
            String textValue = value != null ? value.toString() : "";
            ((Label) control).setText(textValue);
        }
    }

    /**
    * СТАРЫЙ МЕТОД setFieldValue (для обратной совместимости)
    */
    private void setFieldValue(String fieldName, Object value) {
        setFieldValue(fieldName, value, false);
    }

    /**
     * МЕТОД ДЛЯ ЗАГРУЗКИ ДАННЫХ В ФОРМУ (С ФЛАГОМ SILENT)
     * Заполняет форму данными из существующей карточки.
     * Вызывается только при редактировании
     */
    private void loadExistingCardData() {
        if (existingCard == null) return;

        Map<String, Object> fields = existingCard.getFields();
        if (fields == null) return;

        for (Map.Entry<String, Object> entry : fields.entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;

            // Пропускаем служебные поля
            if ("id".equals(fieldName) || "code".equals(fieldName) ||
                    "cardType".equals(fieldName) || "createdAt".equals(fieldName) ||
                    "updatedAt".equals(fieldName) || "createdBy".equals(fieldName)) {
                continue;
            }

            // Устанавливаем с silent = true
            setFieldValue(fieldName, value, true);
        }
    }

    /**
     *  Метод обновления FullMarking через кнопку.
     */
    private void handleRefreshMarkingButton() {
        CardFieldConfigurator configurator = CardFormConfigurator.getConfigurator(cardType);
        if (configurator == null) {
            showAlert("Внимание", "Для этого типа карточки нет конфигуратора", Alert.AlertType.WARNING);
            return;
        }

        try {

//            // 1. Очищаем поле
//            Node control = fieldControls.get("fullMarking");
//            if (control instanceof TextField) {
//                ((TextField) control).setText("");
//            }

            // 2. Принудительно обновляем
            configurator.forceSetFullMarking(fieldControls);

            showAlert("Успешно", "Полная маркировка восстановлена до автоматического значения", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Ошибка", "Не удалось восстановить маркировку: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
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
        // СБОР ДАННЫХ ИЗ ФОРМЫ
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

        // ПОЛУЧАЕМ НАИМЕНОВАНИЕ
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

        // ВАЛИДАЦИЯ
        if (finalName.isEmpty()) {
            showAlert("Ошибка", "Наименование обязательно для заполнения", Alert.AlertType.ERROR);
            return;
        }

        // Проверяем обязательные поля (только видимые)
        for (Map.Entry<String, FieldMetadataDto> entry : fieldMetadata.entrySet()) {
            FieldMetadataDto field = entry.getValue();
            if (field.isRequired()) {
                Node control = fieldControls.get(field.getName());
                if (control != null && !control.isVisible()) {
                    continue;
                }
                Object value = finalFields.get(field.getName());
                if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                    showAlert("Ошибка", "Поле '" + field.getLabel() + "' обязательно для заполнения", Alert.AlertType.ERROR);
                    return;
                }
            }
        }

        // Получаем конфигуратор для данного типа карточки
        CardFieldConfigurator configurator = CardFormConfigurator.getConfigurator(cardType);
        if (configurator != null) {
            // Передаём fieldControls, finalFields, fieldLabels
            if (!configurator.validate(fieldControls, finalFields, fieldLabels)) {
                return; // Ошибка уже показана в конфигураторе
            }
        }

        // СОХРАНЕНИЕ
        saveButton.setDisable(true);
        saveButton.setText("Сохранение...");

        new Thread(() -> {
            try {
                ApiResponse<ProductCardDto> response;
                if (isNewCard && existingCardId != null) {
                    // Обновляем временную карточку
                    response = ProductCardClient.updateCard(existingCardId, finalName, finalFields);
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
                        // Получаем ID карточки из ответа
                        Long cardId = response.getData().getId();
                        // Применяем отложенные изменения компонентов
                        applyComponentChanges(cardId);

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
                pendingComponentChanges.clear();
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

    private void addComponentToProduct(Long componentId, Double quantity, String role) {
        Long cardId = getCurrentCardId();
        if (cardId == null) {
            System.out.println("Cannot add component: cardId is null");
            return;
        }

        // Создаём временный объект для UI
        ProductComponentItemDto newItem = new ProductComponentItemDto();
        newItem.setComponentId(componentId);
        newItem.setQuantity(quantity != null ? quantity : 1.0);
        newItem.setPosition(role);
        newItem.setName("Загрузка..."); // временное имя

        // Добавляем в UI
        componentsTabController.addItem(newItem);

        // Добавляем в список изменений
        pendingComponentChanges.add(new ComponentChange(
                ComponentChange.Type.ADD,
                componentId,
                quantity,
                role
        ));

        // Асинхронно загружаем реальное имя компонента и обновляем элемент
        loadComponentNameAndUpdate(componentId, newItem);

        System.out.println("Marked for addition: component " + componentId + " with role: " + role);
    }

    private void loadComponentNameAndUpdate(Long componentId, ProductComponentItemDto item) {
        new Thread(() -> {
            try {
                ComponentDto component = ComponentClient.getComponentById(componentId);
                String designation = component.getDesignation() != null && !component.getDesignation().isEmpty()
                        ? component.getDesignation()
                        : component.getName();
                Platform.runLater(() -> {
                    item.setName(designation);
                    item.setVendorCode(component.getVendorCode());
                    item.setUnitCode(component.getUnitCode());
                    item.setClassName(component.getClassName());
                    item.setDescription(component.getDescription());
                    componentsTabController.refreshItem(item);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    item.setName("Ошибка загрузки");
                    componentsTabController.refreshItem(item);
                });
            }
        }).start();
    }

    /**
     * Загружает дополнительные данные для редактирования карточки
     * (например, имя компонента ступицы для радиального колеса)
     */
    private void loadAdditionalData() {
        if (existingCard == null) return;

        String cardType = existingCard.getCardType();
        Map<String, Object> fields = existingCard.getFields();

        if ("RADIAL_WHEEL".equals(cardType)) {
            Object hubComponentId = fields.get("hubComponentId");
            if (hubComponentId instanceof Number) {
                Long componentId = ((Number) hubComponentId).longValue();
                loadComponentDesignation(componentId);  // ← посмотри, что здесь!
            }
        }
    }

    private void removeComponentFromProductByRole(String role) {
        if (role == null || role.isEmpty()) return;

        Long cardId = getCurrentCardId();
        if (cardId == null) return;

        if (componentsTabController == null) return;

        // Ищем компонент с такой ролью
        ProductComponentItemDto itemToRemove = null;
        for (ProductComponentItemDto item : componentsTabController.getItems()) {
            if (role.equals(item.getPosition())) {
                itemToRemove = item;
                break;
            }
        }

        if (itemToRemove == null) return;

        // Удаляем через API
        try {
            // Удаляем из локального списка
            componentsTabController.removeItem(itemToRemove);
            System.out.println("Removed old component: " + itemToRemove.getName() + " with role: " + role);
            // Добавляем в список изменений
            pendingComponentChanges.add(new ComponentChange(
                    ComponentChange.Type.REMOVE,
                    itemToRemove.getComponentId(),
                    null,
                    role
            ));
        } catch (Exception e) {
            System.err.println("Failed to remove old component: " + e.getMessage());
        }
    }

    private void applyComponentChanges(Long cardId) {
        if (pendingComponentChanges.isEmpty()) return;

        // Копируем список и очищаем его
        List<ComponentChange> changes = new ArrayList<>(pendingComponentChanges);
        pendingComponentChanges.clear();

        // Сортируем: сначала удаления, потом добавления
        changes.sort((a, b) -> {
            if (a.type == ComponentChange.Type.REMOVE && b.type == ComponentChange.Type.ADD) return -1;
            if (a.type == ComponentChange.Type.ADD && b.type == ComponentChange.Type.REMOVE) return 1;
            return 0;
        });

        for (ComponentChange change : changes) {
            try {
                if (change.type == ComponentChange.Type.REMOVE) {
                    // Удаление
                    TypeReference<ApiResponse<Void>> typeRef = new TypeReference<>() {};
                    ApiClient.delete("/components/product/" + cardId + "/" + change.componentId, typeRef);
                    System.out.println("Applied removal of component " + change.componentId);
                } else if (change.type == ComponentChange.Type.ADD) {
                    // Добавление
                    Map<String, Object> request = new HashMap<>();
                    request.put("componentId", change.componentId);
                    request.put("quantity", change.quantity != null ? change.quantity : 1.0);
                    request.put("position", change.role);
                    TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
                    ApiClient.post("/components/product/" + cardId, request, typeRef);
                    System.out.println("Applied addition of component " + change.componentId);
                }
            } catch (Exception e) {
                System.err.println("Failed to apply component change: " + e.getMessage());
            }
        }
    }

    private void loadComponentDesignation(Long componentId) {
        new Thread(() -> {
            try {
                ComponentDto component = ComponentClient.getComponentById(componentId);
                Platform.runLater(() -> {
                    if (component != null) {
                        String designation = component.getDesignation();
                        if (designation == null || designation.isEmpty()) {
                            designation = component.getName();
                        }
                        setFieldValue("hubName", designation);
                    }
                });
            } catch (Exception e) {
                System.err.println("Failed to load component designation: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Использует setFieldValue с silent=false
     */
    private void autoFillFromSelection(SelectableFieldConfig config, Long selectedId) {
        new Thread(() -> {
            try {
                if (selectedId == null || config == null) return;

                ComponentDto component = ComponentClient.getComponentById(selectedId);
                String designation = component.getDesignation() != null && !component.getDesignation().isEmpty()
                        ? component.getDesignation()
                        : component.getName();

                Platform.runLater(() -> {
                    // ========== 1. УДАЛЯЕМ СТАРЫЙ КОМПОНЕНТ С ТАКОЙ ЖЕ РОЛЬЮ ==========
                    if (config.addToProduct()) {
                        removeComponentFromProductByRole(config.role());
                    }

                    // ========== 2. ЗАПОЛНЯЕМ ТАРГЕТ ПОЛЕ (НЕ silent, нужно обновить маркировку) ==========
                    String targetFieldName = config.targetFieldName();
                    if (targetFieldName != null && !targetFieldName.isEmpty()) {
                        setFieldValue(targetFieldName, designation,true); // ← silent = true
                    }

                    // ========== 3. СОХРАНЯЕМ ID В ПОЛЕ (НЕ silent) ==========
                    setFieldValue(config.fieldName(), selectedId,true); // ← silent = true

                    // ========== 4. ДОБАВЛЯЕМ КОМПОНЕНТ В ПРОДУКТ ==========
                    if (config.addToProduct()) {
                        String role = config.role() != null ? config.role() : "Компонент";
                        addComponentToProduct(selectedId, 1.0, role);
                    }

                    // ========== 5.  ОБНОВЛЯЕМ МАРКИРОВКУ ==========
                    // Вызываем refreshFullMarking через конфигуратор
                    CardFieldConfigurator configurator = CardFormConfigurator.getConfigurator(cardType);
                    if (configurator != null) {
                        configurator.refreshFullMarking(fieldControls);
                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert("Ошибка", "Не удалось загрузить компонент: " + e.getMessage(),
                        Alert.AlertType.ERROR));
            }
        }).start();
    }

    private static class ComponentChange {
        enum Type { ADD, REMOVE }
        Type type;
        Long componentId;
        Double quantity;
        String role;

        ComponentChange(Type type, Long componentId, Double quantity, String role) {
            this.type = type;
            this.componentId = componentId;
            this.quantity = quantity;
            this.role = role;
        }
    }

}
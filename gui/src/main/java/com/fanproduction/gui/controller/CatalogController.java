package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.ProductCardDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для вкладки "Каталог продукции".
 */
public class CatalogController {

    @FXML
    private ComboBox<String> typeFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<ProductCardDto> productsTable;

    @FXML
    private TableColumn<ProductCardDto, Long> idColumn;

    @FXML
    private TableColumn<ProductCardDto, String> nameColumn;

    @FXML
    private TableColumn<ProductCardDto, String> codeColumn;

    @FXML
    private TableColumn<ProductCardDto, String> typeColumn;

    @FXML
    private TableColumn<ProductCardDto, String> createdAtColumn;

    @FXML
    private TableColumn<ProductCardDto, String> createdByColumn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label pageLabel;

    @FXML
    private Button prevButton;

    @FXML
    private Button nextButton;

    @FXML
    private Button createButton;

    @FXML
    private Button editButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button refreshButton;

    private final ObservableList<ProductCardDto> productList = FXCollections.observableArrayList();
    private int currentPage = 0;
    private int totalPages = 0;
    private int totalElements = 0;
    private static final int PAGE_SIZE = 20;
    private String currentTypeFilter = null;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    // Соответствие типов для отображения
    private static final Map<String, String> TYPE_DISPLAY_MAP = Map.of(
            "MOTOR", "Электродвигатель",
            "MOTOR_WHEEL", "Мотор-колесо",
            "RADIAL_WHEEL", "Радиальное колесо",
            "AXIAL_FAN", "Осевой вентилятор",
            "RADIAL_FAN", "Радиальный вентилятор",
            "DUCT_FAN", "Канальный вентилятор",
            "CUP", "Стакан",
            "ACCESSORY", "Комплектующее"
    );

    @FXML
    private void initialize() {
        setupFilters();
        setupTable();
        loadProducts();
    }

    private void setupFilters() {
        // Добавляем типы для фильтрации
        typeFilterComboBox.getItems().add("Все типы");
        typeFilterComboBox.getItems().addAll(TYPE_DISPLAY_MAP.values());
        typeFilterComboBox.setValue("Все типы");

        typeFilterComboBox.valueProperty().addListener((obs, old, newVal) -> {
            if ("Все типы".equals(newVal)) {
                currentTypeFilter = null;
            } else {
                // Находим соответствующий enum
                for (Map.Entry<String, String> entry : TYPE_DISPLAY_MAP.entrySet()) {
                    if (entry.getValue().equals(newVal)) {
                        currentTypeFilter = entry.getKey();
                        break;
                    }
                }
            }
            handleSearch();
        });

        searchField.setOnAction(e -> handleSearch());
    }

    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        codeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCode() != null ? cellData.getValue().getCode() : "—"));
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                TYPE_DISPLAY_MAP.getOrDefault(cellData.getValue().getCardType(), cellData.getValue().getCardType())));
        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });
        createdByColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCreatedBy() != null ? cellData.getValue().getCreatedBy() : ""));

        productsTable.setItems(productList);

        // Обработка двойного клика для редактирования
        productsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleEdit();
            }
        });
    }

    private void loadProducts() {
        statusLabel.setText("Загрузка...");

        new Thread(() -> {
            try {
                ApiResponse<Map<String, Object>> response = ProductCardClient.getAllCards(
                        currentPage, PAGE_SIZE, currentTypeFilter);

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        Map<String, Object> data = response.getData();

                        // Извлекаем список карточек
                        @SuppressWarnings("unchecked")
                        List<ProductCardDto> content = (List<ProductCardDto>) data.get("content");

                        productList.clear();
                        productList.addAll(content);

                        // Извлекаем информацию о пагинации
                        totalPages = ((Number) data.get("totalPages")).intValue();
                        totalElements = ((Number) data.get("totalElements")).intValue();

                        updatePaginationControls();
                        statusLabel.setText("Всего записей: " + totalElements);
                    } else {
                        statusLabel.setText("Ошибка загрузки: " + response.getMessage());
                        showAlert("Ошибка", "Не удалось загрузить каталог: " + response.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка: " + e.getMessage());
                    showAlert("Ошибка", "Не удалось загрузить каталог: " + e.getMessage(), Alert.AlertType.ERROR);
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void updatePaginationControls() {
        pageLabel.setText("Страница " + (currentPage + 1) + " из " + Math.max(1, totalPages));
        prevButton.setDisable(currentPage == 0);
        nextButton.setDisable(currentPage >= totalPages - 1);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            // Поиск
            new Thread(() -> {
                try {
                    ApiResponse<List<ProductCardDto>> response = ProductCardClient.searchCards(query);
                    Platform.runLater(() -> {
                        if (response.isSuccess() && response.getData() != null) {
                            productList.clear();
                            productList.addAll(response.getData());
                            statusLabel.setText("Найдено: " + productList.size());
                            // Отключаем пагинацию при поиске
                            prevButton.setDisable(true);
                            nextButton.setDisable(true);
                            pageLabel.setText("Результаты поиска");
                        } else {
                            statusLabel.setText("Ничего не найдено");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("Ошибка поиска: " + e.getMessage()));
                }
            }).start();
        } else {
            // Сброс поиска - загружаем все с пагинацией
            currentPage = 0;
            loadProducts();
        }
    }

    @FXML
    private void handleRefresh() {
        currentPage = 0;
        searchField.clear();
        loadProducts();
    }

    @FXML
    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            loadProducts();
        }
    }

    @FXML
    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            loadProducts();
        }
    }

    @FXML
    private void handleCreate() {
        // Получаем Stage из текущего окна
        Stage ownerStage = (Stage) productsTable.getScene().getWindow();

        // Диалог выбора типа карточки
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("AXIAL_FAN",
                "MOTOR", "MOTOR_WHEEL", "RADIAL_WHEEL", "AXIAL_FAN", "RADIAL_FAN", "DUCT_FAN", "CUP", "ACCESSORY");
        typeDialog.setTitle("Создание карточки");
        typeDialog.setHeaderText("Выберите тип создаваемой карточки");
        typeDialog.setContentText("Тип продукции:");

        typeDialog.showAndWait().ifPresent(cardType -> {
            CardFormController form = new CardFormController(
                    ownerStage,
                    cardType,
                    null,
                    this::handleRefresh
            );
            form.show();
        });
    }

    @FXML
    private void handleEdit() {
        ProductCardDto selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите карточку для редактирования", Alert.AlertType.WARNING);
            return;
        }

        Stage ownerStage = (Stage) productsTable.getScene().getWindow();

        CardFormController form = new CardFormController(
                ownerStage,
                selected.getCardType(),
                selected,
                this::handleRefresh
        );
        form.show();
    }

    @FXML
    private void handleDelete() {
        ProductCardDto selected = productsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Внимание", "Выберите карточку для удаления", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение удаления");
        confirmAlert.setHeaderText("Удаление карточки");
        confirmAlert.setContentText("Вы уверены, что хотите удалить карточку \"" + selected.getName() + "\"?\nЭто действие необратимо.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    ApiResponse<Void> response = ProductCardClient.deleteCard(selected.getId());
                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            showAlert("Успешно", "Карточка удалена", Alert.AlertType.INFORMATION);
                            handleRefresh();
                        } else {
                            showAlert("Ошибка", "Не удалось удалить карточку: " + response.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Ошибка", "Ошибка удаления: " + e.getMessage(), Alert.AlertType.ERROR));
                }
            }).start();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

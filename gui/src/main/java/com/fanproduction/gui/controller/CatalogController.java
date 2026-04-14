package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ProductCardClient;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.service.TableColumnConfigurator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class CatalogController {

    @FXML
    private ComboBox<String> typeFilterComboBox;

    @FXML
    private TextField searchField;

    @FXML
    private TableView<ProductCardDto> productsTable;

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

    private static final Map<String, String> TYPE_DISPLAY_MAP = Map.of(
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

    @FXML
    private void initialize() {
        setupFilters();
        loadProducts();
    }

    private void setupFilters() {
        typeFilterComboBox.getItems().add("Все типы");
        typeFilterComboBox.getItems().addAll(TYPE_DISPLAY_MAP.values());
        typeFilterComboBox.setValue("Все типы");

        typeFilterComboBox.valueProperty().addListener((obs, old, newVal) -> {
            if ("Все типы".equals(newVal)) {
                currentTypeFilter = null;
            } else {
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

    private void loadProducts() {
        statusLabel.setText("Загрузка...");

        new Thread(() -> {
            try {
                ApiResponse<Map<String, Object>> response = ProductCardClient.getAllCards(
                        currentPage, PAGE_SIZE, currentTypeFilter);

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        Map<String, Object> data = response.getData();

                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> content = (List<Map<String, Object>>) data.get("content");

                        productList.clear();
                        for (Map<String, Object> item : content) {
                            ProductCardDto dto = new ProductCardDto();
                            dto.setId(((Number) item.get("id")).longValue());
                            dto.setName((String) item.get("name"));
                            dto.setCode((String) item.get("code"));
                            dto.setCardType((String) item.get("cardType"));
                            dto.setCreatedAt(parseDateTime(item.get("createdAt")));
                            dto.setUpdatedAt(parseDateTime(item.get("updatedAt")));
                            dto.setCreatedBy((String) item.get("createdBy"));

                            @SuppressWarnings("unchecked")
                            Map<String, Object> fields = (Map<String, Object>) item.get("fields");
                            dto.setFields(fields);

                            String displayName = buildDisplayName(dto);
                            dto.setCardTypeDisplay(displayName);

                            productList.add(dto);
                        }

                        totalPages = ((Number) data.get("totalPages")).intValue();
                        totalElements = ((Number) data.get("totalElements")).intValue();

                        updatePaginationControls();
                        statusLabel.setText("Всего записей: " + totalElements);

                        TableColumnConfigurator.setupColumns(productsTable, currentTypeFilter);
                        productsTable.setItems(productList);

                        // Обработка двойного щелчка для просмотра карточки
                        productsTable.setOnMouseClicked(event -> {
                            if (event.getClickCount() == 2) {
                                ProductCardDto selected = productsTable.getSelectionModel().getSelectedItem();
                                if (selected != null) {
                                    Stage ownerStage = (Stage) productsTable.getScene().getWindow();
                                    CardViewController.show(ownerStage, selected);
                                }
                            }
                        });

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

    private String buildDisplayName(ProductCardDto dto) {
        String cardType = dto.getCardType();
        Map<String, Object> fields = dto.getFields();

        if ("MOTOR".equals(cardType) && fields != null) {
            String fullMarking = (String) fields.get("fullMarking");
            if (fullMarking != null && !fullMarking.isEmpty()) {
                return "Электродвигатель " + fullMarking;
            }
            return "Электродвигатель";
        }

        if ("MOTOR_WHEEL".equals(cardType)) {
            String name = dto.getName();
            if (name != null && !name.isEmpty()) {
                return "Мотор-колесо " + name;
            }
            return "Мотор-колесо";
        }

        if ("RADIAL_WHEEL".equals(cardType) && fields != null) {
            String fullMarking = (String) fields.get("fullMarking");
            if (fullMarking != null && !fullMarking.isEmpty()) {
//                return "Колесо радиальное " + fullMarking;
//            }
//            String marking = (String) fields.get("marking");
//            String bladeMod = (String) fields.get("bladeMod");
//            String hubType = (String) fields.get("hubType");
//
//            StringBuilder sb = new StringBuilder();
//            if (marking != null && !marking.isEmpty()) sb.append(marking);
//            if (bladeMod != null && !bladeMod.isEmpty()) sb.append("-").append(bladeMod);
//            if (hubType != null && !hubType.isEmpty()) sb.append("-").append(hubType);
//
//            if (!sb.isEmpty()) {
//                return "Колесо радиальное " + sb.toString();

                return fullMarking;
            }
            return "Колесо радиальное";
        }

        if ("AXIAL_WHEEL".equals(cardType) && fields != null) {
            String fullMarking = (String) fields.get("fullMarking");
            if (fullMarking != null && !fullMarking.isEmpty()) {
                return fullMarking;
            }
            return "Колесо осевое";
        }

        return dto.getName();
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
            final String lowerQuery = query.toLowerCase();

            new Thread(() -> {
                try {
                    ApiResponse<List<ProductCardDto>> response = ProductCardClient.searchCards(query);

                    Platform.runLater(() -> {
                        if (response.isSuccess() && response.getData() != null) {
                            List<ProductCardDto> filtered = new ArrayList<>();
                            for (ProductCardDto dto : response.getData()) {
                                if (matchesSearch(dto, lowerQuery)) {
                                    String displayName = buildDisplayName(dto);
                                    dto.setCardTypeDisplay(displayName);
                                    filtered.add(dto);
                                }
                            }
                            productList.clear();
                            productList.addAll(filtered);
                            statusLabel.setText("Найдено: " + productList.size());
                            prevButton.setDisable(true);
                            nextButton.setDisable(true);
                            pageLabel.setText("Результаты поиска");
                            TableColumnConfigurator.setupColumns(productsTable, null);
                            productsTable.setItems(productList);
                        } else {
                            statusLabel.setText("Ничего не найдено");
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> statusLabel.setText("Ошибка поиска: " + e.getMessage()));
                    e.printStackTrace();
                }
            }).start();
        } else {
            currentPage = 0;
            loadProducts();
        }
    }

    private boolean matchesSearch(ProductCardDto dto, String query) {
        if (query == null || query.isEmpty()) return true;
        if (dto.getName() != null && dto.getName().toLowerCase().contains(query)) return true;
        if (dto.getCode() != null && dto.getCode().toLowerCase().contains(query)) return true;
        Map<String, Object> fields = dto.getFields();
        if (fields != null) {
            for (Object value : fields.values()) {
                if (value != null && value.toString().toLowerCase().contains(query)) return true;
            }
        }
        return false;
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

    /**
     * Возвращает карту соответствия русских названий типов продукции и их кодов
     */
    private Map<String, String> getTypeMap() {
        Map<String, String> typeMap = new LinkedHashMap<>();
        typeMap.put("Электродвигатель", "MOTOR");
        typeMap.put("Мотор-колесо", "MOTOR_WHEEL");
        typeMap.put("Колесо радиальное", "RADIAL_WHEEL");
        typeMap.put("Колесо осевое", "AXIAL_WHEEL");
        typeMap.put("Вентилятор осевой", "AXIAL_FAN");
        typeMap.put("Вентилятор радиальный", "RADIAL_FAN");
        typeMap.put("Вентилятор канальный", "DUCT_FAN");
        typeMap.put("Стакан", "CUP");
        typeMap.put("Комплектующее", "ACCESSORY");
        return typeMap;
    }

    @FXML
    private void handleCreate() {
        Stage ownerStage = (Stage) productsTable.getScene().getWindow();

        Map<String, String> typeMap = getTypeMap();
        ChoiceDialog<String> typeDialog = new ChoiceDialog<>("Электродвигатель", typeMap.keySet());
        typeDialog.setTitle("Создание карточки");
        typeDialog.setHeaderText("Выберите тип создаваемой карточки");
        typeDialog.setContentText("Тип продукции:");

        typeDialog.showAndWait().ifPresent(russianType -> {
            String cardType = typeMap.get(russianType);
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

    private LocalDateTime parseDateTime(Object dateObj) {
        if (dateObj == null) return null;
        try {
            if (dateObj instanceof LocalDateTime) return (LocalDateTime) dateObj;
            String dateStr = dateObj.toString();
            if (dateStr.isEmpty()) return null;
            return LocalDateTime.parse(dateStr);
        } catch (Exception e) {
            return null;
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
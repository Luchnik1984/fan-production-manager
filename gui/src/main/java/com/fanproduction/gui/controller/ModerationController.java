package com.fanproduction.gui.controller;

import com.fanproduction.gui.dto.request.BlockRequest;
import com.fanproduction.gui.dto.request.RejectRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.UserDto;
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
import java.util.Optional;

public class ModerationController {

    @FXML
    private ComboBox<String> statusFilterComboBox;

    @FXML
    private TableView<UserDto> usersTable;

    @FXML
    private TableColumn<UserDto, Long> idColumn;

    @FXML
    private TableColumn<UserDto, String> emailColumn;

    @FXML
    private TableColumn<UserDto, String> nameColumn;

    @FXML
    private TableColumn<UserDto, String> phoneColumn;

    @FXML
    private TableColumn<UserDto, String> roleColumn;

    @FXML
    private TableColumn<UserDto, String> statusColumn;

    @FXML
    private TableColumn<UserDto, String> createdAtColumn;

    @FXML
    private TableColumn<UserDto, String> approvedByColumn;

    @FXML
    private TableColumn<UserDto, String> rejectionReasonColumn;

    @FXML
    private Button approveButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button blockButton;

    @FXML
    private Button unblockButton;

    @FXML
    private Label statusLabel;

    private Stage stage;
    private ObservableList<UserDto> userList = FXCollections.observableArrayList();
    private String currentStatusFilter = "PENDING";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        // Настройка фильтра по статусу
        statusFilterComboBox.getItems().addAll("PENDING", "ACTIVE", "REJECTED", "BLOCKED", "ALL");
        statusFilterComboBox.setValue("PENDING");
        statusFilterComboBox.valueProperty().addListener((obs, old, newVal) -> {
            currentStatusFilter = newVal;
            loadUsers();
        });

        // Настройка кнопок
        approveButton.setOnAction(e -> approveUser());
        rejectButton.setOnAction(e -> rejectUser());
        blockButton.setOnAction(e -> blockUser());
        unblockButton.setOnAction(e -> unblockUser());

        // Отключаем кнопки, если ничего не выбрано
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> updateButtonsState(newVal));

        setupTable();
        loadUsers();
    }

    private void setupTable() {
        idColumn.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getId()).asObject());
        emailColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        nameColumn.setCellValueFactory(cellData -> {
            String firstName = cellData.getValue().getFirstName() != null ? cellData.getValue().getFirstName() : "";
            String lastName = cellData.getValue().getLastName() != null ? cellData.getValue().getLastName() : "";
            return new SimpleStringProperty((firstName + " " + lastName).trim());
        });
        phoneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPhone() != null ? cellData.getValue().getPhone() : ""
        ));
        roleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getRole()));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus()));
        createdAtColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreatedAt() != null) {
                return new SimpleStringProperty(cellData.getValue().getCreatedAt().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });
        approvedByColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getApprovedBy() != null ? cellData.getValue().getApprovedBy() : ""
        ));
        rejectionReasonColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getRejectionReason() != null ? cellData.getValue().getRejectionReason() : ""
        ));

        // Цвет для статуса
        statusColumn.setCellFactory(column -> new TableCell<UserDto, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "PENDING" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case "ACTIVE" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case "REJECTED" -> setStyle("-fx-text-fill: red;");
                        case "BLOCKED" -> setStyle("-fx-text-fill: gray;");
                    }
                }
            }
        });
    }

    private void updateButtonsState(UserDto selected) {
        boolean hasSelection = selected != null;
        approveButton.setDisable(!hasSelection);
        rejectButton.setDisable(!hasSelection);
        blockButton.setDisable(!hasSelection);
        unblockButton.setDisable(!hasSelection);

        if (selected != null) {
            String status = selected.getStatus();
            approveButton.setDisable(!(status.equals("PENDING") || status.equals("REJECTED")));
            rejectButton.setDisable(!status.equals("PENDING"));
            blockButton.setDisable(!status.equals("ACTIVE"));
            unblockButton.setDisable(!status.equals("BLOCKED"));
        }
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                String url;
                if ("ALL".equals(currentStatusFilter)) {
                    url = "/users";
                } else {
                    url = "/users?status=" + currentStatusFilter;
                }

                System.out.println("Loading users from: " + url);
                System.out.println("Current token: " + ApiClient.getAuthToken());

                ApiResponse<List<UserDto>> response = ApiClient.get(url,
                        new TypeReference<ApiResponse<List<UserDto>>>() {});

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        userList.setAll(response.getData());
                        usersTable.setItems(userList);
                        statusLabel.setText("Найдено пользователей: " + userList.size());
                    } else {
                        showAlert("Ошибка", "Не удалось загрузить пользователей: " + response.getMessage(), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Ошибка", "Ошибка загрузки: " + e.getMessage(), Alert.AlertType.ERROR));
                e.printStackTrace();
            }
        }).start();
    }

    private void approveUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Подтверждение регистрации");
        confirmAlert.setContentText("Вы уверены, что хотите подтвердить регистрацию пользователя " + selected.getEmail() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    ApiResponse<Void> response = ApiClient.post("/users/" + selected.getId() + "/approve", null,
                            new TypeReference<ApiResponse<Void>>() {});

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            showAlert("Успешно", "Пользователь " + selected.getEmail() + " подтверждён", Alert.AlertType.INFORMATION);
                            loadUsers();
                        } else {
                            showAlert("Ошибка", "Ошибка при подтверждении: " + response.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Ошибка", "Ошибка: " + e.getMessage(), Alert.AlertType.ERROR));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void rejectUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Отклонение");
        reasonDialog.setHeaderText("Отклонение регистрации пользователя " + selected.getEmail());
        reasonDialog.setContentText("Укажите причину отклонения:");

        Optional<String> result = reasonDialog.showAndWait();
        if (result.isPresent()) {
            String reason = result.get().trim();
            if (reason.isEmpty()) {
                showAlert("Ошибка", "Необходимо указать причину отклонения", Alert.AlertType.ERROR);
                return;
            }

            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Подтверждение");
            confirmAlert.setHeaderText("Отклонение регистрации");
            confirmAlert.setContentText("Вы уверены, что хотите отклонить регистрацию пользователя " + selected.getEmail() + "?\n\nПричина: " + reason);

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiResponse<Void> response = ApiClient.post("/users/" + selected.getId() + "/reject",
                                new RejectRequest(reason), new TypeReference<ApiResponse<Void>>() {});

                        Platform.runLater(() -> {
                            if (response.isSuccess()) {
                                showAlert("Успешно", "Регистрация пользователя " + selected.getEmail() + " отклонена", Alert.AlertType.INFORMATION);
                                loadUsers();
                            } else {
                                showAlert("Ошибка", "Ошибка при отклонении: " + response.getMessage(), Alert.AlertType.ERROR);
                            }
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Ошибка", "Ошибка: " + e.getMessage(), Alert.AlertType.ERROR));
                        e.printStackTrace();
                    }
                }).start();
            }
        }
    }

    private void blockUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog reasonDialog = new TextInputDialog();
        reasonDialog.setTitle("Блокировка");
        reasonDialog.setHeaderText("Блокировка пользователя " + selected.getEmail());
        reasonDialog.setContentText("Укажите причину блокировки (необязательно):");

        Optional<String> result = reasonDialog.showAndWait();
        String reason = result.orElse("");

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Блокировка пользователя");
        confirmAlert.setContentText("Вы уверены, что хотите заблокировать пользователя " + selected.getEmail() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    ApiResponse<Void> response = ApiClient.post("/users/" + selected.getId() + "/block",
                            new BlockRequest(reason), new TypeReference<ApiResponse<Void>>() {});

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            showAlert("Успешно", "Пользователь " + selected.getEmail() + " заблокирован", Alert.AlertType.INFORMATION);
                            loadUsers();
                        } else {
                            showAlert("Ошибка", "Ошибка при блокировке: " + response.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Ошибка", "Ошибка: " + e.getMessage(), Alert.AlertType.ERROR));
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void unblockUser() {
        UserDto selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Подтверждение");
        confirmAlert.setHeaderText("Разблокировка пользователя");
        confirmAlert.setContentText("Вы уверены, что хотите разблокировать пользователя " + selected.getEmail() + "?");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    ApiResponse<Void> response = ApiClient.post("/users/" + selected.getId() + "/unblock", null,
                            new TypeReference<ApiResponse<Void>>() {});

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            showAlert("Успешно", "Пользователь " + selected.getEmail() + " разблокирован", Alert.AlertType.INFORMATION);
                            loadUsers();
                        } else {
                            showAlert("Ошибка", "Ошибка при разблокировке: " + response.getMessage(), Alert.AlertType.ERROR);
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Ошибка", "Ошибка: " + e.getMessage(), Alert.AlertType.ERROR));
                    e.printStackTrace();
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

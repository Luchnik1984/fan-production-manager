package com.fanproduction.gui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.ChangePasswordRequest;
import com.fanproduction.gui.dto.UpdateProfileRequest;
import com.fanproduction.gui.dto.UserDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ProfileController {

    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label lastLoginLabel;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;

    @FXML
    private Label firstNameErrorLabel;
    @FXML
    private Label lastNameErrorLabel;
    @FXML
    private Label phoneErrorLabel;
    @FXML
    private Label messageLabel;

    private Stage stage;
    private UserDto currentUser;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        loadProfile();

        // Очищаем ошибки при вводе
        firstNameField.textProperty().addListener((obs, old, newVal) -> {
            firstNameErrorLabel.setText("");
            messageLabel.setText("");
        });
        lastNameField.textProperty().addListener((obs, old, newVal) -> {
            lastNameErrorLabel.setText("");
            messageLabel.setText("");
        });
        phoneField.textProperty().addListener((obs, old, newVal) -> {
            phoneErrorLabel.setText("");
            messageLabel.setText("");
        });
    }

    private void loadProfile() {
        new Thread(() -> {
            try {
                ApiResponse<UserDto> response = ApiClient.get("/users/me",
                        new TypeReference<ApiResponse<UserDto>>() {});

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        currentUser = response.getData();
                        updateUI();
                    } else {
                        showError("Не удалось загрузить профиль: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка загрузки профиля: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void updateUI() {
        emailLabel.setText(currentUser.getEmail());
        roleLabel.setText(currentUser.getRole());

        // Отображение статуса с цветом
        String status = currentUser.getStatus();
        switch (status) {
            case "ACTIVE" -> statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            case "PENDING" -> statusLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            case "REJECTED" -> statusLabel.setStyle("-fx-text-fill: red;");
            case "BLOCKED" -> statusLabel.setStyle("-fx-text-fill: gray;");
        }
        statusLabel.setText(status);

        createdAtLabel.setText(currentUser.getCreatedAt() != null ?
                currentUser.getCreatedAt().format(DATE_FORMATTER) : "—");
        lastLoginLabel.setText(currentUser.getLastLoginAt() != null ?
                currentUser.getLastLoginAt().format(DATE_FORMATTER) : "—");

        firstNameField.setText(currentUser.getFirstName() != null ? currentUser.getFirstName() : "");
        lastNameField.setText(currentUser.getLastName() != null ? currentUser.getLastName() : "");
        phoneField.setText(currentUser.getPhone() != null ? currentUser.getPhone() : "");
    }

    @FXML
    private void handleSaveProfile() {
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setText("");
        phoneErrorLabel.setText("");
        messageLabel.setText("");

        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();

        if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty()) {
            if (firstName.isEmpty()) firstNameErrorLabel.setText("Имя не может быть пустым");
            if (lastName.isEmpty()) lastNameErrorLabel.setText("Фамилия не может быть пустой");
            if (phone.isEmpty()) phoneErrorLabel.setText("Телефон не может быть пустым");
            return;
        }

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhone(phone);

        new Thread(() -> {
            try {
                ApiResponse<Void> response = ApiClient.put("/users/me", request,
                        new TypeReference<ApiResponse<Void>>() {});

                Platform.runLater(() -> {
                    if (response.isSuccess()) {
                        showSuccess("Профиль успешно обновлён");
                        loadProfile();
                    } else {
                        showError(response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка сохранения: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleChangePassword() {
        showChangePasswordDialog();
    }

    private void showChangePasswordDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        dialog.setTitle("Смена пароля");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Текущий пароль");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Новый пароль (мин. 4 символа)");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтверждение пароля");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        grid.add(new Label("Текущий пароль:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("Новый пароль:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Подтверждение:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(messageLabel, 1, 3);
        grid.add(saveButton, 0, 4);
        grid.add(cancelButton, 1, 4);

        saveButton.setOnAction(e -> {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("Все поля должны быть заполнены");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                messageLabel.setText("Новый пароль и подтверждение не совпадают");
                return;
            }

            if (newPassword.length() < 4) {
                messageLabel.setText("Новый пароль должен содержать не менее 4 символов");
                return;
            }

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setOldPassword(oldPassword);
            request.setNewPassword(newPassword);

            new Thread(() -> {
                try {
                    ApiResponse<Void> response = ApiClient.post("/users/me/change-password", request,
                            new TypeReference<ApiResponse<Void>>() {});

                    Platform.runLater(() -> {
                        if (response.isSuccess()) {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Успешно");
                            alert.setHeaderText(null);
                            alert.setContentText("Пароль успешно изменён");
                            alert.showAndWait();
                            dialog.close();
                        } else {
                            messageLabel.setText(response.getMessage());
                        }
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> messageLabel.setText("Ошибка: " + ex.getMessage()));
                    ex.printStackTrace();
                }
            }).start();
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid, 450, 280);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
}
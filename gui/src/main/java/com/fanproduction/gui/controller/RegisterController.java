package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.AuthResponse;
import com.fanproduction.gui.dto.RegisterRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private VBox secretKeyBox;
    @FXML
    private PasswordField secretKeyField;
    @FXML
    private Label errorLabel;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        roleComboBox.getItems().addAll("ENGINEER", "MANAGER", "ADMIN");
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isAdmin = "ADMIN".equals(newVal);
            secretKeyBox.setVisible(isAdmin);
            secretKeyBox.setManaged(isAdmin);
            if (!isAdmin) secretKeyField.clear();
        });
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleComboBox.getValue();

        // Валидация
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showError("Заполните все обязательные поля");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Пароли не совпадают");
            return;
        }

        if (password.length() < 4) {
            showError("Пароль должен содержать не менее 4 символов");
            return;
        }

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhone(phone);
        request.setRole(role);

        // Если ADMIN, добавляем секретный ключ
        if ("ADMIN".equals(role)) {
            String secretKey = secretKeyField.getText();
            if (secretKey == null || secretKey.isEmpty()) {
                showError("Для регистрации администратора требуется секретный ключ");
                return;
            }
            request.setSecretKey(secretKey);
        }

        new Thread(() -> {
            try {
                // Сервер возвращает AuthResponse, а не ApiResponse
                AuthResponse response = ApiClient.post("/auth/register", request, AuthResponse.class);

                Platform.runLater(() -> {
                    // Если получили токен, значит регистрация успешна
                    if (response != null && response.getToken() != null) {
                        String message;
                        if ("ADMIN".equals(role)) {
                            message = "Администратор успешно зарегистрирован! Вы можете войти.";
                        } else {
                            message = "Регистрация успешна! После подтверждения администратором вы сможете войти.";
                        }
                        showSuccess(message);
                        goToLogin();
                    } else {
                        showError("Ошибка регистрации");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Ошибка регистрации: " + e.getMessage()));
                e.printStackTrace();
            }
        }).start();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 450);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Вход");
        } catch (IOException e) {
            showError("Ошибка загрузки окна входа");
        }
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    @FXML
    private void handleBack() {
        goToLogin();
    }
}

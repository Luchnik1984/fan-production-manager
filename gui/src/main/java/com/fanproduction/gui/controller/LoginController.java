package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.AuthResponse;
import com.fanproduction.gui.dto.LoginRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox rememberMeCheckBox;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        emailField.textProperty().addListener((obs, old, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError());
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setStyle("");
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Ошибка", "Email и пароль не могут быть пустыми", Alert.AlertType.ERROR);
            return;
        }

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        new Thread(() -> {
            try {
                AuthResponse loginResponse = ApiClient.post("/auth/login", request, AuthResponse.class);

                Platform.runLater(() -> {
                    ApiClient.setAuthToken(loginResponse.getToken());
                    openMainWindow(loginResponse.getRole());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMsg = e.getMessage();
                    System.out.println("Login error: " + errorMsg);

                    // Обрабатываем различные сообщения об ошибках
                    if (errorMsg.contains("ожидает подтверждения") || errorMsg.contains("PENDING")) {
                        showAlert("Внимание", "⏳ Ваша регистрация ожидает подтверждения администратором.", Alert.AlertType.WARNING);
                    } else if (errorMsg.contains("отклонена") || errorMsg.contains("REJECTED")) {
                        showAlert("Отказ", "❌ Ваша регистрация отклонена администратором.", Alert.AlertType.ERROR);
                    } else if (errorMsg.contains("заблокирован") || errorMsg.contains("BLOCKED")) {
                        showAlert("Блокировка", "🔒 Ваш аккаунт заблокирован. Обратитесь к администратору.", Alert.AlertType.ERROR);
                    } else if (errorMsg.contains("не найден")) {
                        showAlert("Ошибка", "Пользователь с таким email не найден", Alert.AlertType.ERROR);
                    } else if (errorMsg.contains("Неверный пароль") || errorMsg.contains("Bad credentials")) {
                        showAlert("Ошибка", "Неверный пароль", Alert.AlertType.ERROR);
                    } else {
                        showAlert("Ошибка", "Ошибка входа: " + errorMsg, Alert.AlertType.ERROR);
                    }
                    passwordField.clear();
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void openMainWindow(String userRole) {
        try {
            Stage mainStage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController controller = loader.getController();
            controller.setStage(mainStage);
            controller.setCurrentUserRole(userRole);

            Scene scene = new Scene(root, 1024, 768);
            mainStage.setScene(scene);
            mainStage.setTitle("Fan Production Manager - Главное окно");
            mainStage.show();

            primaryStage.close();
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка открытия главного окна: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/RegisterView.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 650);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Регистрация");

        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка загрузки окна регистрации: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleForgotPassword() {
        showAlert("Информация", "Функция восстановления пароля будет доступна позже", Alert.AlertType.INFORMATION);
    }
}
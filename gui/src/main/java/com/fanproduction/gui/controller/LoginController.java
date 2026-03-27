package com.fanproduction.gui.controller;


import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.LoginRequest;
import com.fanproduction.gui.dto.LoginResponse;
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
        // Загружаем сохранённый email из Preferences (можно добавить позже)
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
            showError("Email и пароль не могут быть пустыми");
            return;
        }

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        new Thread(() -> {
            try {
                LoginResponse loginResponse = ApiClient.post("/auth/login", request, LoginResponse.class);

                Platform.runLater(() -> {
                    ApiClient.setAuthToken(loginResponse.getToken());
                    openMainWindow();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showError("Ошибка входа: " + e.getMessage());
                    passwordField.clear();
                });
                e.printStackTrace();
            }
        }).start();
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
            showError("Ошибка загрузки окна регистрации: " + e.getMessage());
        }
    }

    private void openMainWindow() {
        try {
            Stage mainStage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController controller = loader.getController();
            controller.setStage(mainStage);

            Scene scene = new Scene(root, 1024, 768);
            mainStage.setScene(scene);
            mainStage.setTitle("Fan Production Manager - Главное окно");
            mainStage.show();

            primaryStage.close();
        } catch (IOException e) {
            showError("Ошибка открытия главного окна: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    @FXML
    private void handleForgotPassword() {
        showError("Функция восстановления пароля будет доступна позже");
    }
}
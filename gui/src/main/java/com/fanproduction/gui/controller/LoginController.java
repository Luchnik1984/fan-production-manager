package com.fanproduction.gui.controller;

import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.response.AuthResponse;
import com.fanproduction.gui.dto.request.LoginRequest;
import com.fanproduction.gui.dto.request.RefreshTokenRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import lombok.Setter;

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

    @Setter
    private Stage primaryStage;

    @FXML
    private void initialize() {
        emailField.textProperty().addListener((obs, old, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, old, newVal) -> clearError());

        // Пытаемся автоматически войти по refresh token
        attemptAutoLogin();
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setStyle("");
    }

    /**
     * Попытка автоматического входа по сохранённому refresh token
     */
    private void attemptAutoLogin() {
        if (UserPreferences.hasValidRefreshToken()) {
            String refreshToken = UserPreferences.getRefreshToken();
            String savedEmail = UserPreferences.getSavedEmail();

            if (savedEmail != null && !savedEmail.isEmpty()) {
                emailField.setText(savedEmail);
                rememberMeCheckBox.setSelected(true);

                // Пытаемся обновить токен
                new Thread(() -> {
                    try {
                        RefreshTokenRequest request = new RefreshTokenRequest();
                        request.setRefreshToken(refreshToken);

                        AuthResponse response = ApiClient.post("/auth/refresh", request, AuthResponse.class);

                        Platform.runLater(() -> {
                            ApiClient.setAuthToken(response.getToken());
                            // Обновляем refresh token
                            if (rememberMeCheckBox.isSelected()) {
                                UserPreferences.saveRefreshToken(response.getEmail(), response.getRefreshToken());
                            }
                            openMainWindow(response.getRole(), response.getEmail());
                        });

                    } catch (Exception e) {
                        // Если не удалось, просто показываем окно входа
                        Platform.runLater(() -> {
                            UserPreferences.clearRememberData();
                            passwordField.requestFocus();
                        });
                    }
                }).start();
            }
        }
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
                    // Сохраняем refresh token, если выбран "Запомнить меня"
                    if (rememberMeCheckBox.isSelected()) {
                        UserPreferences.saveRefreshToken(loginResponse.getEmail(), loginResponse.getRefreshToken());
                    } else {
                        UserPreferences.clearRememberData();
                    }

                    ApiClient.setAuthToken(loginResponse.getToken());
                    openMainWindow(loginResponse.getRole(), loginResponse.getEmail());
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMsg = e.getMessage();
                    System.out.println("Login error: " + errorMsg);

                    if (errorMsg.contains("ожидает подтверждения") ||
                            errorMsg.contains("подтверждения администратором") ||
                            errorMsg.contains("PENDING")) {
                        showAlert("Внимание",
                                """
                                        ⏳ Ваша регистрация ожидает подтверждения администратором.
                                        
                                        После подтверждения вы сможете войти в систему.""",
                                Alert.AlertType.WARNING);

                    } else if (errorMsg.contains("отклонена") ||
                            errorMsg.contains("REJECTED")) {
                        showAlert("Отказ",
                                """
                                        ❌ Ваша регистрация отклонена администратором.
                                        
                                        Обратитесь к администратору для уточнения причин.""",
                                Alert.AlertType.ERROR);

                    } else if (errorMsg.contains("заблокирован") ||
                            errorMsg.contains("BLOCKED")) {
                        showAlert("Блокировка",
                                """
                                        🔒 Ваш аккаунт заблокирован.
                                        
                                        Обратитесь к администратору для выяснения причин.""",
                                Alert.AlertType.ERROR);

                    } else if (errorMsg.contains("не найден") ||
                            errorMsg.contains("User not found")) {
                        showAlert("Ошибка",
                                """
                                        Пользователь с таким email не найден.
                                        
                                        Проверьте правильность ввода email.""",
                                Alert.AlertType.ERROR);

                    } else if (errorMsg.contains("Неверный пароль") ||
                            errorMsg.contains("Bad credentials") ||
                            errorMsg.contains("пароль")) {
                        showAlert("Ошибка",
                                """
                                        Неверный пароль.
                                        
                                        Пожалуйста, проверьте правильность ввода пароля.""",
                                Alert.AlertType.ERROR);

                    } else {
                        showAlert("Ошибка", "Ошибка входа: " + errorMsg, Alert.AlertType.ERROR);
                    }

                    passwordField.clear();
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void openMainWindow(String userRole, String userEmail) {
        try {
            Stage mainStage = new Stage();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/MainWindow.fxml"));
            Parent root = loader.load();

            MainWindowController controller = loader.getController();
            controller.setStage(mainStage);
            controller.setCurrentUserRole(userRole);
            controller.setCurrentUserEmail(userEmail);

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
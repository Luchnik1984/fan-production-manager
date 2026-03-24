package com.fanproduction.gui.controller;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.MainWindow;
import com.fanproduction.services.AuditService;
import com.fanproduction.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private CheckBox rememberMeCheckBox;

    private SpringContextProvider springContext;
    private Stage primaryStage;
    private UserService userService;
    private AuditService auditService;

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
        this.auditService = springContext.getBean(AuditService.class);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        if (UserPreferences.hasLastEmail()) {
            emailField.setText(UserPreferences.getLastEmail());
            passwordField.requestFocus();
        }

        emailField.textProperty().addListener((obs, old, newVal) -> {
            errorLabel.setText("");
            errorLabel.setStyle("");
        });
        passwordField.textProperty().addListener((obs, old, newVal) -> {
            errorLabel.setText("");
            errorLabel.setStyle("");
        });
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Email и пароль не могут быть пустыми");
            return;
        }

        new Thread(() -> {
            try {
                UserEntity user = userService.getUserByEmail(email);

                if (user == null) {
                    showError("Пользователь с таким email не найден");
                    auditService.log(email, AuditAction.LOGIN_FAILED, "Пользователь не найден");
                    Platform.runLater(() -> passwordField.clear());
                    return;
                }

                // Проверка статуса
                if (user.getStatus() != UserStatus.ACTIVE) {
                    String message;
                    Alert.AlertType alertType;

                    switch (user.getStatus()) {
                        case PENDING:
                            message = "⏳ Ваша регистрация ожидает подтверждения администратором.\nПосле подтверждения вы сможете войти в систему.";
                            alertType = Alert.AlertType.WARNING;  // Оранжевое окно
                            break;
                        case REJECTED:
                            message = "❌ Ваша регистрация отклонена.\nПричина: " +
                                    (user.getRejectionReason() != null ? user.getRejectionReason() : "не указана");
                            alertType = Alert.AlertType.ERROR;
                            break;
                        case BLOCKED:
                            message = "🔒 Ваш аккаунт заблокирован.\nОбратитесь к администратору.";
                            alertType = Alert.AlertType.ERROR;
                            break;
                        default:
                            message = "Доступ запрещён. Статус аккаунта: " + user.getStatus();
                            alertType = Alert.AlertType.ERROR;
                            break;
                    }

                    showAlert(message, alertType);
                    auditService.log(email, AuditAction.LOGIN_FAILED,
                            "Попытка входа с неактивным статусом: " + user.getStatus());
                    Platform.runLater(() -> passwordField.clear());
                    return;
                }

                // Проверка пароля
                if (userService.authenticate(email, password)) {
                    Platform.runLater(() -> {
                        if (rememberMeCheckBox.isSelected()) {
                            UserPreferences.saveRememberData(email, password);
                        } else {
                            UserPreferences.saveLastEmail(email);
                        }

                        user.setLastLoginAt(LocalDateTime.now());
                        user.setLoginCount(user.getLoginCount() + 1);
                        userService.updateLoginInfo(user);

                        SessionContext.setCurrentUser(user);
                        openMainWindow();
                    });
                    auditService.log(email, AuditAction.LOGIN_SUCCESS, "Успешный вход в систему");
                } else {
                    showError("Неверный пароль");
                    auditService.log(email, AuditAction.LOGIN_FAILED, "Неверный пароль");
                    Platform.runLater(() -> passwordField.clear());
                }
            } catch (Exception e) {
                showError("Ошибка при входе: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(String message) {
        showAlert(message, Alert.AlertType.ERROR);
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        System.out.println("Showing alert: " + message + " (type: " + alertType + ")");
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            if (alertType == Alert.AlertType.ERROR) {
                alert.setTitle("Ошибка входа");
            } else if (alertType == Alert.AlertType.WARNING) {
                alert.setTitle("Внимание");
            } else {
                alert.setTitle("Информация");
            }
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();

            // Также обновляем label
            errorLabel.setText(message);
            if (alertType == Alert.AlertType.WARNING) {
                errorLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
            } else {
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        });
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/RegisterView.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setSpringContext(springContext);
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
            MainWindow mainWindow = new MainWindow(springContext);
            mainWindow.start(mainStage);
            primaryStage.close();
        } catch (Exception e) {
            showError("Ошибка при открытии главного окна: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText("Функция восстановления пароля будет доступна позже");
        alert.showAndWait();
    }

}
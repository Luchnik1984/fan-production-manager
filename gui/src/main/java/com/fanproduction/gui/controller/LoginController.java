package com.fanproduction.gui.controller;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.SafeExecutor;
import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.MainWindow;
import com.fanproduction.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
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

        SafeExecutor.execute(() -> {
            UserEntity user = userService.getUserByEmail(email);

            if (user == null) {
                Platform.runLater(() -> {
                    showError("Пользователь с таким email не найден");
                    passwordField.clear();
                });
                return;
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                Platform.runLater(() -> {
                    handleNonActiveUser(user);
                    passwordField.clear();
                });
                return;
            }

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
            } else {
                Platform.runLater(() -> {
                    showError("Неверный пароль");
                    passwordField.clear();
                });
            }
        }, e -> {
            // Обработчик ошибок SafeExecutor
            Platform.runLater(() -> showError("Ошибка при входе: " + e.getMessage()));
        });
    }

    private void handleNonActiveUser(UserEntity user) {
        switch (user.getStatus()) {
            case PENDING:
                showWarning("⏳ Ваша регистрация ожидает подтверждения администратором.\nПосле подтверждения вы сможете войти в систему.");
                break;
            case REJECTED:
                showError("❌ Ваша регистрация отклонена.\nПричина: " +
                        (user.getRejectionReason() != null ? user.getRejectionReason() : "не указана"));
                break;
            case BLOCKED:
                showError("🔒 Ваш аккаунт заблокирован.\nОбратитесь к администратору.");
                break;
            default:
                showError("Доступ запрещён. Статус аккаунта: " + user.getStatus());
                break;
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void showWarning(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
    }

    @FXML
    private void handleRegister() {
        SafeExecutor.loadFxml("/com/fanproduction/gui/view/RegisterView.fxml",
                (root, controller) -> {
                    RegisterController registerController = (RegisterController) controller;
                    registerController.setSpringContext(springContext);
                    registerController.setPrimaryStage(primaryStage);

                    Scene scene = new Scene(root, 400, 650);
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Регистрация");
                },
                e -> Platform.runLater(() -> showError("Ошибка загрузки окна регистрации: " + e.getMessage()))
        );
    }

    private void openMainWindow() {
        SafeExecutor.execute(() -> {
            Stage mainStage = new Stage();
            MainWindow mainWindow = new MainWindow(springContext);
            mainWindow.start(mainStage);
            primaryStage.close();
        }, e -> Platform.runLater(() -> showError("Ошибка при открытии главного окна: " + e.getMessage())));
    }

    @FXML
    private void handleForgotPassword() {
        showWarning("Функция восстановления пароля будет доступна позже");
    }
}
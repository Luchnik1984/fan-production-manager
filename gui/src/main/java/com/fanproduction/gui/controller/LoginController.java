package com.fanproduction.gui.controller;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.MainWindow;
import com.fanproduction.services.UserService;
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
        // Загружаем сохранённый email, если есть
        if (UserPreferences.hasLastEmail()) {
            emailField.setText(UserPreferences.getLastEmail());
            // Ставим фокус на поле пароля для удобства
            passwordField.requestFocus();
        }

        // Очищаем ошибку при вводе
        emailField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setText(""));
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> errorLabel.setText(""));
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email и пароль не могут быть пустыми");
            return;
        }

        try {
            if (userService.authenticate(email, password)) {
                // Если чекбокс отмечен, сохраняем данные для автоматического входа
                if (rememberMeCheckBox.isSelected()) {
                    UserPreferences.saveRememberData(email, password);
                } else {
                    // Если не отмечен, сохраняем только email (как раньше)
                    UserPreferences.saveLastEmail(email);
                }
                openMainWindow();
            } else {
                errorLabel.setText("Неверный email или пароль");
                passwordField.clear();
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка при входе: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/RegisterView.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setSpringContext(springContext);
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 650); // Увеличил высоту для всех полей
            primaryStage.setScene(scene);
            primaryStage.setTitle("Регистрация");
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки окна регистрации");
        }
    }

    private void openMainWindow() {
        try {
            // Создаём новый Stage для главного окна
            Stage mainStage = new Stage();
            MainWindow mainWindow = new MainWindow(springContext);
            mainWindow.start(mainStage);

            // Закрываем окно входа
            primaryStage.close();
        } catch (Exception e) {
            errorLabel.setText("Ошибка при открытии главного окна: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleForgotPassword() {
        // Опционально: окно восстановления пароля
        errorLabel.setText("Функция восстановления пароля будет доступна позже");
    }
}

package com.fanproduction.gui.controller;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    private UserService userService;


    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    private SpringContextProvider springContext;
    private Stage primaryStage;

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
    }

    public void setPrimaryStage(Stage primaryStage) {

        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        // Очищаем сообщение об ошибке при вводе текста
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setText("");
        });

        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setText("");
        });
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Простейшая валидация
        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Email и пароль не могут быть пустыми");
            return;
        }

        try {
            if (userService.authenticate(email, password)) {
                openMainWindow();
            } else {
                errorLabel.setText("Неверный email или пароль");
            }
        } catch (Exception e) {
            errorLabel.setText("Ошибка при входе: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            // Загружаем окно регистрации
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/RegisterView.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setSpringContext(springContext);
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 600);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Регистрация");
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки окна регистрации");
        }
    }

    @FXML
    private void handleForgotPassword() {
        // Опционально: окно восстановления пароля
        errorLabel.setText("Функция восстановления пароля будет доступна позже");
    }

    private void openMainWindow() {
        try {
            // Здесь нужно будет создать MainWindow.fxml позже
            // Пока используем существующий JavaFXSpringApplication
            com.fanproduction.gui.JavaFXSpringApplication mainApp = new com.fanproduction.gui.JavaFXSpringApplication();
            mainApp.setSpringContextProvider(springContext);
            mainApp.start(primaryStage);

        } catch (Exception e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки главного окна");
        }
    }
}

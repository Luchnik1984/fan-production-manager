package com.fanproduction.gui.controller;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.services.UserService;
import javafx.collections.FXCollections;
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
    private VBox roleBox;

    @FXML
    private ComboBox<Role> roleComboBox;

    @FXML
    private Label errorLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button backButton;

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
        // Заполняем ComboBox ролями
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));

        // Очищаем ошибку при вводе
        emailField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        passwordField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));

        // TODO: Проверка, является ли текущий пользователь ADMIN
        // Пока скрываем выбор роли
        roleBox.setVisible(false);
        roleBox.setManaged(false);
    }

    @FXML
    private void handleRegister() {
        // Валидация полей
        if (!validateFields()) {
            return;
        }

        try {
            // Создаём нового пользователя
            UserEntity newUser = new UserEntity();
            newUser.setEmail(emailField.getText().trim().toLowerCase());
            newUser.setPassword(passwordField.getText()); // TODO: хешировать пароль
            newUser.setFirstName(firstNameField.getText().trim());
            newUser.setLastName(lastNameField.getText().trim());
            newUser.setPhone(phoneField.getText().trim());

            // Если роль не выбрана или скрыта, ставим ENGINEER по умолчанию
            if (roleComboBox.getValue() != null) {
                newUser.setRole(roleComboBox.getValue());
            } else {
                newUser.setRole(Role.ENGINEER);
            }

            // Сохраняем пользователя
            userService.register(newUser);

            // Показываем сообщение об успехе и возвращаемся к входу
            showSuccessAndGoToLogin();

        } catch (Exception e) {
            errorLabel.setText("Ошибка при регистрации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateFields() {
        // Проверка email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            errorLabel.setText("Email не может быть пустым");
            return false;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorLabel.setText("Некорректный формат email");
            return false;
        }

        // Проверка пароля
        String password = passwordField.getText();
        if (password.isEmpty()) {
            errorLabel.setText("Пароль не может быть пустым");
            return false;
        }
        if (password.length() < 4) {
            errorLabel.setText("Пароль должен быть не менее 4 символов");
            return false;
        }

        // Проверка подтверждения пароля
        if (!password.equals(confirmPasswordField.getText())) {
            errorLabel.setText("Пароли не совпадают");
            return false;
        }

        // Проверка имени (необязательно, но если заполнено - ок)
        String firstName = firstNameField.getText().trim();
        if (firstName.length() > 50) {
            errorLabel.setText("Имя слишком длинное (макс. 50 символов)");
            return false;
        }

        String lastName = lastNameField.getText().trim();
        if (lastName.length() > 50) {
            errorLabel.setText("Фамилия слишком длинная (макс. 50 символов)");
            return false;
        }

        return true;
    }

    private void showSuccessAndGoToLogin() {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Регистрация");
            alert.setHeaderText(null);
            alert.setContentText("Пользователь успешно зарегистрирован!");
            alert.showAndWait();

            // Возвращаемся к окну входа
            goToLogin();
        } catch (Exception e) {
            errorLabel.setText("Ошибка при возврате к окну входа");
        }
    }

    @FXML
    private void handleBack() {
        goToLogin();
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setSpringContext(springContext);
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Fan Production Manager - Вход");
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            errorLabel.setText("Ошибка загрузки окна входа");
        }
    }
}
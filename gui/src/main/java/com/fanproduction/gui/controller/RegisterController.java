package com.fanproduction.gui.controller;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.security.AdminSecretKeyValidator;
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
    private ComboBox<Role> roleComboBox;

    @FXML
    private VBox secretKeyBox;

    @FXML
    private PasswordField secretKeyField;

    @FXML
    private Label errorLabel;

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
        // Заполняем ComboBox ролями (показываем все роли)
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));

        // При выборе роли показываем/скрываем поле для секретного ключа
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAdmin = (newVal == Role.ADMIN);
            secretKeyBox.setVisible(isAdmin);
            secretKeyBox.setManaged(isAdmin);
            if (!isAdmin) {
                secretKeyField.clear();
            }
        });

        // Очищаем ошибку при вводе в любое поле
        emailField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        passwordField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        confirmPasswordField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        firstNameField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        lastNameField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        phoneField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        roleComboBox.valueProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        secretKeyField.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
    }

    @FXML
    private void handleRegister() {
        // Базовая проверка обязательных полей
        if (!validateRequiredFields()) {
            return;
        }

        Role selectedRole = roleComboBox.getValue();

        // Если выбрана роль ADMIN, проверяем секретный ключ
        if (selectedRole == Role.ADMIN) {
            String secretKey = secretKeyField.getText();
            if (!AdminSecretKeyValidator.validate(secretKey)) {
                errorLabel.setText("Неверный секретный ключ администратора");
                return;
            }
        }

        try {
            // Создаём нового пользователя
            UserEntity newUser = new UserEntity();
            newUser.setEmail(emailField.getText().trim().toLowerCase());
            newUser.setPassword(passwordField.getText()); // TODO: хешировать пароль
            newUser.setFirstName(firstNameField.getText().trim());
            newUser.setLastName(lastNameField.getText().trim());
            newUser.setPhone(phoneField.getText().trim());
            newUser.setRole(selectedRole);

            // Устанавливаем статус: ADMIN сразу ACTIVE, остальные PENDING
            if (selectedRole == Role.ADMIN) {
                newUser.setStatus(UserStatus.ACTIVE);
            } else {
                newUser.setStatus(UserStatus.PENDING);
            }

            // Регистрируем пользователя через сервис
            userService.register(newUser);

            // Показываем сообщение об успехе и возвращаемся к окну входа
            showSuccessAndGoToLogin();

        } catch (IllegalArgumentException e) {
            // Ошибки валидации (например, email уже существует)
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Ошибка при регистрации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Базовая проверка полей, которые не покрыты валидацией в сервисе
     */
    private boolean validateRequiredFields() {
        // Проверка на пустой email
        if (emailField.getText().trim().isEmpty()) {
            errorLabel.setText("Email не может быть пустым");
            return false;
        }

        // Проверка на пустой пароль
        if (passwordField.getText().isEmpty()) {
            errorLabel.setText("Пароль не может быть пустым");
            return false;
        }

        // Проверка совпадения паролей
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Пароли не совпадают");
            return false;
        }

        // Проверка выбора роли
        if (roleComboBox.getValue() == null) {
            errorLabel.setText("Выберите роль");
            return false;
        }

        return true;
    }

    private void showSuccessAndGoToLogin() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Регистрация");
        alert.setHeaderText(null);
        alert.setContentText("Пользователь успешно зарегистрирован!");
        alert.showAndWait();
        goToLogin();
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
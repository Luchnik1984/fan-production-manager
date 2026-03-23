package com.fanproduction.gui.controller;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.exception.ValidationException;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.security.AdminSecretKeyValidator;
import com.fanproduction.core.util.SafeExecutor;
import com.fanproduction.services.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
    private Label emailErrorLabel;
    @FXML
    private Label passwordErrorLabel;
    @FXML
    private Label firstNameErrorLabel;
    @FXML
    private Label lastNameErrorLabel;
    @FXML
    private Label phoneErrorLabel;
    @FXML
    private Label errorLabel;

    private SpringContextProvider springContext;
    private Stage primaryStage;
    private UserService userService;
    private final Map<String, Consumer<String>> errorHandlers = new HashMap<>();

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        initErrorHandlers();
        setupFieldListeners();
        setupRoleListener();
        roleComboBox.setItems(FXCollections.observableArrayList(Role.values()));
    }

    private void initErrorHandlers() {
        errorHandlers.put("email", msg -> emailErrorLabel.setText(msg));
        errorHandlers.put("password", msg -> passwordErrorLabel.setText(msg));
        errorHandlers.put("firstName", msg -> firstNameErrorLabel.setText(msg));
        errorHandlers.put("lastName", msg -> lastNameErrorLabel.setText(msg));
        errorHandlers.put("phone", msg -> phoneErrorLabel.setText(msg));
    }

    private void setupFieldListeners() {
        // Создаём список полей и соответствующих лейблов ошибок
        List<Pair<TextField, Label>> fields = List.of(
                new Pair<>(emailField, emailErrorLabel),
                new Pair<>(passwordField, passwordErrorLabel),
                new Pair<>(confirmPasswordField, passwordErrorLabel),
                new Pair<>(firstNameField, firstNameErrorLabel),
                new Pair<>(lastNameField, lastNameErrorLabel),
                new Pair<>(phoneField, phoneErrorLabel),
                new Pair<>(secretKeyField, errorLabel)
        );

        // Для каждого поля добавляем слушатель
        fields.forEach(pair -> {
            TextField field = pair.getKey();
            Label errorLabel = pair.getValue();
            field.textProperty().addListener((obs, old, newVal) -> errorLabel.setText(""));
        });
    }



        private void setupRoleListener() {
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAdmin = (newVal == Role.ADMIN);
            secretKeyBox.setVisible(isAdmin);
            secretKeyBox.setManaged(isAdmin);
            if (!isAdmin) secretKeyField.clear();
            clearAllErrors();
        });
    }

    private void clearAllErrors() {
        emailErrorLabel.setText("");
        passwordErrorLabel.setText("");
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setText("");
        phoneErrorLabel.setText("");
        errorLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        if (!validateRequiredFields()) return;

        Role selectedRole = roleComboBox.getValue();

        if (selectedRole == Role.ADMIN) {
            String secretKey = secretKeyField.getText();
            if (!AdminSecretKeyValidator.validate(secretKey)) {
                showError("Неверный секретный ключ администратора");
                return;
            }
        }

        SafeExecutor.execute(() -> {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(emailField.getText().trim().toLowerCase());
            newUser.setPassword(passwordField.getText());
            newUser.setFirstName(firstNameField.getText().trim());
            newUser.setLastName(lastNameField.getText().trim());
            newUser.setPhone(phoneField.getText().trim());
            newUser.setRole(selectedRole);
            newUser.setStatus(selectedRole == Role.ADMIN ? UserStatus.ACTIVE : UserStatus.PENDING);

            userService.register(newUser);

            Platform.runLater(() -> {
                if (selectedRole == Role.ADMIN) {
                    showSuccess("Пользователь успешно зарегистрирован! Вы можете войти в систему.");
                } else {
                    showSuccess("Запрос на регистрацию отправлен.\nПриложение будет доступно после подтверждения статуса администратором.");
                }
            });
        }, this::handleException);
    }

    private void handleException(Exception e) {
        clearAllErrors();

        if (e instanceof ValidationException ve) {
            ve.getViolations().forEach(violation -> {
                String fieldName = violation.getPropertyPath().toString();
                String message = violation.getMessage();

                errorHandlers.getOrDefault(fieldName, msg -> errorLabel.setText("Ошибка: " + msg))
                        .accept(message);
            });
        } else if (e instanceof IllegalArgumentException iae) {
            String message = iae.getMessage();
            if (message != null) {
                errorHandlers.entrySet().stream()
                        .filter(entry -> message.toLowerCase().contains(entry.getKey()))
                        .findFirst()
                        .ifPresentOrElse(
                                entry -> entry.getValue().accept(message),
                                () -> showError(message)
                        );
            }
        } else {
            showError("Ошибка при регистрации. Попробуйте позже.");
            e.printStackTrace();
        }
    }

    private boolean validateRequiredFields() {
        boolean isValid = true;

        // Проверка email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailErrorLabel.setText("Email не может быть пустым");
            isValid = false;
        }

        // Проверка пароля
        String password = passwordField.getText();
        if (password.isEmpty()) {
            passwordErrorLabel.setText("Пароль не может быть пустым");
            isValid = false;
        }

        // Проверка подтверждения пароля (только если пароль не пустой)
        if (!password.isEmpty() && !password.equals(confirmPasswordField.getText())) {
            passwordErrorLabel.setText("Пароли не совпадают");
            isValid = false;
        }

        // Проверка роли
        if (roleComboBox.getValue() == null) {
            errorLabel.setText("Выберите роль");
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            isValid = false;
        }

        // Имя
        if (firstNameField.getText().trim().isEmpty()) {
            firstNameErrorLabel.setText("Имя не может быть пустым");
            isValid = false;
        }

        // Фамилия
        if (lastNameField.getText().trim().isEmpty()) {
            lastNameErrorLabel.setText("Фамилия не может быть пустой");
            isValid = false;
        }

        // Телефон
        if (phoneField.getText().trim().isEmpty()) {
            phoneErrorLabel.setText("Телефон не может быть пустым");
            isValid = false;
        }

        return isValid;
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Регистрация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        goToLogin();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    @FXML
    private void handleBack() {
        goToLogin();
    }

    private void goToLogin() {
        SafeExecutor.loadFxml("/com/fanproduction/gui/view/LoginView.fxml",
                (root, controller) -> {
                    LoginController loginController = (LoginController) controller;
                    loginController.setSpringContext(springContext);
                    loginController.setPrimaryStage(primaryStage);

                    Scene scene = new Scene(root, 400, 450);
                    primaryStage.setScene(scene);
                    primaryStage.setTitle("Fan Production Manager - Вход");
                },
                e -> Platform.runLater(() -> showError("Ошибка загрузки окна входа: " + e.getMessage()))
        );
    }
}
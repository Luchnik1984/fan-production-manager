package com.fanproduction.gui.controller;

import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.Role;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.exception.ValidationException;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.security.AdminSecretKeyValidator;
import com.fanproduction.services.UserService;
import jakarta.validation.ConstraintViolation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RegisterController {

    // Поля ввода
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

    // Лейблы для ошибок под полями
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

    // Общий лейбл для ошибок
    @FXML
    private Label errorLabel;

    private SpringContextProvider springContext;
    private Stage primaryStage;
    private UserService userService;

    // Record для связки поля и его компонентов
    private record FieldComponents(
            TextField textField,
            Label errorLabel,
            String fieldName
    ) {}

    private Map<String, FieldComponents> fieldComponentsMap;

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        // Инициализируем маппинг полей
        fieldComponentsMap = Map.of(
                "email", new FieldComponents(emailField, emailErrorLabel, "email"),
                "password", new FieldComponents(passwordField, passwordErrorLabel, "password"),
                "firstName", new FieldComponents(firstNameField, firstNameErrorLabel, "firstName"),
                "lastName", new FieldComponents(lastNameField, lastNameErrorLabel, "lastName"),
                "phone", new FieldComponents(phoneField, phoneErrorLabel, "phone")
        );

        // Заполняем ComboBox ролями
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

        // Очищаем ошибки при вводе в любое поле
        setupFieldListeners();
    }

    private void setupFieldListeners() {
        // При изменении любого поля сбрасываем его ошибку
        fieldComponentsMap.values().forEach(components -> components.textField().textProperty().addListener((obs, old, newVal) -> {
            components.textField().setStyle("");
            components.errorLabel().setText("");
        }));

        // Очищаем общую ошибку при любом вводе
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
            newUser.setPassword(passwordField.getText());
            newUser.setFirstName(firstNameField.getText().trim());
            newUser.setLastName(lastNameField.getText().trim());
            newUser.setPhone(phoneField.getText().trim());
            newUser.setRole(selectedRole);

            // Устанавливаем статус: ADMIN сразу ACTIVE, остальные PENDING
            newUser.setStatus(selectedRole == Role.ADMIN ? UserStatus.ACTIVE : UserStatus.PENDING);

            // Регистрируем пользователя
            userService.register(newUser);

            // Показываем разное сообщение в зависимости от роли
            if (selectedRole == Role.ADMIN) {
                showSuccessMessage("Пользователь успешно зарегистрирован! Вы можете войти в систему.");
            } else {
                showSuccessMessage("Запрос на регистрацию отправлен.\n" +
                        "Приложение будет доступно после подтверждения статуса администратором.");
            }

        } catch (ValidationException e) {
            // Показываем детальные ошибки валидации
            showFieldErrors(e.getViolations());
        } catch (IllegalArgumentException e) {
            // Ошибка уникальности email или другая бизнес-ошибка
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Ошибка при регистрации: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showFieldErrors(Set<ConstraintViolation<UserEntity>> violations) {
        // Сбрасываем все ошибки
        resetFieldStyles();

        // Группируем нарушения по имени поля
        Map<String, List<ConstraintViolation<UserEntity>>> violationsByField =
                violations.stream()
                        .collect(Collectors.groupingBy(
                                v -> v.getPropertyPath().toString()
                        ));

        // Для каждого поля, у которого есть нарушения, показываем ошибку
        violationsByField.forEach((fieldName, fieldViolations) -> {
            FieldComponents components = fieldComponentsMap.get(fieldName);
            if (components != null) {
                // Берём первое сообщение для этого поля
                String message = fieldViolations.get(0).getMessage();

                // Подсвечиваем поле
                components.textField().setStyle("-fx-border-color: red; -fx-border-width: 2;");

                // Показываем сообщение
                components.errorLabel().setText(message);
            }
        });

        // Если есть ошибки, не связанные с конкретным полем, показываем их в общем лейбле
        List<ConstraintViolation<UserEntity>> globalViolations =
                violations.stream()
                        .filter(v -> !fieldComponentsMap.containsKey(v.getPropertyPath().toString()))
                        .toList();

        if (!globalViolations.isEmpty()) {
            String globalErrors = globalViolations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("\n"));
            errorLabel.setText(globalErrors);
        }
    }

    private void resetFieldStyles() {
        // Функционально проходим по всем компонентам
        fieldComponentsMap.values().forEach(components -> {
            components.textField().setStyle("");
            components.errorLabel().setText("");
        });
        errorLabel.setText("");
    }

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

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Регистрация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        goToLogin();
    }
}
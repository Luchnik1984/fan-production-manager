package com.fanproduction.gui.controller;

import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.response.AuthResponse;
import com.fanproduction.gui.dto.request.RegisterRequest;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
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
    private ComboBox<String> roleComboBox;
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

    private Stage primaryStage;

    // Map для связи имени поля с лейблом ошибки
    private final Map<String, Label> errorLabels = new HashMap<>();

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        // Инициализируем маппинг полей на лейблы ошибок
        errorLabels.put("email", emailErrorLabel);
        errorLabels.put("password", passwordErrorLabel);
        errorLabels.put("firstName", firstNameErrorLabel);
        errorLabels.put("lastName", lastNameErrorLabel);
        errorLabels.put("phone", phoneErrorLabel);

        roleComboBox.getItems().addAll("ENGINEER", "MANAGER", "ADMIN");
        roleComboBox.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            boolean isAdmin = "ADMIN".equals(newVal);
            secretKeyBox.setVisible(isAdmin);
            secretKeyBox.setManaged(isAdmin);
            if (!isAdmin) secretKeyField.clear();
            clearAllErrors();
        });

        // Очищаем ошибки при вводе
        setupFieldListeners();
    }

    private void setupFieldListeners() {
        Consumer<TextField> setupListener = field ->
                field.textProperty().addListener((obs, old, newVal) -> clearAllErrors());

        setupListener.accept(emailField);
        setupListener.accept(passwordField);
        setupListener.accept(confirmPasswordField);
        setupListener.accept(firstNameField);
        setupListener.accept(lastNameField);
        setupListener.accept(phoneField);
        setupListener.accept(secretKeyField);
    }

    private void clearAllErrors() {
        errorLabels.values().forEach(label -> label.setText(""));
        errorLabel.setText("");
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String role = roleComboBox.getValue();

        // Базовая валидация на клиенте
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || role == null) {
            showAlert("Ошибка", "Заполните все обязательные поля", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert("Ошибка", "Пароли не совпадают", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 4) {
            showAlert("Ошибка", "Пароль должен содержать не менее 4 символов", Alert.AlertType.ERROR);
            return;
        }

        RegisterRequest request = new RegisterRequest();
        request.setEmail(email);
        request.setPassword(password);
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setPhone(phone);
        request.setRole(role);

        if ("ADMIN".equals(role)) {
            String secretKey = secretKeyField.getText();
            if (secretKey == null || secretKey.isEmpty()) {
                showAlert("Ошибка", "Для регистрации администратора требуется секретный ключ", Alert.AlertType.ERROR);
                return;
            }
            request.setSecretKey(secretKey);
        }

        new Thread(() -> {
            try {
                AuthResponse response = ApiClient.post("/auth/register", request, AuthResponse.class);

                Platform.runLater(() -> {
                    if (response != null && response.getToken() != null) {
                        String message = "ADMIN".equals(role)
                                ? "Администратор успешно зарегистрирован! Вы можете войти."
                                : "Регистрация успешна! После подтверждения администратором вы сможете войти.";
                        showAlert("Успешно", message, Alert.AlertType.INFORMATION);
                        goToLogin();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    String errorMsg = e.getMessage();
                    System.out.println("Registration error: " + errorMsg);

                    // Пробуем распарсить ошибки валидации из JSON
                    Map<String, String> validationErrors = parseValidationErrors(errorMsg);

                    if (!validationErrors.isEmpty()) {
                        // Показываем ошибки под соответствующими полями
                        validationErrors.forEach((field, message) -> {
                            Label errorLabel = errorLabels.get(field);
                            if (errorLabel != null) {
                                errorLabel.setText(message);
                            } else {
                                // Если поле не найдено, показываем в общем лейбле
                                showError(message);
                            }
                        });
                    } else {
                        // Если не удалось распарсить, показываем общее сообщение
                        showAlert("Ошибка", "Ошибка регистрации: " + errorMsg, Alert.AlertType.ERROR);
                    }
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Парсит ошибки валидации из JSON ответа сервера
     * Использует properties() вместо deprecated fields()
     */
    private Map<String, String> parseValidationErrors(String responseBody) {
        Map<String, String> errors = new HashMap<>();
        if (responseBody == null || responseBody.isEmpty()) {
            return errors;
        }

        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(responseBody);

            if (node.isObject()) {
                // Используем properties() вместо fields() (не deprecated)
                node.properties().forEach(entry -> {
                    String field = entry.getKey();
                    String message = entry.getValue().asText();
                    errors.put(field, message);
                });
            }
        } catch (Exception ex) {
            System.out.println("Failed to parse validation errors: " + ex.getMessage());
        }

        return errors;
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 450);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Вход");
        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка загрузки окна входа", Alert.AlertType.ERROR);
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
    private void handleBack() {
        goToLogin();
    }

    private void showError(String message) {
        // Безопасный показ ошибки
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            // Если errorLabel не инициализирован, используем alert
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}

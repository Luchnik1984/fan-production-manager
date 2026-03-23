package com.fanproduction.gui.controller;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.dto.ProfileDto;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.exception.ValidationException;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.SafeExecutor;
import com.fanproduction.services.UserService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ProfileController {

    @FXML
    private Label emailLabel;
    @FXML
    private Label roleLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label createdAtLabel;
    @FXML
    private Label lastLoginLabel;

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField phoneField;

    @FXML
    private Label firstNameErrorLabel;
    @FXML
    private Label lastNameErrorLabel;
    @FXML
    private Label phoneErrorLabel;
    @FXML
    private Label messageLabel;

    private SpringContextProvider springContext;
    private UserService userService;
    private Stage stage;
    private ProfileDto profile;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private Map<String, Consumer<String>> errorHandlers;

    public void setSpringContext(SpringContextProvider springContext) {
        this.springContext = springContext;
        this.userService = springContext.getBean(UserService.class);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        initErrorHandlers();
        setupFieldListeners();
        Platform.runLater(this::loadProfile);
    }

    private void initErrorHandlers() {
        errorHandlers = new HashMap<>();
        errorHandlers.put("firstName", msg -> Platform.runLater(() -> firstNameErrorLabel.setText(msg)));
        errorHandlers.put("lastName", msg -> Platform.runLater(() -> lastNameErrorLabel.setText(msg)));
        errorHandlers.put("phone", msg -> Platform.runLater(() -> phoneErrorLabel.setText(msg)));
    }

    private void setupFieldListeners() {
        Consumer<TextField> setupCleanup = field ->
                field.textProperty().addListener((obs, old, val) -> {
                    clearErrorLabels();
                    clearMessage();
                });

        setupCleanup.accept(firstNameField);
        setupCleanup.accept(lastNameField);
        setupCleanup.accept(phoneField);
    }

    private void clearErrorLabels() {
        firstNameErrorLabel.setText("");
        lastNameErrorLabel.setText("");
        phoneErrorLabel.setText("");
    }

    private void clearMessage() {
        messageLabel.setText("");
    }

    private String getStatusStyle(UserStatus status) {
        return switch (status) {
            case ACTIVE -> "-fx-text-fill: green; -fx-font-weight: bold;";
            case PENDING -> "-fx-text-fill: orange; -fx-font-weight: bold;";
            case REJECTED -> "-fx-text-fill: red;";
            case BLOCKED -> "-fx-text-fill: gray;";
        };
    }

    private void loadProfile() {
        if (SessionContext.getCurrentUser() == null) {
            showError("Пользователь не авторизован");
            return;
        }

        SafeExecutor.execute(() -> {
            String email = SessionContext.getCurrentUser().getEmail();
            profile = userService.getCurrentUserProfile(email);

            if (profile == null) {
                Platform.runLater(() -> showError("Не удалось загрузить данные профиля"));
                return;
            }

            Platform.runLater(() -> {
                emailLabel.setText(profile.email());
                roleLabel.setText(profile.role().toString());
                statusLabel.setStyle(getStatusStyle(profile.status()));
                statusLabel.setText(profile.status().toString());
                createdAtLabel.setText(formatDate(profile.createdAt()));
                lastLoginLabel.setText(formatDate(profile.lastLoginAt()));
                firstNameField.setText(profile.firstName() != null ? profile.firstName() : "");
                lastNameField.setText(profile.lastName() != null ? profile.lastName() : "");
                phoneField.setText(profile.phone() != null ? profile.phone() : "");
            });
        }, e -> showError("Не удалось загрузить данные профиля"));
    }

    private String formatDate(java.time.LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : "—";
    }

    @FXML
    private void handleSaveProfile() {
        clearErrorLabels();
        clearMessage();

        if (profile == null) {
            showError("Данные профиля не загружены");
            return;
        }

        SafeExecutor.execute(() -> {
            userService.updateProfile(
                    profile.email(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    phoneField.getText().trim()
            );
            Platform.runLater(() -> {
                showSuccess("Профиль успешно обновлён");
                loadProfile();
            });
        }, this::handleValidationError);
    }

    private void handleValidationError(Exception e) {
        if (e instanceof ValidationException) {
            String errorMessage = e.getMessage();
            errorHandlers.forEach((field, handler) -> {
                if (errorMessage.toLowerCase().contains(field.toLowerCase())) {
                    handler.accept("Некорректное " + getFieldName(field));
                }
            });

            if (firstNameErrorLabel.getText().isEmpty() && lastNameErrorLabel.getText().isEmpty() && phoneErrorLabel.getText().isEmpty()) {
                showError("Ошибка валидации: " + errorMessage);
            }
        } else if (e instanceof IllegalArgumentException) {
            showError(e.getMessage());
        } else {
            showError("Не удалось сохранить изменения. Попробуйте позже.");
        }
    }

    private String getFieldName(String field) {
        return switch (field) {
            case "firstName" -> "имя";
            case "lastName" -> "фамилия";
            case "phone" -> "телефон";
            default -> field;
        };
    }

    @FXML
    private void handleChangePassword() {
        if (profile == null) {
            showError("Данные профиля не загружены");
            return;
        }
        ChangePasswordController.showDialog(stage, springContext, profile.email());
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }
}
package com.fanproduction.gui.controller;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.entity.UserEntity;
import com.fanproduction.core.enums.UserStatus;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.MainWindow;
import com.fanproduction.services.UserService;
import javafx.application.Platform;
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
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setText("");
            errorLabel.setStyle("");
        });
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorLabel.setText("");
            errorLabel.setStyle("");
        });
    }


    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        System.out.println("=== LOGIN DEBUG ===");
        System.out.println("Email entered: " + email);
        System.out.println("Password entered: " + password);

        if (email.isEmpty() || password.isEmpty()) {
            Platform.runLater(() -> {
                errorLabel.setText("Email и пароль не могут быть пустыми");
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            });
            return;
        }

        try {
            UserEntity user = userService.getUserByEmail(email);
            System.out.println("User found: " + (user != null ? user.getEmail() : "null"));

            if (user != null) {
                System.out.println("User status: " + user.getStatus());
                System.out.println("User role: " + user.getRole());
            }

            if (user == null) {
                System.out.println("Case: User not found");
                Platform.runLater(() -> {
                    errorLabel.setText("Пользователь с таким email не найден");
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                });
                passwordField.clear();
                return;
            }

            if (user.getStatus() != UserStatus.ACTIVE) {
                System.out.println("Case: User status is " + user.getStatus() + ", not ACTIVE");

                Platform.runLater(() -> {
                    switch (user.getStatus()) {
                        case PENDING:
                            System.out.println("Showing PENDING message");
                            errorLabel.setText("⏳ Ваша регистрация ожидает подтверждения администратором.\nПосле подтверждения вы сможете войти в систему.");
                            errorLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            break;
                        case REJECTED:
                            System.out.println("Showing REJECTED message");
                            errorLabel.setText("❌ Ваша регистрация отклонена.\nПричина: " +
                                    (user.getRejectionReason() != null ? user.getRejectionReason() : "не указана"));
                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        case BLOCKED:
                            System.out.println("Showing BLOCKED message");
                            errorLabel.setText("🔒 Ваш аккаунт заблокирован.\nОбратитесь к администратору.");
                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                        default:
                            errorLabel.setText("Доступ запрещён. Статус аккаунта: " + user.getStatus());
                            errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            break;
                    }
                });

                passwordField.clear();
                return;
            }

            System.out.println("Case: User ACTIVE, checking password");
            if (userService.authenticate(email, password)) {
                System.out.println("Password correct, login successful");
                if (rememberMeCheckBox.isSelected()) {
                    UserPreferences.saveRememberData(email, password);
                } else {
                    UserPreferences.saveLastEmail(email);
                }

                SessionContext.setCurrentUser(user);
                openMainWindow();
            } else {
                System.out.println("Password incorrect");
                Platform.runLater(() -> {
                    errorLabel.setText("Неверный пароль");
                    errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                });
                passwordField.clear();
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                errorLabel.setText("Ошибка при входе: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            });
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
            String email = emailField.getText().trim();

            // Получаем полного пользователя из БД
            UserEntity currentUser = userService.getUserByEmail(email);
            if (currentUser == null) {
                errorLabel.setText("Пользователь не найден");
                return;
            }

            // Сохраняем в сессию
            SessionContext.setCurrentUser(currentUser);

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

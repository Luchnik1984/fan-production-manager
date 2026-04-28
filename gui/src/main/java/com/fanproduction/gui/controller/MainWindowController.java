package com.fanproduction.gui.controller;

import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.UserDto;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Setter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Контроллер главного окна приложения.
 * Управляет TabPane с основными разделами и статус-баром.
 */
public class MainWindowController {

    @FXML
    private BorderPane root;

    @FXML
    private Label statusLabel;

    // Элементы статус-бара (создаются в коде)
    private Label connectionStatusLabel;
    private Label timeLabel;

    @Setter
    private Stage stage;
    private String currentUserRole;
    private String currentUserEmail;

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private Timeline timeUpdater;


    /**
     * Устанавливает роль текущего пользователя и обновляет вкладки.
     */
    public void setCurrentUserRole(String role) {
        this.currentUserRole = role;

        refreshTabs();
    }

    /**
     * Устанавливает email текущего пользователя и обновляет статус-бар.
     */
    public void setCurrentUserEmail(String email) {
        this.currentUserEmail = email;
        updateStatusBarUserInfo();
    }

    @FXML
    private void initialize() {

        createStatusBar();

        createPlaceholderTabs();

        startTimeUpdater();

        checkApiConnection();

        if (currentUserEmail == null) {
            loadCurrentUserInfo();
        } else {
            updateStatusBarUserInfo();
        }
    }

    /**
     * Создает временные вкладки до установки роли.
     */
    private void createPlaceholderTabs() {
        TabPane tabPane = new TabPane();

        Tab placeholderTab = new Tab("Загрузка...");
        placeholderTab.setClosable(false);

        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        content.getChildren().add(new Label("Загрузка данных пользователя..."));
        placeholderTab.setContent(content);

        tabPane.getTabs().add(placeholderTab);
        root.setCenter(tabPane);
    }

    /**
     * Обновляет вкладки после установки роли.
     */
    private void refreshTabs() {
        if (root != null && currentUserRole != null) {
            TabPane tabPane = createTabPane();
            root.setCenter(tabPane);
        }
    }

    /**
     * Создает TabPane с вкладками в зависимости от роли.
     */
    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        // Вкладка "Главная" — доступна всем
        Tab homeTab = createHomeTab();
        tabPane.getTabs().add(homeTab);

        // Вкладка "Журнал" — доступна всем
        Tab journalTab = createPlaceholderTab("Журнал", "Производственный журнал");
        tabPane.getTabs().add(journalTab);

        // Вкладка "Документы" — доступна всем
        Tab documentsTab = createPlaceholderTab("Документы", "Генерация ТЗ, паспортов, табличек");
        tabPane.getTabs().add(documentsTab);

        // Вкладка "Справочники" — доступна всем
        Tab referencesTab = createPlaceholderTab("Справочники", "Электродвигатели, материалы, сертификаты");
        tabPane.getTabs().add(referencesTab);

        // Вкладка "Карточки" — доступна всем
        Tab catalogTab = createCatalogTab();
        tabPane.getTabs().add(catalogTab);

        // Вкладка "Модерация" — ТОЛЬКО ДЛЯ ADMIN
        if ("ADMIN".equals(currentUserRole)) {
            Tab moderationTab = createModerationTab();
            tabPane.getTabs().add(moderationTab);
        }

        // Вкладка "Журнал аудита" — ТОЛЬКО ДЛЯ ADMIN
        if ("ADMIN".equals(currentUserRole)) {
             Tab auditTab = createAuditTab();
             tabPane.getTabs().add(auditTab);
         }


        return tabPane;
    }

    /**
     * Создает приветственную вкладку "Главная".
     */
    private Tab createHomeTab() {
        Tab tab = new Tab("Главная");
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Label welcomeLabel = new Label("Добро пожаловать в Fan Production Manager!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Выберите раздел в меню выше для начала работы.");
        infoLabel.setStyle("-fx-font-size: 14px;");

        content.getChildren().addAll(welcomeLabel, infoLabel);
        tab.setContent(content);

        return tab;
    }

    /**
     * Создает вкладку-заглушку для будущих разделов.
     */
    private Tab createPlaceholderTab(String title, String description) {
        Tab tab = new Tab(title);
        tab.setClosable(false);

        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px;");

        Label placeholderLabel = new Label("⚙️ Раздел находится в разработке");
        placeholderLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");

        content.getChildren().addAll(titleLabel, descLabel, placeholderLabel);
        tab.setContent(content);

        return tab;
    }

    /**
     * Создает вкладку модерации (только для ADMIN).
     */
    private Tab createModerationTab() {
        Tab tab = new Tab("Модерация");
        tab.setClosable(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ModerationView.fxml"));
            Parent content = loader.load();

            ModerationController controller = loader.getController();
            controller.setStage(stage);

            tab.setContent(content);

        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20px;");
            errorBox.getChildren().add(new Label("Ошибка загрузки модуля модерации: " + e.getMessage()));
            tab.setContent(errorBox);
        }

        return tab;
    }

    /**
     * Создает статус-бар в нижней части окна.
     */
    private void createStatusBar() {
        javafx.scene.layout.HBox statusBar = new javafx.scene.layout.HBox();
        statusBar.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 5px;");
        statusBar.setSpacing(20);
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-font-weight: bold;");
        statusLabel.setText("Загрузка...");

        connectionStatusLabel = new Label();
        connectionStatusLabel.setStyle("-fx-font-weight: bold;");

        timeLabel = new Label();
        timeLabel.setStyle("-fx-font-family: monospace;");

        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        statusBar.getChildren().addAll(statusLabel, connectionStatusLabel, spacer, timeLabel);

        root.setBottom(statusBar);
    }

    /**
     * Обновляет информацию о пользователе в статус-баре.
     */
    private void updateStatusBarUserInfo() {
        if (statusLabel != null) {
            if (currentUserEmail != null && currentUserRole != null) {
                String roleDisplay = switch (currentUserRole) {
                    case "ADMIN" -> "Администратор";
                    case "ENGINEER" -> "Инженер";
                    case "MANAGER" -> "Менеджер";
                    default -> currentUserRole;
                };
                statusLabel.setText("👤 " + currentUserEmail + " | Роль: " + roleDisplay);
            } else {
                statusLabel.setText("👤 Пользователь не загружен");
            }
        }
    }

    /**
     * Запускает таймер для обновления времени.
     */
    private void startTimeUpdater() {
        timeUpdater = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> updateCurrentTime())
        );
        timeUpdater.setCycleCount(Animation.INDEFINITE);
        timeUpdater.play();
    }

    /**
     * Обновляет отображение текущего времени.
     */
    private void updateCurrentTime() {
        if (timeLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            timeLabel.setText("🕐 " + now.format(TIME_FORMATTER));
        }
    }

    /**
     * Проверяет подключение к API.
     */
    private void checkApiConnection() {
        new Thread(() -> {
            try {
                com.fasterxml.jackson.core.type.TypeReference<ApiResponse<Object>> typeRef =
                        new com.fasterxml.jackson.core.type.TypeReference<>() {};

                ApiClient.get("/test/ping", typeRef);
                Platform.runLater(() -> updateConnectionStatus(true));

            } catch (Exception e) {
                Platform.runLater(() -> updateConnectionStatus(false));
            }
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(30000);
                checkApiConnection();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    /**
     * Обновляет статус подключения.
     */
    private void updateConnectionStatus(boolean isConnected) {
        if (connectionStatusLabel != null) {
            if (isConnected) {
                connectionStatusLabel.setText("🔌 API: ONLINE");
                connectionStatusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            } else {
                connectionStatusLabel.setText("🔌 API: OFFLINE");
                connectionStatusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            }
        }
    }

    /**
     * Загружает информацию о текущем пользователе через API.
     */
    private void loadCurrentUserInfo() {
        new Thread(() -> {
            try {
                com.fasterxml.jackson.core.type.TypeReference<ApiResponse<UserDto>> typeRef =
                        new com.fasterxml.jackson.core.type.TypeReference<>() {};

                ApiResponse<UserDto> response =
                        ApiClient.get("/users/me", typeRef);

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        UserDto user = response.getData();
                        currentUserEmail = user.getEmail();
                        currentUserRole = user.getRole();

                        updateStatusBarUserInfo();
                        refreshTabs();  // Обновляем вкладки после получения роли
                    } else {
                        statusLabel.setText("Ошибка загрузки профиля: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка подключения к серверу: " + e.getMessage());
                    updateConnectionStatus(false);
                });
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Создает вкладку журнала аудита (только для ADMIN)
     */
    private Tab createAuditTab() {
        Tab tab = new Tab("Журнал аудита");
        tab.setClosable(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/AuditView.fxml"));
            Parent content = loader.load();

            // Сохраняем ссылку на контроллер
            AuditController controller = loader.getController();

            // Добавляем слушатель на выделение вкладки
            tab.setOnSelectionChanged(event -> {
                if (tab.isSelected() && controller != null) {
                    // При переключении на вкладку аудита обновляем данные
                    controller.refresh();
                }
            });

            tab.setContent(content);

        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20px;");
            errorBox.getChildren().add(new Label("Ошибка загрузки журнала аудита: " + e.getMessage()));
            tab.setContent(errorBox);
        }

        return tab;
    }

    /**
     * Создаёт вкладку каталога продукции
     */
    private Tab createCatalogTab() {
        Tab tab = new Tab("Карточки");
        tab.setClosable(false);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/CatalogView.fxml"));
            Parent content = loader.load();
            tab.setContent(content);
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.setStyle("-fx-padding: 20px;");
            errorBox.getChildren().add(new Label("Ошибка загрузки каталога: " + e.getMessage()));
            tab.setContent(errorBox);
        }

        return tab;
    }

    @FXML
    private void handleOpenProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ProfileView.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 500, 650);
            Stage profileStage = new Stage();
            profileStage.setTitle("Мой профиль");
            profileStage.setScene(scene);
            profileStage.initModality(Modality.WINDOW_MODAL);
            profileStage.initOwner(stage);
            profileStage.show();

        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка открытия профиля: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {

        // Очищаем сохранённые данные "Запомнить меня"
        UserPreferences.clearRememberData();

        if (timeUpdater != null) {
            timeUpdater.stop();
        }

        ApiClient.clearAuthToken();
        stage.close();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            Stage loginStage = new Stage();
            controller.setPrimaryStage(loginStage);

            Scene scene = new Scene(root, 400, 450);
            loginStage.setScene(scene);
            loginStage.setTitle("Вход");
            loginStage.show();

        } catch (IOException e) {
            showAlert("Ошибка", "Ошибка открытия окна входа: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExit() {
        if (timeUpdater != null) {
            timeUpdater.stop();
        }
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        showAlert("О программе",
                """
                        Fan Production Manager
                        Версия 2.0 (API Edition)
                        
                        Система автоматизации производства вентиляторов
                        © 2024""",
                Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
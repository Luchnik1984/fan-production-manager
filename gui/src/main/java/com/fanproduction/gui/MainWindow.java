package com.fanproduction.gui;

import com.fanproduction.core.context.SessionContext;
import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.EnvLoader;
import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.controller.AuditController;
import com.fanproduction.gui.controller.LoginController;
import com.fanproduction.gui.controller.ModerationController;
import com.fanproduction.gui.controller.ProfileController;
import com.fanproduction.services.TestService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindow {

    private final SpringContextProvider springContext;
    private Stage primaryStage;

    public MainWindow(SpringContextProvider springContext) {
        this.springContext = springContext;
    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Fan Production Manager");

        BorderPane root = new BorderPane();

        MenuBar menuBar = createMenuBar();
        root.setTop(menuBar);

        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        Label statusLabel = createStatusBar();
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();

        checkDatabaseConnection(statusLabel);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Меню "Файл"
        Menu fileMenu = new Menu("Файл");

        MenuItem profileItem = new MenuItem("Мой профиль");
        profileItem.setOnAction(e -> openProfileWindow());

        MenuItem logoutItem = new MenuItem("Выйти из профиля");
        logoutItem.setOnAction(e -> logout());

        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> {
            springContext.close();
            primaryStage.close();
        });

        fileMenu.getItems().addAll(profileItem, new SeparatorMenuItem(), logoutItem, new SeparatorMenuItem(), exitItem);

        // Меню "Справка"
        Menu helpMenu = new Menu("Справка");

        MenuItem aboutItem = new MenuItem("О программе");
        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().addAll(aboutItem);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    private TabPane createTabPane() {
        TabPane tabPane = new TabPane();

        // Вкладка "Производственный журнал" (доступна всем)
        Tab journalTab = new Tab("Производственный журнал");
        journalTab.setContent(createJournalTabContent());
        journalTab.setClosable(false);
        tabPane.getTabs().add(journalTab);

        // Вкладка "Карточки продукции" (доступна ENGINEER и ADMIN)
        if (isEngineerOrAdmin()) {
            Tab cardsTab = new Tab("Карточки продукции");
            cardsTab.setContent(createCardsTabContent());
            cardsTab.setClosable(false);
            tabPane.getTabs().add(cardsTab);
        }

        // Вкладка "Документы" (доступна ENGINEER и ADMIN)
        if (isEngineerOrAdmin()) {
            Tab documentsTab = new Tab("Документы");
            documentsTab.setContent(createDocumentsTabContent());
            documentsTab.setClosable(false);
            tabPane.getTabs().add(documentsTab);
        }

        // Вкладка "Справочники" (доступна ENGINEER и ADMIN)
        if (isEngineerOrAdmin()) {
            Tab referencesTab = new Tab("Справочники");
            referencesTab.setContent(createReferencesTabContent());
            referencesTab.setClosable(false);
            tabPane.getTabs().add(referencesTab);
        }

        // Вкладка "Модерация пользователей" (только ADMIN)
        if (SessionContext.isAdmin()) {
            Tab moderationTab = new Tab("Модерация пользователей");
            moderationTab.setContent(createModerationTabContent());
            moderationTab.setClosable(false);
            tabPane.getTabs().add(moderationTab);
        }

        // Вкладка "Журнал аудита" (только ADMIN)
        if (SessionContext.isAdmin()) {
            Tab auditTab = new Tab("Журнал аудита");
            auditTab.setContent(createAuditTabContent());
            auditTab.setClosable(false);
            tabPane.getTabs().add(auditTab);
        }

        return tabPane;
    }

    private VBox createJournalTabContent() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px;");

        Label title = new Label("Производственный журнал");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label placeholder = new Label("Здесь будет таблица с записями журнала");
        placeholder.setStyle("-fx-font-style: italic;");

        Button refreshBtn = new Button("Обновить");
        refreshBtn.setOnAction(e -> System.out.println("Журнал: запрос на обновление"));

        vbox.getChildren().addAll(title, placeholder, refreshBtn);
        return vbox;
    }

    private VBox createCardsTabContent() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px;");

        Label title = new Label("Карточки продукции");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label placeholder = new Label("Здесь будет список карточек вентиляторов");
        placeholder.setStyle("-fx-font-style: italic;");

        Button addCardBtn = new Button("Создать карточку");
        addCardBtn.setOnAction(e -> System.out.println("Карточки: создание новой карточки"));

        vbox.getChildren().addAll(title, placeholder, addCardBtn);
        return vbox;
    }

    private VBox createDocumentsTabContent() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px;");

        Label title = new Label("Генерация документов");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label placeholder = new Label("Здесь будут кнопки для генерации ТЗ, паспортов и чертежей");
        placeholder.setStyle("-fx-font-style: italic;");

        Button generateTzBtn = new Button("Сформировать ТЗ");
        generateTzBtn.setOnAction(e -> System.out.println("Документы: генерация ТЗ"));

        vbox.getChildren().addAll(title, placeholder, generateTzBtn);
        return vbox;
    }

    private VBox createReferencesTabContent() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px;");

        Label title = new Label("Справочники");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label placeholder = new Label("Здесь будут справочники (двигатели, материалы, сертификаты)");
        placeholder.setStyle("-fx-font-style: italic;");

        vbox.getChildren().addAll(title, placeholder);
        return vbox;
    }

    private VBox createModerationTabContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ModerationView.fxml"));
            VBox content = loader.load();

            ModerationController controller = loader.getController();
            controller.setSpringContext(springContext);

            return content;
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.getChildren().add(new Label("Ошибка загрузки модуля модерации: " + e.getMessage()));
            return errorBox;
        }
    }

    private Label createStatusBar() {
        Label statusLabel = new Label("Статус: подключение к БД...");
        statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #f0f0f0;");
        return statusLabel;
    }

    private void checkDatabaseConnection(Label statusLabel) {
        try {
            TestService testService = springContext.getBean(TestService.class);
            testService.testDatabaseConnection();

            String dbHost = EnvLoader.get("DB_HOST");
            String dbName = EnvLoader.get("DB_NAME");

            statusLabel.setText("✅ БД: " + dbHost + "/" + dbName);
            statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #e0ffe0;");
        } catch (Exception e) {
            statusLabel.setText("❌ Ошибка: " + e.getMessage());
            statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #ffe0e0;");
            e.printStackTrace();
        }
    }

    private boolean isEngineerOrAdmin() {
        return SessionContext.isAdmin() || SessionContext.isEngineer();
    }

    private void openProfileWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ProfileView.fxml"));
            Parent root = loader.load();

            ProfileController controller = loader.getController();
            controller.setSpringContext(springContext);

            Stage profileStage = new Stage();
            profileStage.setTitle("Мой профиль");
            profileStage.setScene(new Scene(root, 500, 650));
            profileStage.initModality(Modality.WINDOW_MODAL);
            profileStage.initOwner(primaryStage);
            profileStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showError("Ошибка открытия профиля");
        }
    }

    private void logout() {
        // Очищаем все сохранённые данные
        UserPreferences.clearRememberData();

        // Закрываем текущее окно
        primaryStage.close();

        // Открываем окно входа
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController loginController = loader.getController();
            loginController.setSpringContext(springContext);

            Stage loginStage = new Stage();
            loginController.setPrimaryStage(loginStage);

            Scene scene = new Scene(root, 400, 450);
            loginStage.setScene(scene);
            loginStage.setTitle("Fan Production Manager - Вход");
            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Fan Production Manager");
        alert.setContentText("Версия 0.1\n\nСистема управления производством вентиляторов");
        alert.showAndWait();
    }

    private VBox createAuditTabContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/AuditView.fxml"));
            VBox content = loader.load();

            AuditController controller = loader.getController();
            controller.setSpringContext(springContext);

            return content;
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.getChildren().add(new Label("Ошибка загрузки журнала аудита: " + e.getMessage()));
            return errorBox;
        }
    }
}
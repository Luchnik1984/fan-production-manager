package com.fanproduction.gui;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.EnvLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow {

    private final SpringContextProvider springContext;

    public MainWindow(SpringContextProvider springContext) {
        this.springContext = springContext;
    }

    public void start(Stage primaryStage) {
        primaryStage.setTitle("Fan Production Manager");

        BorderPane root = new BorderPane();

        // ========== Верхнее меню ==========
        MenuBar menuBar = createMenuBar(primaryStage);
        root.setTop(menuBar);

        // ========== Центральная область с вкладками ==========
        TabPane tabPane = createTabPane();
        root.setCenter(tabPane);

        // ========== Нижняя панель статуса ==========
        Label statusLabel = createStatusBar();
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Проверяем подключение к БД
        updateDatabaseStatus(statusLabel);
    }

    private MenuBar createMenuBar(Stage primaryStage) {
        MenuBar menuBar = new MenuBar();

        // Меню "Файл"
        Menu fileMenu = new Menu("Файл");

        MenuItem exitItem = new MenuItem("Выход");
        exitItem.setOnAction(e -> {
            springContext.close();
            primaryStage.close();
        });

        fileMenu.getItems().addAll(exitItem);

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

        // Вкладка "Производственный журнал"
        Tab journalTab = new Tab("Производственный журнал");
        journalTab.setContent(createJournalTabContent());
        journalTab.setClosable(false);

        // Вкладка "Карточки продукции"
        Tab cardsTab = new Tab("Карточки продукции");
        cardsTab.setContent(createCardsTabContent());
        cardsTab.setClosable(false);

        // Вкладка "Документы"
        Tab documentsTab = new Tab("Документы");
        documentsTab.setContent(createDocumentsTabContent());
        documentsTab.setClosable(false);

        // Вкладка "Справочники"
        Tab referencesTab = new Tab("Справочники");
        referencesTab.setContent(createReferencesTabContent());
        referencesTab.setClosable(false);

        tabPane.getTabs().addAll(journalTab, cardsTab, documentsTab, referencesTab);

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
        refreshBtn.setOnAction(e ->
                System.out.println("Журнал: запрос на обновление")
        );

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
        addCardBtn.setOnAction(e ->
                System.out.println("Карточки: создание новой карточки")
        );

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
        generateTzBtn.setOnAction(e ->
                System.out.println("Документы: генерация ТЗ")
        );

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

    private Label createStatusBar() {
        Label statusLabel = new Label("Статус: подключение к БД...");
        statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #f0f0f0;");
        return statusLabel;
    }

    private void updateDatabaseStatus(Label statusLabel) {
        try {
            String dbHost = EnvLoader.get("DB_HOST");
            String dbName = EnvLoader.get("DB_NAME");

            statusLabel.setText("✅ Подключено к БД: " + dbHost + "/" + dbName);
            statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #e0ffe0;");
        } catch (Exception e) {
            statusLabel.setText("❌ Ошибка подключения к БД: " + e.getMessage());
            statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #ffe0e0;");
        }
    }

    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Fan Production Manager");
        alert.setContentText("Версия 0.1\n\nСистема управления производством вентиляторов");
        alert.showAndWait();
    }
}
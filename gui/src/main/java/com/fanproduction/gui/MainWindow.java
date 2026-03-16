package com.fanproduction.gui;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.EnvLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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
        Label titleLabel = new Label("Система управления производством вентиляторов");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10px;");
        root.setTop(titleLabel);

        VBox centerBox = new VBox(10);
        centerBox.setStyle("-fx-padding: 20px; -fx-alignment: center;");

        Label welcomeLabel = new Label("Добро пожаловать в приложение!");
        welcomeLabel.setStyle("-fx-font-size: 14px;");

        Label statusLabel = new Label("Статус: Подключение к базе данных...");

        centerBox.getChildren().addAll(welcomeLabel, statusLabel);
        root.setCenter(centerBox);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        checkDatabaseConnection(statusLabel);
    }

    private void checkDatabaseConnection(Label statusLabel) {
        try {
            String dbHost = EnvLoader.get("DB_HOST");
            String dbName = EnvLoader.get("DB_NAME");
            statusLabel.setText("✅ Подключено к базе данных: " + dbHost + "/" + dbName);
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("❌ Ошибка подключения к БД: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
}
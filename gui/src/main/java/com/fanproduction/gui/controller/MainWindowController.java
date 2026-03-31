package com.fanproduction.gui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.UserDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private BorderPane root;

    @FXML
    private Label statusLabel;

    private Stage stage;
    private String currentUserRole;  // ROLE_ADMIN, ROLE_ENGINEER, ROLE_MANAGER

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCurrentUserRole(String role) {
        this.currentUserRole = role;
        initializeTabs(); // после установки роли создаём вкладки
    }

    @FXML
    private void initialize() {
        loadCurrentUser();
    }

    private void initializeTabs() {
        TabPane tabPane = new TabPane();

        // Вкладка "Главная" (для всех)
        Tab homeTab = new Tab("Главная");
        homeTab.setContent(createHomeTabContent());
        homeTab.setClosable(false);
        tabPane.getTabs().add(homeTab);

        // Вкладка "Модерация" (только для ADMIN)
        if ("ADMIN".equals(currentUserRole)) {
            Tab moderationTab = new Tab("Модерация");
            moderationTab.setContent(createModerationTabContent());
            moderationTab.setClosable(false);
            tabPane.getTabs().add(moderationTab);
        }

        root.setCenter(tabPane);
    }

    private VBox createHomeTabContent() {
        VBox vbox = new VBox(10);
        vbox.setStyle("-fx-padding: 20px;");
        Label label = new Label("Добро пожаловать в Fan Production Manager!");
        vbox.getChildren().add(label);
        return vbox;
    }

    private VBox createModerationTabContent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/ModerationView.fxml"));
            VBox content = loader.load();

            ModerationController controller = loader.getController();
            controller.setStage(stage);

            return content;
        } catch (IOException e) {
            e.printStackTrace();
            VBox errorBox = new VBox(10);
            errorBox.getChildren().add(new Label("Ошибка загрузки модуля модерации: " + e.getMessage()));
            return errorBox;
        }
    }

    private void loadCurrentUser() {
        new Thread(() -> {
            try {
                ApiResponse<UserDto> response = ApiClient.get("/users/me",
                        new TypeReference<ApiResponse<UserDto>>() {});

                Platform.runLater(() -> {
                    if (response.isSuccess() && response.getData() != null) {
                        UserDto user = response.getData();
                        statusLabel.setText("Добро пожаловать, " +
                                (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                (user.getLastName() != null ? user.getLastName() : ""));
                    } else {
                        statusLabel.setText("Ошибка загрузки профиля: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Ошибка подключения к серверу: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
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
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        showAlert("О программе", "Fan Production Manager\nВерсия 1.0", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
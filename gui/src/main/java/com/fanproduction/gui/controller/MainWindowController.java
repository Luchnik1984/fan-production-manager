package com.fanproduction.gui.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.client.ApiClient;
import com.fanproduction.gui.dto.ApiResponse;
import com.fanproduction.gui.dto.UserDto;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MainWindowController {

    @FXML
    private BorderPane root;

    @FXML
    private Label statusLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        new Thread(() -> {
            try {
                // Используем TypeReference напрямую
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
    private void handleExit() {
        if (stage != null) {
            stage.close();
        }
        Platform.exit();
    }

    @FXML
    private void handleAbout() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("О программе");
        alert.setHeaderText("Fan Production Manager");
        alert.setContentText("Версия 1.0\n\nСистема управления производством вентиляторов");
        alert.showAndWait();
    }
}
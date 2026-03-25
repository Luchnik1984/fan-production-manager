package com.fanproduction.gui.controller;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.SafeExecutor;
import com.fanproduction.services.UserService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ChangePasswordController {

    public static void showDialog(Stage owner, SpringContextProvider springContext, String email) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle("Смена пароля");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Текущий пароль");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("Новый пароль (мин. 4 символа)");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтверждение пароля");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red;");

        Button saveButton = new Button("Сохранить");
        Button cancelButton = new Button("Отмена");

        grid.add(new Label("Текущий пароль:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("Новый пароль:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Подтверждение:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        grid.add(messageLabel, 1, 3);
        grid.add(saveButton, 0, 4);
        grid.add(cancelButton, 1, 4);

        saveButton.setOnAction(e -> {
            String oldPassword = oldPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                messageLabel.setText("Все поля должны быть заполнены");
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                messageLabel.setText("Новый пароль и подтверждение не совпадают");
                return;
            }

            if (newPassword.length() < 4) {
                messageLabel.setText("Новый пароль должен содержать не менее 4 символов");
                return;
            }

            SafeExecutor.execute(() -> {
                UserService userService = springContext.getBean(UserService.class);
                userService.changePassword(email, oldPassword, newPassword);

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Успешно");
                    alert.setHeaderText(null);
                    alert.setContentText("Пароль успешно изменён");
                    alert.showAndWait();
                    dialog.close();
                });
            }, ex -> Platform.runLater(() -> {
                if (ex instanceof IllegalArgumentException) {
                    messageLabel.setText(ex.getMessage());
                } else {
                    messageLabel.setText("Ошибка при смене пароля. Попробуйте позже.");
                    ex.printStackTrace();
                }
            }));
        });

        cancelButton.setOnAction(e -> dialog.close());

        Scene scene = new Scene(grid, 450, 280);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}

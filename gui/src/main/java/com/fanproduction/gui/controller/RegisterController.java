package com.fanproduction.gui.controller;

import com.fanproduction.core.launcher.SpringContextProvider;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;

@Setter
public class RegisterController {

    private SpringContextProvider springContext;
    private Stage primaryStage;

    @FXML
    private void handleBack() {
        try {
            // Возвращаемся к окну входа
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
            Parent root = loader.load();

            LoginController controller = loader.getController();
            controller.setSpringContext(springContext);
            controller.setPrimaryStage(primaryStage);

            Scene scene = new Scene(root, 400, 400);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Fan Production Manager - Вход");
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

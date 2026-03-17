package com.fanproduction.gui;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.gui.controller.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class LoginApplication extends Application {

    private static SpringContextProvider springContext;

    public static void setSpringContext(SpringContextProvider context) {
        springContext = context;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (springContext == null) {
            throw new IllegalStateException("Spring context not initialized!");
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setSpringContext(springContext);
        controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setTitle("Fan Production Manager - Вход");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}

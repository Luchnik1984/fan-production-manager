package com.fanproduction.gui;

import com.fanproduction.core.launcher.SpringContextProvider;
import com.fanproduction.core.util.UserPreferences;
import com.fanproduction.gui.controller.LoginController;
import com.fanproduction.services.UserService;
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

        // Проверяем, есть ли действительный токен для автоматического входа
        if (UserPreferences.hasValidToken()) {
            String email = UserPreferences.getEmailFromToken();
            UserService userService = springContext.getBean(UserService.class);

            // Пытаемся автоматически войти
            if (userService.autoLogin(email)) {
                // Успешный автовход - открываем главное окно
                openMainWindow(primaryStage);
                return;
            } else {
                // Если автовход не удался, очищаем токен
                UserPreferences.clearRememberData();
            }
        }

        // Если нет токена или автовход не удался, показываем окно входа
        showLoginWindow(primaryStage);
    }

    private void showLoginWindow(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/fanproduction/gui/view/LoginView.fxml"));
        Parent root = loader.load();

        LoginController controller = loader.getController();
        controller.setSpringContext(springContext);
        controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setTitle("Fan Production Manager - Вход");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openMainWindow(Stage primaryStage) {
        try {
            MainWindow mainWindow = new MainWindow(springContext);
            mainWindow.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
            // Если не удалось открыть главное окно, показываем окно входа
            try {
                showLoginWindow(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}

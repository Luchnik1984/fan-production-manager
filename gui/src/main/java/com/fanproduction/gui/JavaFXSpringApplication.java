package com.fanproduction.gui;

import com.fanproduction.core.launcher.SpringContextProvider;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import lombok.Setter;

public class JavaFXSpringApplication extends Application {

    @Setter
    private static SpringContextProvider springContextProvider;

    @Override
    public void start(Stage primaryStage) {
        if (springContextProvider == null) {
            throw new IllegalStateException("SpringContextProvider not set!");
        }
        MainWindow mainWindow = new MainWindow(springContextProvider);
        mainWindow.start(primaryStage);
    }

    @Override
    public void stop() {
        Platform.exit();
    }
}

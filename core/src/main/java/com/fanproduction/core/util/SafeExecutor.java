package com.fanproduction.core.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SafeExecutor {

    private static final Logger log = LoggerFactory.getLogger(SafeExecutor.class);

    public static void execute(Runnable task, Consumer<Exception> errorHandler) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("Error executing task", e);
            Platform.runLater(() -> errorHandler.accept(e));
        }
    }

    public static void execute(Runnable task) {
        execute(task, e -> {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        });
    }

    public static <T> void executeWithResult(Supplier<T> supplier, Consumer<T> successHandler, Consumer<Exception> errorHandler) {
        try {
            T result = supplier.get();
            Platform.runLater(() -> successHandler.accept(result));
        } catch (Exception e) {
            log.error("Error executing task with result", e);
            Platform.runLater(() -> errorHandler.accept(e));
        }
    }

    public static <T> void loadFxml(String fxmlPath, BiConsumer<Parent, T> onSuccess, Consumer<Exception> onError) {
        execute(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(SafeExecutor.class.getResource(fxmlPath));
                Parent root = loader.load();
                T controller = loader.getController();
                Platform.runLater(() -> onSuccess.accept(root, controller));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, onError);
    }
}

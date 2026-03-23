package com.fanproduction.core.exception;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception in thread: " + t.getName(), e);

        // Определяем, является ли поток JavaFX
        boolean isJavaFXThread = t.getName().contains("JavaFX") ||
                t.getName().contains("FX") ||
                Thread.currentThread().getName().contains("JavaFX");

        Platform.runLater(() -> showErrorDialog(e, isJavaFXThread));
    }

    private void showErrorDialog(Throwable e, boolean isJavaFXThread) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        if (isJavaFXThread) {
            alert.setTitle("Ошибка интерфейса");
            alert.setHeaderText("Произошла ошибка в интерфейсе");
        } else {
            alert.setTitle("Критическая ошибка");
            alert.setHeaderText("Произошла непредвиденная ошибка");
        }

        alert.setContentText(getUserFriendlyMessage(e));
        alert.showAndWait();
    }

    private String getUserFriendlyMessage(Throwable e) {
        if (e instanceof IllegalArgumentException) {
            return "Ошибка: " + e.getMessage();
        }
        if (e instanceof ValidationException) {
            return "Ошибка валидации: " + e.getMessage();
        }
        if (e.getMessage() != null && e.getMessage().contains("SQL")) {
            return "Ошибка работы с базой данных. Пожалуйста, обратитесь к администратору.";
        }
        if (e.getMessage() != null && e.getMessage().contains("connection")) {
            return "Потеряно соединение с базой данных. Проверьте подключение.";
        }
        return "Произошла непредвиденная ошибка. Пожалуйста, перезапустите приложение.\n\n" +
                "Если ошибка повторяется, обратитесь в службу поддержки.";
    }

    public static void register() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }
}
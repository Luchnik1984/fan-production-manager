package com.fanproduction.gui.util;

import com.fanproduction.core.exception.ValidationException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        log.error("Uncaught exception in thread: " + t.getName(), e);

        boolean isJavaFXThread = t.getName().contains("JavaFX") ||
                t.getName().contains("FX") ||
                Thread.currentThread().getName().contains("JavaFX");

        Platform.runLater(() -> showErrorDialog(e, isJavaFXThread));
    }

    private void showErrorDialog(Throwable e, boolean isJavaFXThread) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        String message = getUserFriendlyMessage(e);

        if (isJavaFXThread) {
            alert.setTitle("Ошибка интерфейса");
            alert.setHeaderText("Произошла ошибка в интерфейсе");
        } else {
            alert.setTitle("Критическая ошибка");
            alert.setHeaderText("Произошла непредвиденная ошибка");
        }

        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getUserFriendlyMessage(Throwable e) {
        String message = e.getMessage();
        Throwable cause = e.getCause();

        // Ошибки соединения
        if (cause instanceof ConnectException || (message != null && message.contains("Connection refused"))) {
            return "Нет соединения с сервером.\nПожалуйста, проверьте, запущен ли API.";
        }
        if (cause instanceof SocketTimeoutException || (message != null && message.contains("timeout"))) {
            return "Сервер не отвечает.\nПревышено время ожидания ответа.";
        }
        if (cause instanceof UnknownHostException) {
            return "Не удалось найти сервер.\nПроверьте адрес сервера.";
        }
        if (message != null && message.contains("connection")) {
            return "Потеряно соединение с сервером.\nПроверьте подключение и перезапустите приложение.";
        }

        // Ошибки авторизации
        if (message != null && (message.contains("401") || message.contains("Unauthorized"))) {
            return "Сессия истекла. Пожалуйста, войдите заново.";
        }
        if (message != null && (message.contains("403") || message.contains("Forbidden"))) {
            return "У вас нет прав для выполнения этой операции.";
        }

        // Ошибки API
        if (message != null && (message.contains("API error") || message.contains("500"))) {
            return "Ошибка сервера.\nПожалуйста, попробуйте позже.";
        }
        if (message != null && message.contains("429")) {
            return "Слишком много запросов. Подождите немного.";
        }

        // Бизнес-ошибки
        if (e instanceof IllegalArgumentException) {
            return "Ошибка: " + e.getMessage();
        }
        if (e instanceof ValidationException) {
            return "Ошибка валидации: " + e.getMessage();
        }

        // Ошибки БД
        if (message != null && message.contains("SQL")) {
            return "Ошибка работы с базой данных.\nПожалуйста, обратитесь к администратору.";
        }

        // Общая ошибка
        return """
                Произошла непредвиденная ошибка. Пожалуйста, перезапустите приложение.
                
                Если ошибка повторяется, обратитесь в службу поддержки.""";
    }

    public static void register() {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
    }
}
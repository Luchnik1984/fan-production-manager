package com.fanproduction.gui.component;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**
 * Универсальный диалог загрузки с индикатором прогресса.
 * Используется для асинхронной загрузки данных перед открытием окна.
 */
public class LoadingDialog {

    private final Stage dialog;
    private final Stage owner;
    private volatile boolean isLoading = true;

    public LoadingDialog(Stage owner, String title) {
        this.owner = owner;
        this.dialog = new Stage();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(owner);
        dialog.setTitle(title);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setResizable(false);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-alignment: center;");

        ProgressIndicator progress = new ProgressIndicator();
        Label label = new Label("Загрузка данных...");
        label.setStyle("-fx-font-size: 12px;");

        layout.getChildren().addAll(progress, label);
        Scene scene = new Scene(layout, 250, 150);
        dialog.setScene(scene);
    }

    /**
     * Показывает диалог загрузки
     */
    public void show() {
        Platform.runLater(dialog::show);
    }

    /**
     * Закрывает диалог загрузки
     */
    public void close() {
        isLoading = false;
        Platform.runLater(dialog::close);
    }

    /**
     * Асинхронно загружает данные и после завершения вызывает callback
     * @param loader функция загрузки данных
     * @param onSuccess callback при успешной загрузке
     * @param <T> тип результата
     */
    public <T> void loadAsync(Callable<T> loader, Consumer<T> onSuccess) {
        new Thread(() -> {
            try {
                T result = loader.call();
                if (isLoading) {
                    Platform.runLater(() -> {
                        close();
                        onSuccess.accept(result);
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    close();
                    showError("Ошибка загрузки данных: " + e.getMessage());
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

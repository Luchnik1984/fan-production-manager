package com.fanproduction.gui.util;

import com.fanproduction.gui.client.FanTemplateVersionClient;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Утилиты контроллеров конструктора шаблонов (Спринт 9, US9.1–US9.5).
 * <p>
 * Устраняет дублирование между {@code TemplateTreeController},
 * {@code TemplateDetailController} и будущими {@code TemplateEditorController},
 * {@code MarkingRuleEditorController}, {@code TemplatePreviewController}:
 * <ul>
 *   <li>Создание диалогов / алертов ({@link #buildDialog}, {@link #showAlert}, {@link #confirm}).</li>
 *   <li>Асинхронные действия с обратной связью ({@link #runAsync}).</li>
 *   <li>Классификация ошибок «бизнес vs техническая» ({@link #isBusinessError}).</li>
 *   <li>Резолвинг owner-окна из узла ({@link #resolveOwner(Node)}).</li>
 * </ul>
 */
public final class TemplateControllerHelper {

    private static final Logger log = LoggerFactory.getLogger(TemplateControllerHelper.class);

    private TemplateControllerHelper() {
    }

    // ==========================================================
    // OWNER
    // ==========================================================

    /**
     * Получить owner-окно из узла (Scene → Window). Возвращает {@code null},
     * если узел ещё не добавлен в Scene.
     */
    public static Window resolveOwner(Node node) {
        if (node == null) return null;
        return node.getScene() != null ? node.getScene().getWindow() : null;
    }

    // ==========================================================
    // DIALOGS / ALERTS
    // ==========================================================

    /**
     * Создать диалог с кнопками «ОК» и «Отмена». Модальность — WINDOW_MODAL.
     */
    public static Dialog<ButtonType> buildDialog(String title, Window owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.getDialogPane().getButtonTypes().addAll(
                new ButtonType("ОК", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE));
        return dialog;
    }

    /**
     * Стандартный GridPane для форм: hgap 10, vgap 10, padding 20.
     */
    public static GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));
        return grid;
    }

    /**
     * Показать подтверждение. Возвращает {@code true}, если пользователь
     * нажал «ОК».
     */
    public static boolean confirm(Window owner, String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    /**
     * Подтверждение удаления. Заголовок «Подтверждение удаления», контент
     * «Это действие необратимо.» + {@code additionalHint} (если задан).
     */
    public static boolean confirmDelete(Window owner, String header, String additionalHint) {
        StringBuilder content = new StringBuilder("Это действие необратимо.");
        if (additionalHint != null && !additionalHint.isBlank()) {
            content.append("\n").append(additionalHint);
        }
        return confirm(owner, "Подтверждение удаления", header, content.toString());
    }

    /**
     * Показать алерт (ERROR / WARNING / INFORMATION / CONFIRMATION).
     * <p>
     * Для длинных сообщений (список ошибок валидации и т.п.) диалог
     * делается resizable, с минимальной шириной 500px и переносом строк —
     * чтобы текст не обрезался.
     */
    public static void showAlert(Window owner, String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Длинные сообщения (валидация) — делаем читаемыми
        if (message != null && message.length() > 80) {
            alert.setResizable(true);
            alert.getDialogPane().setMinWidth(600);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        }

        if (owner != null) {
            alert.initOwner(owner);
            alert.initModality(Modality.WINDOW_MODAL);
        }
        alert.showAndWait();
    }

    // ==========================================================
    // ASYNC
    // ==========================================================

    /**
     * Выполнить асинхронное действие (сетевая операция) в фоновом потоке.
     * <p>
     * При успехе:
     * <ul>
     *   <li>логируется {@code successMessage};</li>
     *   <li>В FX-потоке вызывается {@code onSuccess} (если не null).</li>
     * </ul>
     * <p>
     * При ошибке в FX-потоке показывается алерт:
     * {@code WARNING} для бизнес-ошибок (см. {@link #isBusinessError}),
     * {@code ERROR} для технических.
     * <p>
     * <b>Важно:</b> {@code action} должна содержать только сетевой вызов.
     * Перезагрузка UI должна быть в {@code onSuccess}.
     */
    public static void runAsync(Window owner,
                                ThrowingRunnable action,
                                String successMessage,
                                Runnable onSuccess) {
        new Thread(() -> {
            try {
                action.run();
                Platform.runLater(() -> {
                    log.info(successMessage);
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                });
            } catch (Exception e) {
                log.warn("{} — ошибка: {}", successMessage, e.getMessage());
                Platform.runLater(() -> {
                    Alert.AlertType type = isBusinessError(e.getMessage())
                            ? Alert.AlertType.WARNING
                            : Alert.AlertType.ERROR;
                    String title = type == Alert.AlertType.WARNING
                            ? "Действие невозможно"
                            : "Ошибка";
                    showAlert(owner, title,
                            e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка",
                            type);
                });
            }
        }, "TemplateController-action").start();
    }

    /**
     * Является ли ошибка бизнес-ограничением (400/409), а не технической?
     * <p>
     * Используется для выбора {@code AlertType.WARNING} vs {@code ERROR}.
     */
    public static boolean isBusinessError(String message) {
        if (message == null) return false;
        String lower = message.toLowerCase();
        return lower.contains("уже существует")
                || lower.contains("нельзя")
                || lower.contains("невозможно")
                || lower.contains("не найден")
                || lower.contains("не найдена")
                || lower.contains("только")
                || lower.contains("уже есть");
    }

    /**
     * Асинхронно проверить DRAFT-версию шаблона через сервер ({@code /validate}).
     * <p>
     * Управляет состоянием кнопки проверки (disable → «Проверка…» → восстановление)
     * и показывает результат:
     * <ul>
     *   <li>список ошибок пуст → INFO «Ошибок не найдено».</li>
     *   <li>список ошибок не пуст → WARNING «Найдены ошибки: …».</li>
     *   <li>исключение → ERROR «Не удалось выполнить валидацию».</li>
     * </ul>
     * <p>
     * {@code onFinally} вызывается после восстановления кнопки и статуса
     * (в FX-потоке) — например, чтобы выставить «Блоков: N» / «Полей: N».
     *
     * @param owner         owner-окно
     * @param validateButton кнопка «Проверить» (для disable/enable)
     * @param versionId     ID DRAFT-версии
     * @param baseStatus    базовый статус (например, «Блоков: 5»), восстанавливается после проверки
     * @param onStatusChange колбэк для установки статуса (null — не менять)
     * @param onFinally     колбэк после завершения (null — не вызывать)
     */
    public static void validateVersionAsync(Window owner,
                                            Button validateButton,
                                            Long versionId,
                                            String baseStatus,
                                            java.util.function.Consumer<String> onStatusChange,
                                            Runnable onFinally) {
        if (validateButton != null) validateButton.setDisable(true);
        if (onStatusChange != null) onStatusChange.accept("Проверка…");

        new Thread(() -> {
            try {
                List<String> errors = FanTemplateVersionClient.validateVersion(versionId);
                javafx.application.Platform.runLater(() -> {
                    if (validateButton != null) validateButton.setDisable(false);
                    if (onStatusChange != null) onStatusChange.accept(baseStatus);

                    if (errors == null || errors.isEmpty()) {
                        showAlert(owner, "Валидация",
                                "Ошибок не найдено. Шаблон готов к публикации.",
                                Alert.AlertType.INFORMATION);
                    } else {
                        StringBuilder sb = new StringBuilder("Найдены ошибки:\n");
                        for (String err : errors) sb.append("• ").append(err).append("\n");
                        showAlert(owner, "Ошибки валидации", sb.toString(),
                                Alert.AlertType.WARNING);
                    }
                    if (onFinally != null) onFinally.run();
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    if (validateButton != null) validateButton.setDisable(false);
                    if (onStatusChange != null) onStatusChange.accept(baseStatus);
                    showAlert(owner, "Ошибка",
                            "Не удалось выполнить валидацию: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                    if (onFinally != null) onFinally.run();
                });
            }
        }, "TemplateController-validate").start();
    }

    // ==========================================================
    // FUNCTIONAL
    // ==========================================================

    /**
     * Функциональный интерфейс для {@link #runAsync}. Позволяет бросать
     * checked-исключения (методы клиентов объявлены как {@code throws Exception}).
     */
    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }
}

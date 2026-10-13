package com.fanproduction.gui.configurator;

import com.fanproduction.gui.client.FanTemplateVersionClient;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Конфигуратор FAN-карточки.
 * <p>
 * Особенности:
 * <ul>
 *   <li>Real-time пересчёт маркировки через REST preview (debounce 400 мс).</li>
 *   <li>Защита от перезаписи ручных правок.</li>
 *   <li>Флаг {@code initializing} блокирует preview во время загрузки existing-карточки.</li>
 *   <li>{@link #markInitialized()} вызывается из {@code CardFormController} после
 *       заполнения формы — с этого момента preview активен.</li>
 * </ul>
 */
@Slf4j
public class FanCardConfigurator implements CardFieldConfigurator {

    private static final long DEBOUNCE_MS = 400;

    @Setter
    private Long templateVersionId;

    /** Последнее автоматически сгенерированное значение маркировки. */
    private String lastAutoMarking = "";

    /** Блокировка preview во время инициализации (загрузки existing-карточки). */
    private volatile boolean initializing = false;

    /** Debounce-таймер. */
    private PauseTransition debounceTimer;

    /** Сохранённая ссылка на поле fullMarking (для markInitialized). */
    private TextField fullMarkingFieldRef;

    // ==========================================================
    // SETUP
    // ==========================================================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // При existing-карточке — блокируем preview до markInitialized()
        if (existingCardExists) {
            initializing = true;
        }

        // Сохраняем ссылку на поле fullMarking (если есть)
        Node fmNode = fieldControls.get("fullMarking");
        if (fmNode instanceof TextField tf) {
            fullMarkingFieldRef = tf;
        }

        // Вешаем listeners на все поля, кроме fullMarking и name
        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String fieldName = entry.getKey();
            if ("fullMarking".equals(fieldName)) continue;
            if ("name".equals(fieldName)) continue;

            Node control = entry.getValue();
            if (control instanceof TextField tf) {
                tf.textProperty().addListener((obs, o, n) -> onFieldChanged(fieldControls));
            } else if (control instanceof ComboBox<?> cb) {
                cb.valueProperty().addListener((obs, o, n) -> onFieldChanged(fieldControls));
            } else if (control instanceof CheckBox ch) {
                ch.selectedProperty().addListener((obs, o, n) -> onFieldChanged(fieldControls));
            }
        }
    }

    /**
     * Вызывается из {@code CardFormController} после заполнения формы.
     * Сбрасывает флаг {@code initializing}, запоминает текущее значение
     * маркировки как {@code lastAutoMarking}.
     */
    public void markInitialized() {
        if (!initializing) return;

        lastAutoMarking = fullMarkingFieldRef != null && fullMarkingFieldRef.getText() != null
                ? fullMarkingFieldRef.getText()
                : "";
        initializing = false;

        log.debug("FanCardConfigurator: initialized, lastAutoMarking='{}'", lastAutoMarking);
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ
    // ==========================================================

    @Override
    public void refreshFullMarking(Map<String, Node> fieldControls) {
        recalcMarking(fieldControls, false);
    }

    @Override
    public void forceSetFullMarking(Map<String, Node> fieldControls) {
        recalcMarking(fieldControls, true);
    }

    @Override
    public boolean validate(Map<String, Node> fieldControls,
                            Map<String, Object> fields,
                            Map<String, Label> fieldLabels) {
        String fullMarking = (String) fields.get("fullMarking");
        if (fullMarking == null || fullMarking.trim().isEmpty()) {
            showValidationError(
                    "Полная маркировка не может быть пустой. "
                            + "Заполните поля или нажмите «Восстановить маркировку».");
            return false;
        }
        return true;
    }

    // ==========================================================
    // DEBOUNCE + ПЕРЕСЧЁТ
    // ==========================================================

    private void onFieldChanged(Map<String, Node> fieldControls) {
        if (initializing) return;  // блокировка во время загрузки
        schedulePreview(fieldControls);
    }

    private void schedulePreview(Map<String, Node> fieldControls) {
        if (debounceTimer != null) {
            debounceTimer.stop();
        }
        debounceTimer = new PauseTransition(Duration.millis(DEBOUNCE_MS));
        debounceTimer.setOnFinished(e -> recalcMarking(fieldControls, false));
        debounceTimer.play();
    }

    private void recalcMarking(Map<String, Node> fieldControls, boolean force) {
        if (templateVersionId == null) {
            log.warn("templateVersionId не установлен — preview невозможен");
            return;
        }

        TextField localField = fullMarkingFieldRef;
        if (localField == null) {
            Node node = fieldControls.get("fullMarking");
            if (node instanceof TextField tf) {
                localField = tf;
                fullMarkingFieldRef = tf;
            }
        }
        if (localField == null) return;

        final TextField fullMarkingFieldFinal = localField;

        // Проверяем «ручное редактирование»
        String current = fullMarkingFieldFinal.getText();
        boolean manuallyEdited = current != null && !current.isEmpty()
                && !current.equals(lastAutoMarking);

        if (manuallyEdited && !force) {
            log.debug("Поле fullMarking было отредактировано вручную — не перезаписываем");
            return;
        }

        Map<String, Object> values = collectValues(fieldControls, "fullMarking");

        new Thread(() -> {
            try {
                String newMarking = FanTemplateVersionClient.previewMarking(templateVersionId, values);
                if (newMarking == null) newMarking = "";

                final String finalMarking = newMarking;
                Platform.runLater(() -> {
                    String now = fullMarkingFieldFinal.getText();
                    boolean stillManuallyEdited = now != null && !now.isEmpty()
                            && !now.equals(lastAutoMarking);

                    if (force || !stillManuallyEdited) {
                        fullMarkingFieldFinal.setText(finalMarking);
                        lastAutoMarking = finalMarking;
                    }
                });
            } catch (Exception e) {
                log.warn("Ошибка preview маркировки: {}", e.getMessage());
            }
        }).start();
    }

    // ==========================================================
    // СБОР ЗНАЧЕНИЙ
    // ==========================================================

    private Map<String, Object> collectValues(Map<String, Node> fieldControls, String excludeKey) {
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String key = entry.getKey();
            if (key.equals(excludeKey)) continue;
            if ("name".equals(key)) continue;

            Object value = extractValue(entry.getValue());
            if (value != null) {
                result.put(key, value);
            }
        }
        return result;
    }

    private Object extractValue(Node control) {
        if (control instanceof TextField tf) {
            String text = tf.getText() != null ? tf.getText().trim() : "";
            return text.isEmpty() ? null : text;
        }
        if (control instanceof ComboBox<?> cb) {
            return cb.getValue();
        }
        if (control instanceof CheckBox ch) {
            return ch.isSelected();
        }
        return null;
    }
}
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
 * Отвечает за:
 * <ul>
 *   <li>Real-time пересчёт полной маркировки через REST-preview
 *       (при изменении полей, с debounce).</li>
 *   <li>Защиту от перезаписи вручную отредактированной маркировки.</li>
 *   <li>Кнопку «Восстановить маркировку» (принудительный пересчёт).</li>
 *   <li>Валидацию непустой маркировки при сохранении.</li>
 * </ul>
 * <p>
 * По логике совпадает с {@code MotorCardConfigurator},
 * {@code RadialWheelCardConfigurator} и др., но источник маркировки —
 * не Java-код, а сервер ({@code MarkingEngine}).
 */
@Slf4j
public class FanCardConfigurator implements CardFieldConfigurator {

    /** Задержка debounce перед вызовом preview (мс). */
    private static final long DEBOUNCE_MS = 400;

    /** ID версии шаблона (устанавливается из CardFormController). */
    @Setter
    private Long templateVersionId;

    /** Последнее автоматически сгенерированное значение маркировки. */
    private String lastAutoMarking = "";

    /** Начальные значения полей (для отслеживания изменений). */
    private Map<String, Object> initialValues = new HashMap<>();

    /** Debounce-таймер. */
    private PauseTransition debounceTimer;

    // ==========================================================
    // SETUP
    // ==========================================================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        // При существующей карточке запоминаем текущее значение маркировки
        if (existingCardExists) {
            TextField fmField = getTextField(fieldControls, "fullMarking");
            if (fmField != null && fmField.getText() != null && !fmField.getText().isEmpty()) {
                lastAutoMarking = fmField.getText();
            }
            initialValues = collectValues(fieldControls, "fullMarking");
        }

        // Вешаем listeners на все поля кроме fullMarking
        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String fieldName = entry.getKey();
            if ("fullMarking".equals(fieldName)) continue;
            if ("name".equals(fieldName)) continue;  // name скрыто, не участвует

            Node control = entry.getValue();
            if (control instanceof TextField tf) {
                tf.textProperty().addListener((obs, o, n) -> schedulePreview(fieldControls));
            } else if (control instanceof ComboBox<?> cb) {
                cb.valueProperty().addListener((obs, o, n) -> schedulePreview(fieldControls));
            } else if (control instanceof CheckBox ch) {
                ch.selectedProperty().addListener((obs, o, n) -> schedulePreview(fieldControls));
            }
        }
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ МЕТОДЫ (для CardFormController)
    // ==========================================================

    @Override
    public void refreshFullMarking(Map<String, Node> fieldControls) {
        // С защитой: не перезаписывает, если пользователь редактировал вручную
        recalcMarking(fieldControls, false);
    }

    @Override
    public void forceSetFullMarking(Map<String, Node> fieldControls) {
        // Кнопка «Восстановить маркировку» — принудительно перезаписывает
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

        TextField fullMarkingField = getTextField(fieldControls, "fullMarking");
        if (fullMarkingField == null) return;

        // Проверяем «ручное редактирование» в момент запуска
        String current = fullMarkingField.getText();
        boolean manuallyEdited = current != null && !current.isEmpty()
                && !current.equals(lastAutoMarking);

        if (manuallyEdited && !force) {
            log.debug("Поле fullMarking было отредактировано вручную — не перезаписываем");
            return;
        }

        // Собираем значения полей
        Map<String, Object> values = collectValues(fieldControls, "fullMarking");

        // Вызов REST в фоне
        new Thread(() -> {
            try {
                String newMarking = FanTemplateVersionClient.previewMarking(templateVersionId, values);
                if (newMarking == null) newMarking = "";

                final String finalMarking = newMarking;
                Platform.runLater(() -> {
                    // Перепроверяем на UI-потоке (мог измениться между вызовами)
                    String now = fullMarkingField.getText();
                    boolean stillManuallyEdited = now != null && !now.isEmpty()
                            && !now.equals(lastAutoMarking);

                    if (force || !stillManuallyEdited) {
                        fullMarkingField.setText(finalMarking);
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

    /**
     * Собрать значения полей формы в Map.
     *
     * @param excludeKey ключ, который нужно исключить (например, fullMarking)
     */
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

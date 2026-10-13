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
 *   <li>Флаг {@code updatingFromPreview} отличает программное обновление
 *       {@code fullMarking} от ручного редактирования.</li>
 *   <li>{@code manualOverride} блокирует автообновление, если пользователь
 *       изменил маркировку вручную или сохранённая маркировка не совпадает
 *       с preview при открытии existing-карточки.</li>
 *   <li>{@code initializing} блокирует listeners во время загрузки формы.</li>
 * </ul>
 */
@Slf4j
public class FanCardConfigurator implements CardFieldConfigurator {

    private static final long DEBOUNCE_MS = 400;

    @Setter
    private Long templateVersionId;

    private String lastAutoMarking = "";
    private volatile boolean manualOverride = false;
    private volatile boolean initializing = false;
    private volatile boolean updatingFromPreview = false;

    private PauseTransition debounceTimer;
    private TextField fullMarkingFieldRef;
    private Map<String, Node> fieldControlsRef;

    // ==========================================================
    // SETUP
    // ==========================================================

    @Override
    public void setupFields(Map<String, Node> fieldControls,
                            Map<String, Label> fieldLabels,
                            Map<String, Label> fieldHints,
                            boolean existingCardExists) {

        this.fieldControlsRef = fieldControls;

        if (existingCardExists) {
            initializing = true;
        }

        Node fmNode = fieldControls.get("fullMarking");
        if (fmNode instanceof TextField tf) {
            fullMarkingFieldRef = tf;
            tf.textProperty().addListener((obs, o, n) -> {
                if (initializing) return;
                if (updatingFromPreview) return;   // ← программное обновление
                if (n != null && !n.equals(lastAutoMarking)) {
                    manualOverride = true;
                    log.debug("MANUAL OVERRIDE: '{}' != '{}'", n, lastAutoMarking);
                }
            });
        }

        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String fieldName = entry.getKey();
            if ("fullMarking".equals(fieldName)) continue;
            if ("name".equals(fieldName)) continue;

            Node control = entry.getValue();
            if (control instanceof TextField tf) {
                tf.textProperty().addListener((obs, o, n) -> onFieldChanged());
            } else if (control instanceof ComboBox<?> cb) {
                cb.valueProperty().addListener((obs, o, n) -> onFieldChanged());
            } else if (control instanceof CheckBox ch) {
                ch.selectedProperty().addListener((obs, o, n) -> onFieldChanged());
            }
        }
    }

    public void markInitialized() {
        if (!initializing) return;

        String currentMarking = fullMarkingFieldRef != null && fullMarkingFieldRef.getText() != null
                ? fullMarkingFieldRef.getText().trim()
                : "";
        lastAutoMarking = currentMarking;
        initializing = false;

        log.info("markInitialized: currentMarking='{}'", currentMarking);

        if (!currentMarking.isEmpty() && templateVersionId != null && fieldControlsRef != null) {
            Map<String, Object> values = collectValues(fieldControlsRef, "fullMarking");
            new Thread(() -> {
                try {
                    String preview = FanTemplateVersionClient.previewMarking(templateVersionId, values);
                    if (preview != null && !preview.equals(currentMarking)) {
                        manualOverride = true;
                        log.info("markInitialized: MANUAL OVERRIDE, preview='{}' != current='{}'",
                                preview, currentMarking);
                    } else {
                        log.debug("markInitialized: маркировка автоматическая");
                    }
                } catch (Exception e) {
                    log.warn("markInitialized: preview failed: {}", e.getMessage());
                }
            }).start();
        }
    }

    // ==========================================================
    // ПУБЛИЧНЫЕ
    // ==========================================================

    @Override
    public void refreshFullMarking(Map<String, Node> fieldControls) {
        recalcMarking(fieldControls, false);
    }

    @Override
    public void forceSetFullMarking(Map<String, Node> fieldControls) {
        manualOverride = false;
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
    // DEBOUNCE
    // ==========================================================

    private void onFieldChanged() {
        if (initializing) return;
        schedulePreview();
    }

    private void schedulePreview() {
        if (debounceTimer != null) debounceTimer.stop();
        debounceTimer = new PauseTransition(Duration.millis(DEBOUNCE_MS));
        debounceTimer.setOnFinished(e -> recalcMarking(fieldControlsRef, false));
        debounceTimer.play();
    }

    private void recalcMarking(Map<String, Node> fieldControls, boolean force) {
        if (templateVersionId == null) return;
        if (fieldControls == null) return;

        if (manualOverride && !force) {
            log.debug("recalcMarking: manualOverride=true, пропуск");
            return;
        }

        TextField fmField = fullMarkingFieldRef;
        if (fmField == null) {
            Node node = fieldControls.get("fullMarking");
            if (node instanceof TextField tf) {
                fmField = tf;
                fullMarkingFieldRef = tf;
            }
        }
        if (fmField == null) return;

        final TextField finalFmField = fmField;
        final Map<String, Object> values = collectValues(fieldControls, "fullMarking");
        final Long versionId = templateVersionId;

        new Thread(() -> {
            try {
                String newMarking = FanTemplateVersionClient.previewMarking(versionId, values);
                if (newMarking == null) newMarking = "";

                final String finalMarking = newMarking;
                Platform.runLater(() -> {
                    if (manualOverride && !force) return;
                    updatingFromPreview = true;
                    try {
                        finalFmField.setText(finalMarking);
                        lastAutoMarking = finalMarking;
                        log.debug("recalcMarking: setText '{}'", finalMarking);
                    } finally {
                        updatingFromPreview = false;
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
            if (value != null) result.put(key, value);
        }
        return result;
    }

    private Object extractValue(Node control) {
        if (control instanceof TextField tf) {
            String text = tf.getText() != null ? tf.getText().trim() : "";
            return text.isEmpty() ? null : text;
        }
        if (control instanceof ComboBox<?> cb) return cb.getValue();
        if (control instanceof CheckBox ch) return ch.isSelected();
        return null;
    }
}
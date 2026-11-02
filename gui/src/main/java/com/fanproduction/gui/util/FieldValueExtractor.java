package com.fanproduction.gui.util;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * Извлечение значения из контрола (TextField / ComboBox / CheckBox).
 * <p>
 * Используется в:
 * <ul>
 *   <li>{@code FanCardConfigurator.collectValues} — для preview-маркировки.</li>
 *   <li>{@code TemplatePreviewController.collectValues} — для предпросмотра шаблона.</li>
 * </ul>
 * <p>
 * Правила:
 * <ul>
 *   <li>TextField → текст; пустая строка → {@code null}.</li>
 *   <li>ComboBox → значение (может быть {@code null}).</li>
 *   <li>CheckBox → boolean (всегда, не {@code null}).</li>
 *   <li>Другие типы → {@code null}.</li>
 * </ul>
 */
public final class FieldValueExtractor {

    private FieldValueExtractor() {
    }

    public static Object extract(Node control) {
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

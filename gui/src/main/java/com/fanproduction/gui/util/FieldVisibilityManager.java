package com.fanproduction.gui.util;

import com.fanproduction.gui.dto.config.ConditionDto;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;

import java.util.HashMap;
import java.util.Map;

/**
 * Управляет видимостью и обязательностью полей формы на основе
 * условий {@code visibleIf} / {@code requiredIf}.
 * <p>
 * Хранит ссылки на контролы, метки и «базовые» (из шаблона) значения
 * обязательности и текста метки. Пересчитывает состояние при каждом
 * изменении полей.
 * <p>
 * Логика:
 * <ul>
 *   <li>{@code visible = visibleIf == null || evaluate(visibleIf) == true}.</li>
 *   <li>{@code required = baseRequired || (requiredIf != null && evaluate(requiredIf))}.</li>
 *   <li>При visible=false: label, control и его родительский контейнер
 *       скрыты и {@code managed=false}.</li>
 *   <li>Метка получает суффикс {@code " *"}, если поле обязательно,
 *       и {@code ":"} — иначе.</li>
 * </ul>
 */
public class FieldVisibilityManager {

    private final Map<String, Node> fieldControls;
    private final Map<String, Label> fieldLabels;
    private final Map<String, ConditionDto> visibleIfMap = new HashMap<>();
    private final Map<String, ConditionDto> requiredIfMap = new HashMap<>();
    private final Map<String, Boolean> baseRequiredMap = new HashMap<>();
    private final Map<String, String> originalLabels = new HashMap<>();

    /**
     * Кэш текущего «эффективного» состояния обязательности.
     * Нужен, чтобы валидация знала, какие поля сейчас обязательны.
     */
    private final Map<String, Boolean> effectiveRequiredMap = new HashMap<>();

    public FieldVisibilityManager(Map<String, Node> fieldControls,
                                  Map<String, Label> fieldLabels) {
        this.fieldControls = fieldControls;
        this.fieldLabels = fieldLabels;
    }

    /**
     * Зарегистрировать поле с условиями.
     *
     * @param key               ключ поля
     * @param visibleIf         условие видимости (может быть {@code null})
     * @param requiredIf        условие обязательности (может быть {@code null})
     * @param baseRequired      обязательность из шаблона (поле.required)
     * @param originalLabel     оригинальный текст метки без суффикса
     */
    public void register(String key,
                         ConditionDto visibleIf,
                         ConditionDto requiredIf,
                         boolean baseRequired,
                         String originalLabel) {
        if (visibleIf != null) visibleIfMap.put(key, visibleIf);
        if (requiredIf != null) requiredIfMap.put(key, requiredIf);
        baseRequiredMap.put(key, baseRequired);
        if (originalLabel != null) {
            originalLabels.put(key, originalLabel);
        }
    }

    /**
     * Пересчитать видимость и обязательность всех полей.
     *
     * @param values текущие значения полей
     */
    public void recalcAll(Map<String, Object> values) {
        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String key = entry.getKey();
            Node control = entry.getValue();
            Label label = fieldLabels.get(key);

            boolean visible = isVisible(key, values);
            boolean required = isRequired(key, values);

            effectiveRequiredMap.put(key, required && visible);
            applyState(key, control, label, visible, required);
        }
    }

    /**
     * Вернуть текущее эффективное значение обязательности поля.
     * <p>
     * Скрытое поле всегда считается необязательным.
     */
    public boolean isEffectiveRequired(String key) {
        return Boolean.TRUE.equals(effectiveRequiredMap.get(key));
    }

    /**
     * Проверить, видимо ли поле сейчас.
     */
    public boolean isVisible(String key, Map<String, Object> values) {
        ConditionDto visibleIf = visibleIfMap.get(key);
        return visibleIf == null || ConditionEvaluator.evaluate(visibleIf, values);
    }

    /**
     * Проверить, обязательно ли поле сейчас (без учёта видимости).
     */
    public boolean isRequired(String key, Map<String, Object> values) {
        Boolean base = baseRequiredMap.getOrDefault(key, false);
        if (Boolean.TRUE.equals(base)) return true;

        ConditionDto requiredIf = requiredIfMap.get(key);
        return requiredIf != null && ConditionEvaluator.evaluate(requiredIf, values);
    }

    // ==========================================================
    // ПРИМЕНЕНИЕ СОСТОЯНИЯ
    // ==========================================================

    private void applyState(String key, Node control, Label label,
                            boolean visible, boolean required) {
        if (control != null) {
            control.setVisible(visible);
            control.setManaged(visible);
            Parent parent = control.getParent();
            if (parent != null) {
                parent.setVisible(visible);
                parent.setManaged(visible);
            }
        }
        if (label != null) {
            label.setVisible(visible);
            label.setManaged(visible);
            String base = originalLabels.getOrDefault(key, "");
            label.setText(base + (required ? " *" : ":"));
        }
    }
}

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
 * <b>Ключевой принцип (US8.5.b-fix3):</b>
 * менеджер управляет <b>только теми полями</b>, для которых определено
 * соответствующее условие. Поля без {@code visibleIf} / {@code requiredIf}
 * <b>не трогаются</b> — их видимость и обязательность управляются другими
 * механизмами ({@code ExecutionMarkingHelper}, {@code FieldMetadataService}
 * и т.д.).
 * <p>
 * Это устраняет конфликт с {@code ExecutionMarkingHelper}, который
 * управляет видимостью полей огнестойкости/взрывозащиты у MOTOR-карточек.
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
     * <p>
     * Регистрировать можно любое поле — но {@code recalcAll} затронет
     * только те, у которых есть {@code visibleIf} или {@code requiredIf}.
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
     * Пересчитать видимость и обязательность полей, у которых есть условия.
     * <p>
     * Поля без условий не трогаются.
     */
    public void recalcAll(Map<String, Object> values) {
        for (Map.Entry<String, Node> entry : fieldControls.entrySet()) {
            String key = entry.getKey();
            Node control = entry.getValue();
            Label label = fieldLabels.get(key);

            // 1. Видимость: только если есть visibleIf
            if (visibleIfMap.containsKey(key)) {
                boolean visible = ConditionEvaluator.evaluate(visibleIfMap.get(key), values);
                applyVisibility(control, label, visible);
            }

            // 2. Обязательность: только если есть requiredIf
            boolean effectiveRequired;
            if (requiredIfMap.containsKey(key)) {
                Boolean base = baseRequiredMap.getOrDefault(key, false);
                boolean required = base
                        || ConditionEvaluator.evaluate(requiredIfMap.get(key), values);
                applyRequiredSuffix(key, label, required);
                effectiveRequired = required;
            } else {
                effectiveRequired = baseRequiredMap.getOrDefault(key, false);
            }

            // 3. Кэш эффективной обязательности = required AND visible
            boolean visibleNow = control == null || control.isVisible();
            effectiveRequiredMap.put(key, effectiveRequired && visibleNow);
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
     * Проверить, видимо ли поле сейчас (без побочных эффектов).
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

    private void applyVisibility(Node control, Label label, boolean visible) {
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
        }
    }

    private void applyRequiredSuffix(String key, Label label, boolean required) {
        if (label == null) return;
        String base = originalLabels.getOrDefault(key, "");
        label.setText(base + (required ? " *" : ":"));
    }
}
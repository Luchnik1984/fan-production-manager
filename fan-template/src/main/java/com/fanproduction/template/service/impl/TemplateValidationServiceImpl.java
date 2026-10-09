package com.fanproduction.template.service.impl;

import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.ConditionOperator;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.formula.FormulaEngine;
import com.fanproduction.template.model.Condition;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.model.MarkingRuleElement;
import com.fanproduction.template.service.TemplateValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация сервиса валидации шаблонов.
 * <p>
 * Проверяет:
 * <ul>
 *   <li>Дубликаты {@code key} в полях.</li>
 *   <li>{@code COMBOBOX} без {@code options}.</li>
 *   <li>{@code SELECTABLE} без {@code referenceType} или с недопустимым.</li>
 *   <li>{@code COMPUTED} без {@code formula} или с неверной формулой.</li>
 *   <li>{@code pullFrom}, ссылающийся на несуществующее поле.</li>
 *   <li>Пустое правило маркировки.</li>
 *   <li>{@code LITERAL}/{@code SEPARATOR} без {@code value}.</li>
 *   <li>{@code FIELD} без {@code fieldKey}.</li>
 *   <li>{@code CONDITION} без {@code field}, {@code operator}, с недостающими {@code value}/{@code values}.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateValidationServiceImpl implements TemplateValidationService {

    /** Допустимые типы ссылок для SELECTABLE. */
    private static final Set<String> VALID_REFERENCE_TYPES = Set.of(
            "MOTOR", "MOTOR_WHEEL", "RADIAL_WHEEL", "AXIAL_WHEEL", "COMPONENT"
    );

    private final FormulaEngine formulaEngine;

    // ==========================================================
    // PUBLIC API
    // ==========================================================

    @Override
    public void validate(FanTemplateVersion version) {
        List<String> errors = collectErrors(version);
        if (!errors.isEmpty()) {
            throw new TemplateValidationException(
                    "Шаблон содержит ошибки:\n- " + String.join("\n- ", errors));
        }
    }

    @Override
    public List<String> collectErrors(FanTemplateVersion version) {
        List<String> errors = new ArrayList<>();

        if (version == null) {
            errors.add("Версия шаблона не может быть null");
            return errors;
        }

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        Set<String> fieldKeys = validateFields(fields, errors);
        validateMarkingRule(rule, fieldKeys, errors);

        if (!errors.isEmpty()) {
            log.warn("Найдено {} ошибок валидации шаблона (versionId={})",
                    errors.size(), version.getId());
        }

        return errors;
    }

    // ==========================================================
    // ВАЛИДАЦИЯ ПОЛЕЙ
    // ==========================================================

    /**
     * Проверить список полей.
     *
     * @return множество всех уникальных ключей полей (для дальнейших проверок)
     */
    private Set<String> validateFields(List<FieldDefinition> fields, List<String> errors) {
        Set<String> keys = new HashSet<>();

        if (fields == null || fields.isEmpty()) {
            errors.add("Список полей не может быть пустым");
            return keys;
        }

        // Первый проход: сбор ключей и базовая валидация
        for (int i = 0; i < fields.size(); i++) {
            FieldDefinition field = fields.get(i);
            String path = "Поле[" + i + "]";

            if (field == null) {
                errors.add(path + ": null");
                continue;
            }

            String key = field.key();
            if (key == null || key.isBlank()) {
                errors.add(path + ": отсутствует key");
                continue;
            }

            if (!keys.add(key)) {
                errors.add("Дублирующийся key поля: '" + key + "'");
            }

            if (field.type() == null) {
                errors.add("Поле '" + key + "': отсутствует type");
                continue;
            }

            validateFieldType(field, errors);
        }

        // Второй проход: проверка pullFrom (нужны все ключи)
        for (FieldDefinition field : fields) {
            if (field == null || field.pullFrom() == null || field.pullFrom().isEmpty()) {
                continue;
            }
            for (Map.Entry<String, String> entry : field.pullFrom().entrySet()) {
                String targetKey = entry.getValue();
                if (targetKey != null && !keys.contains(targetKey)) {
                    errors.add("Поле '" + field.key() + "': pullFrom ссылается на несуществующее поле '"
                            + targetKey + "'");
                }
            }
        }

        return keys;
    }

    private void validateFieldType(FieldDefinition field, List<String> errors) {
        String key = field.key();

        switch (field.type()) {
            case COMBOBOX -> {
                if (field.options() == null || field.options().isEmpty()) {
                    errors.add("COMBOBOX-поле '" + key + "': отсутствует options");
                }
            }

            case SELECTABLE -> {
                String refType = field.referenceType();
                if (refType == null || refType.isBlank()) {
                    errors.add("SELECTABLE-поле '" + key + "': отсутствует referenceType");
                } else if (!VALID_REFERENCE_TYPES.contains(refType)) {
                    errors.add("SELECTABLE-поле '" + key + "': недопустимый referenceType '"
                            + refType + "' (допустимые: " + VALID_REFERENCE_TYPES + ")");
                }
            }

            case COMPUTED -> {
                String formula = field.formula();
                if (formula == null || formula.isBlank()) {
                    errors.add("COMPUTED-поле '" + key + "': отсутствует formula");
                } else {
                    try {
                        formulaEngine.parse(formula);
                    } catch (Exception e) {
                        errors.add("COMPUTED-поле '" + key + "': ошибка в формуле — "
                                + e.getMessage());
                    }
                }
            }

            default -> {
                // Остальные типы не требуют дополнительной валидации
            }
        }
    }

    // ==========================================================
    // ВАЛИДАЦИЯ ПРАВИЛА МАРКИРОВКИ
    // ==========================================================

    private void validateMarkingRule(MarkingRule rule,
                                     Set<String> fieldKeys,
                                     List<String> errors) {
        if (rule == null || rule.sequence() == null || rule.sequence().isEmpty()) {
            errors.add("Правило маркировки не может быть пустым");
            return;
        }

        validateSequence(rule.sequence(), "rule", errors);
    }

    private void validateSequence(List<MarkingRuleElement> sequence,
                                  String path,
                                  List<String> errors) {
        if (sequence == null || sequence.isEmpty()) {
            return;
        }

        for (int i = 0; i < sequence.size(); i++) {
            MarkingRuleElement element = sequence.get(i);
            validateElement(element, path + "[" + i + "]", errors);
        }
    }

    private void validateElement(MarkingRuleElement element,
                                 String path,
                                 List<String> errors) {
        if (element == null) {
            errors.add(path + ": null-элемент");
            return;
        }

        if (element.type() == null) {
            errors.add(path + ": отсутствует type");
            return;
        }

        switch (element.type()) {
            case LITERAL, SEPARATOR -> {
                if (element.value() == null) {
                    errors.add(path + ": " + element.type() + " без value");
                }
            }

            case FIELD -> {
                if (element.fieldKey() == null || element.fieldKey().isBlank()) {
                    errors.add(path + ": FIELD без fieldKey");
                }
            }

            case CONDITION -> validateConditionElement(element, path, errors);
        }
    }

    private void validateConditionElement(MarkingRuleElement element,
                                          String path,
                                          List<String> errors) {
        Condition condition = element.condition();
        if (condition == null) {
            errors.add(path + ": CONDITION без condition");
            return;
        }

        if (condition.field() == null || condition.field().isBlank()) {
            errors.add(path + ": CONDITION без field");
        }

        if (condition.operator() == null) {
            errors.add(path + ": CONDITION без operator");
        } else {
            validateConditionValue(condition, path, errors);
        }

        // Рекурсия по then / otherwise
        validateSequence(element.then(), path + ".then", errors);
        validateSequence(element.otherwise(), path + ".otherwise", errors);
    }

    private void validateConditionValue(Condition condition,
                                        String path,
                                        List<String> errors) {
        ConditionOperator operator = condition.operator();

        switch (operator) {
            case EQUALS, NOT_EQUALS -> {
                if (condition.value() == null) {
                    errors.add(path + ": CONDITION с оператором " + operator
                            + " требует value");
                }
            }
            case IN -> {
                if (condition.values() == null || condition.values().isEmpty()) {
                    errors.add(path + ": CONDITION с оператором IN требует values");
                }
            }
            case EMPTY, NOT_EMPTY -> {
                // value / values не требуются
            }
        }
    }
}

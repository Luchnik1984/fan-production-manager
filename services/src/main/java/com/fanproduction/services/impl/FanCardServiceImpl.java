package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.FanCardEntity;
import com.fanproduction.core.entity.product.FanCardFields;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.services.FanCardService;
import com.fanproduction.services.exception.FanCardCreationException;
import com.fanproduction.repositories.product.FanCardRepository;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.MarkingEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация сервиса FAN-карточек.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FanCardServiceImpl implements FanCardService {

    /**
     * Системные поля {@link FanCardEntity} — те, что хранятся в отдельных колонках
     * таблицы {@code fan_card}.
     * <p>
     * Ссылка на общий набор {@link FanCardFields#SYSTEM_FIELDS}, вычисляемый
     * автоматически через reflection. Подробности — в javadoc {@link FanCardFields}.
     * <p>
     * Локальная константа нужна только для краткости использования внутри класса.
     */
    private static final Set<String> SYSTEM_FIELDS = FanCardFields.SYSTEM_FIELDS;

    private final FanCardRepository fanCardRepository;
    private final FanTemplateRepository fanTemplateRepository;
    private final FanTemplateVersionRepository fanTemplateVersionRepository;
    private final MarkingEngine markingEngine;
    private final ApplicationEventPublisher eventPublisher;

    // ==========================================================
    // CREATE
    // ==========================================================

    @Override
    @Transactional
    public FanCardEntity createFromTemplate(Long templateVersionId,
                                            Map<String, Object> values,
                                            String createdBy) {
        // 1. Загружаем версию шаблона
        FanTemplateVersion version = fanTemplateVersionRepository.findById(templateVersionId)
                .orElseThrow(() -> new FanCardCreationException(
                        "Версия шаблона не найдена: " + templateVersionId));

        // 2. Проверяем статус: создавать карточки можно только по PUBLISHED или LEGACY
        if (version.getStatus() != TemplateStatus.PUBLISHED
                && version.getStatus() != TemplateStatus.LEGACY) {
            throw new FanCardCreationException(
                    "Создавать карточки можно только по версиям PUBLISHED или LEGACY. "
                            + "Текущий статус: " + version.getStatus());
        }

        FanTemplate template = fanTemplateRepository.findById(version.getTemplateId())
                .orElseThrow(() -> new FanCardCreationException(
                        "Шаблон не найден: " + version.getTemplateId()));

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        if (fields == null || fields.isEmpty()) {
            throw new FanCardCreationException("У шаблона нет полей");
        }
        if (rule == null || rule.sequence() == null || rule.sequence().isEmpty()) {
            throw new FanCardCreationException("У шаблона нет правила маркировки");
        }

        // 3. Валидация значений по шаблону
        validateValues(values, fields);

        // 4. Создаём карточку
        FanCardEntity card = new FanCardEntity();
        card.setCardType("FAN");
        card.setTemplateVersionId(templateVersionId);
        card.setFanTemplateId(template.getId());
        card.setCreatedBy(createdBy);

        // 5. Раскладываем поля: системные → колонки, остальные → dynamic_fields
        Map<String, Object> dynamicFields = new HashMap<>();

        // name — из BaseProductCard
        Object nameValue = values.get("name");
        if (nameValue != null && !nameValue.toString().isBlank()) {
            card.setName(nameValue.toString().trim());
        } else {
            card.setName("Вентилятор " + template.getName());
        }

        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null) continue;
            if ("name".equals(key)) continue;  // уже обработали
            if ("fullMarking".equals(key)) continue;  // вычислим позже

            if (SYSTEM_FIELDS.contains(key)) {
                setSystemField(card, key, value);
            } else {
                dynamicFields.put(key, value);
            }
        }

        card.setDynamicFields(dynamicFields);

        // 6. Считаем маркировку
        Map<String, Object> markingValues = buildMarkingValues(card, dynamicFields);
        String fullMarking;
        try {
            fullMarking = markingEngine.evaluate(rule, markingValues, fields);
        } catch (Exception e) {
            throw new FanCardCreationException(
                    "Ошибка вычисления маркировки: " + e.getMessage(), e);
        }

        if (fullMarking == null || fullMarking.isBlank()) {
            throw new FanCardCreationException(
                    "Не удалось вычислить полную маркировку (пустой результат)");
        }

        // 7. Проверяем уникальность маркировки
        if (fanCardRepository.existsByFullMarkingIgnoreCase(fullMarking)) {
            throw new FanCardCreationException(
                    "Карточка с маркировкой '" + fullMarking + "' уже существует");
        }
        card.setFullMarking(fullMarking);

        // 8. Код карточки — технический (по маркировке)
        card.setCode(generateCode(fullMarking));

        // 9. Сохраняем
        FanCardEntity saved = fanCardRepository.save(card);

        // 10. Аудит
        publishAudit(createdBy, AuditAction.CREATE_CARD,
                "Создана FAN-карточка: " + fullMarking);

        log.info("FAN-карточка создана: id={}, templateVersionId={}, marking={}",
                saved.getId(), templateVersionId, fullMarking);

        return saved;
    }

    // ==========================================================
    // ВАЛИДАЦИЯ
    // ==========================================================

    /**
     * Проверить обязательные поля по шаблону.
     */
    private void validateValues(Map<String, Object> values, List<FieldDefinition> fields) {
        if (values == null) values = Map.of();

        for (FieldDefinition field : fields) {
            if (!field.required()) continue;
            Object value = values.get(field.key());
            if (value == null || (value instanceof String s && s.isBlank())) {
                throw new FanCardCreationException(
                        "Обязательное поле не заполнено: " + field.label());
            }
        }
    }

    // ==========================================================
    // РАСКЛАДКА ПОЛЕЙ
    // ==========================================================

    /**
     * Установить системное поле через reflection с конвертацией типа.
     */
    private void setSystemField(FanCardEntity card, String fieldName, Object value) {
        try {
            Field field = FanCardEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object converted = convertValue(value, field.getType());
            field.set(card, converted);
        } catch (NoSuchFieldException e) {
            log.warn("Поле '{}' не найдено в FanCardEntity, игнорируем", fieldName);
        } catch (Exception e) {
            throw new FanCardCreationException(
                    "Ошибка установки поля '" + fieldName + "': " + e.getMessage(), e);
        }
    }

    /**
     * Собрать все значения для вычисления маркировки:
     * системные (из карточки) + динамические.
     */
    private Map<String, Object> buildMarkingValues(FanCardEntity card,
                                                   Map<String, Object> dynamicFields) {
        Map<String, Object> result = new HashMap<>(dynamicFields);

        // Добавляем системные поля из карточки
        for (String fieldName : SYSTEM_FIELDS) {
            try {
                Field field = FanCardEntity.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(card);
                if (value != null) {
                    result.put(fieldName, value);
                }
            } catch (Exception ignored) {
                // Игнорируем — поле могло быть не установлено
            }
        }

        return result;
    }

    // ==========================================================
    // ПРИВЕДЕНИЕ ТИПОВ
    // ==========================================================

    private Object convertValue(Object value, Class<?> targetType) {
        if (value == null) return null;
        if (targetType.isInstance(value)) return value;

        String str = value.toString().trim();
        if (str.isEmpty()) return null;

        try {
            if (targetType == Long.class || targetType == long.class) {
                return Long.parseLong(str);
            }
            if (targetType == Integer.class || targetType == int.class) {
                return Integer.parseInt(str);
            }
            if (targetType == Double.class || targetType == double.class) {
                return Double.parseDouble(str.replace(',', '.'));
            }
            if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.parseBoolean(str);
            }
            if (targetType == String.class) {
                return str;
            }
        } catch (NumberFormatException e) {
            throw new FanCardCreationException(
                    "Не удалось преобразовать '" + value + "' к типу " + targetType.getSimpleName());
        }

        return value;
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    /**
     * Технический код карточки — на основе маркировки.
     */
    private String generateCode(String fullMarking) {
        return fullMarking
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_")
                .replaceAll("_+", "_");
    }

    private void publishAudit(String username, AuditAction action, String details) {
        String actor = (username != null && !username.isBlank()) ? username : "system";
        eventPublisher.publishEvent(new AuditEvent(this, actor, action, details));
    }
}

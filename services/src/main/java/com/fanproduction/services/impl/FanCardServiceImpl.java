package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.FanCardEntity;
import com.fanproduction.core.entity.product.FanCardFields;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.repositories.product.FanCardRepository;
import com.fanproduction.services.FanCardService;
import com.fanproduction.services.exception.FanCardCreationException;
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
import java.util.Optional;
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
     */
    private static final Set<String> SYSTEM_FIELDS = FanCardFields.SYSTEM_FIELDS;

    /**
     * Технические поля, которые не сбрасываются при обновлении.
     * Управляются отдельными методами (миграция карточки на новую версию).
     */
    private static final Set<String> TECHNICAL_FIELDS = Set.of(
            "templateVersionId",
            "fanTemplateId"
    );

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
        FanTemplateVersion version = loadVersionForCreation(templateVersionId);
        FanTemplate template = loadTemplate(version.getTemplateId());

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        validateTemplateContent(fields, rule);
        validateRequiredFields(values, fields);

        FanCardEntity card = new FanCardEntity();
        card.setCardType("FAN");
        card.setTemplateVersionId(templateVersionId);
        card.setFanTemplateId(template.getId());
        card.setCreatedBy(createdBy);
        // Заполняем fan_type именем типа (для отображения в каталоге)
        fillFanType(card, template);

        applyValuesToCard(card, values, "Вентилятор " + template.getName());

        String marking = computeMarking(card, rule, fields, values);
        ensureMarkingUnique(marking, null);

        card.setFullMarking(marking);
        card.setCode(generateCode(marking));

        FanCardEntity saved = fanCardRepository.save(card);

        publishAudit(createdBy, AuditAction.CREATE_CARD,
                "Создана FAN-карточка: " + marking);

        log.info("FAN-карточка создана: id={}, templateVersionId={}, marking={}",
                saved.getId(), templateVersionId, marking);

        return saved;
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @Override
    @Transactional
    public FanCardEntity updateFromValues(Long cardId,
                                          Map<String, Object> values,
                                          String updatedBy) {
        FanCardEntity card = loadCard(cardId);
        FanTemplateVersion version = loadVersionForCard(card);

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        validateTemplateContent(fields, rule);
        validateRequiredFields(values, fields);

        String oldMarking = card.getFullMarking();

        applyValuesToCard(card, values, card.getName());

        String newMarking = computeMarking(card, rule, fields, values);
        ensureMarkingUnique(newMarking, cardId);

        card.setFullMarking(newMarking);
        card.setCode(generateCode(newMarking));

        FanCardEntity saved = fanCardRepository.save(card);

        String auditDetails = oldMarking != null && !oldMarking.equals(newMarking)
                ? "Обновлена FAN-карточка: маркировка '" + oldMarking + "' → '" + newMarking + "'"
                : "Обновлена FAN-карточка: " + newMarking;
        publishAudit(updatedBy, AuditAction.UPDATE_CARD, auditDetails);

        log.info("FAN-карточка обновлена: id={}, marking={}", cardId, newMarking);

        return saved;
    }

    // ==========================================================
    // RECALCULATE
    // ==========================================================

    @Override
    @Transactional
    public FanCardEntity recalculateMarking(Long cardId, String updatedBy) {
        FanCardEntity card = loadCard(cardId);
        FanTemplateVersion version = loadVersionForCard(card);

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        validateTemplateContent(fields, rule);

        String oldMarking = card.getFullMarking();
        String newMarking = computeMarking(card, rule, fields, null );
        ensureMarkingUnique(newMarking, cardId);

        card.setFullMarking(newMarking);
        card.setCode(generateCode(newMarking));

        FanCardEntity saved = fanCardRepository.save(card);

        String auditDetails = oldMarking != null && !oldMarking.equals(newMarking)
                ? "Пересчитана маркировка FAN-карточки: '" + oldMarking + "' → '" + newMarking + "'"
                : "Пересчитана маркировка FAN-карточки: " + newMarking;
        publishAudit(updatedBy, AuditAction.UPDATE_CARD, auditDetails);

        log.info("Маркировка FAN-карточки пересчитана: id={}, marking={}", cardId, newMarking);

        return saved;
    }

    // ==========================================================
    // ЗАГРУЗКА И ПРОВЕРКИ
    // ==========================================================

    private FanCardEntity loadCard(Long cardId) {
        FanCardEntity card = fanCardRepository.findById(cardId)
                .orElseThrow(() -> new FanCardCreationException(
                        "Карточка не найдена: " + cardId));

        if (!"FAN".equals(card.getCardType())) {
            throw new FanCardCreationException(
                    "Карточка не является FAN-карточкой: " + card.getCardType());
        }
        return card;
    }

    private FanTemplateVersion loadVersionForCreation(Long templateVersionId) {
        FanTemplateVersion version = fanTemplateVersionRepository.findById(templateVersionId)
                .orElseThrow(() -> new FanCardCreationException(
                        "Версия шаблона не найдена: " + templateVersionId));

        if (version.getStatus() != TemplateStatus.PUBLISHED
                && version.getStatus() != TemplateStatus.LEGACY) {
            throw new FanCardCreationException(
                    "Создавать карточки можно только по версиям PUBLISHED или LEGACY. "
                            + "Текущий статус: " + version.getStatus());
        }
        return version;
    }

    private FanTemplateVersion loadVersionForCard(FanCardEntity card) {
        if (card.getTemplateVersionId() == null) {
            throw new FanCardCreationException(
                    "У карточки нет привязки к версии шаблона");
        }

        FanTemplateVersion version = fanTemplateVersionRepository
                .findById(card.getTemplateVersionId())
                .orElseThrow(() -> new FanCardCreationException(
                        "Версия шаблона не найдена: " + card.getTemplateVersionId()));

        if (version.getStatus() == TemplateStatus.DRAFT
                || version.getStatus() == TemplateStatus.ARCHIVED) {
            throw new FanCardCreationException(
                    "Обновление карточек по версии в статусе " + version.getStatus() + " запрещено");
        }
        return version;
    }

    private FanTemplate loadTemplate(Long templateId) {
        return fanTemplateRepository.findById(templateId)
                .orElseThrow(() -> new FanCardCreationException(
                        "Шаблон не найден: " + templateId));
    }

    private void validateTemplateContent(List<FieldDefinition> fields, MarkingRule rule) {
        if (fields == null || fields.isEmpty()) {
            throw new FanCardCreationException("У шаблона нет полей");
        }
        if (rule == null || rule.sequence() == null || rule.sequence().isEmpty()) {
            throw new FanCardCreationException("У шаблона нет правила маркировки");
        }
    }

    private void validateRequiredFields(Map<String, Object> values, List<FieldDefinition> fields) {
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
    // ПРИМЕНЕНИЕ ЗНАЧЕНИЙ К КАРТОЧКЕ
    // ==========================================================

    /**
     * Применить значения из {@code values} к карточке.
     * <p>
     * Семантика «форма отправляется целиком»:
     * <ul>
     *   <li>Все системные поля (кроме {@link #TECHNICAL_FIELDS}) сначала сбрасываются.</li>
     *   <li>Затем применяются значения из {@code values}.</li>
     *   <li>Поля, которых нет в {@code values}, остаются null.</li>
     *   <li>Пользовательские поля идут в {@code dynamicFields}.</li>
     *   <li>{@code fullMarking} вычисляется отдельно — не из values.</li>
     * </ul>
     *
     * @param defaultName имя по умолчанию, если в {@code values} нет {@code name}
     */
    private void applyValuesToCard(FanCardEntity card,
                                   Map<String, Object> values,
                                   String defaultName) {
        Map<String, Object> safeValues = values != null ? values : Map.of();

        // name
        Object nameValue = safeValues.get("name");
        if (nameValue != null && !nameValue.toString().isBlank()) {
            card.setName(nameValue.toString().trim());
        } else if (card.getName() == null && defaultName != null) {
            card.setName(defaultName);
        }

        // Сброс системных полей (кроме технических и fullMarking)
        for (String fieldName : SYSTEM_FIELDS) {
            if (TECHNICAL_FIELDS.contains(fieldName)) continue;
            if ("fullMarking".equals(fieldName)) continue;
            setSystemField(card, fieldName, null);
        }

        // Применение значений
        Map<String, Object> dynamicFields = new HashMap<>();

        for (Map.Entry<String, Object> entry : safeValues.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if ("name".equals(key)) continue;
            if ("fullMarking".equals(key)) continue;
            if ("code".equals(key)) continue;

            if (SYSTEM_FIELDS.contains(key)) {
                setSystemField(card, key, value);
            } else if (value != null) {
                dynamicFields.put(key, value);
            }
        }

        card.setDynamicFields(dynamicFields);
    }

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

    // ==========================================================
    // ВЫЧИСЛЕНИЕ МАРКИРОВКИ
    // ==========================================================

    /**
     * Определить полную маркировку карточки.
     * <p>
     * Логика:
     * <ol>
     *   <li>Если в {@code values} передан непустой {@code fullMarking} —
     *       используем его (пользователь отредактировал вручную).</li>
     *   <li>Иначе — вычисляем через {@link MarkingEngine}.</li>
     * </ol>
     *
     * @param card           карточка (с уже применёнными values)
     * @param rule           правило маркировки
     * @param fields         определения полей
     * @param valuesFromForm значения из формы (могут содержать fullMarking)
     * @return итоговая маркировка
     */
    private String computeMarking(FanCardEntity card,
                                  MarkingRule rule,
                                  List<FieldDefinition> fields,
                                  Map<String, Object> valuesFromForm) {
        // 1. Ручное редактирование
        if (valuesFromForm != null) {
            Object manual = valuesFromForm.get("fullMarking");
            if (manual != null) {
                String manualStr = manual.toString().trim();
                if (!manualStr.isEmpty()) {
                    log.debug("Используется маркировка, заданная вручную: {}", manualStr);
                    return manualStr;
                }
            }
        }

        // 2. Автоматическое вычисление
        Map<String, Object> markingValues = buildMarkingValues(card, card.getDynamicFields());

        String marking;
        try {
            marking = markingEngine.evaluate(rule, markingValues, fields);
        } catch (Exception e) {
            throw new FanCardCreationException(
                    "Ошибка вычисления маркировки: " + e.getMessage(), e);
        }

        if (marking == null || marking.isBlank()) {
            throw new FanCardCreationException(
                    "Не удалось вычислить полную маркировку (пустой результат)");
        }
        return marking;
    }

    private Map<String, Object> buildMarkingValues(FanCardEntity card,
                                                   Map<String, Object> dynamicFields) {
        Map<String, Object> result = new HashMap<>();
        if (dynamicFields != null) {
            result.putAll(dynamicFields);
        }

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

    /**
     * Проверить, что маркировка уникальна.
     *
     * @param excludeCardId ID карточки, которую нужно исключить из проверки
     *                      (для update/recalculate). При создании — {@code null}.
     */
    private void ensureMarkingUnique(String marking, Long excludeCardId) {
        Optional<FanCardEntity> existing = fanCardRepository.findByFullMarkingIgnoreCase(marking);
        if (existing.isPresent()) {
            if (excludeCardId == null || !existing.get().getId().equals(excludeCardId)) {
                throw new FanCardCreationException(
                        "Карточка с маркировкой '" + marking + "' уже существует");
            }
        }
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

    /**
     * Заполнить {@code fanType} (имя типа) и {@code fanSubtype} (имя вида)
     * из шаблона. Используется GUI для отображения в каталоге:
     * «Вентилятор канальный VRK-PatAIR-P-60-30-4-380».
     * <p>
     * Требует доступа к {@code FanTypeRepository}, {@code FanSpeciesRepository},
     * {@code FanSeriesRepository}. Если в будущем откажемся — удалим метод.
     */
    private void fillFanType(FanCardEntity card, FanTemplate template) {
        // Идём по цепочке: series → species → type
        // Пока FanCardRepository — отдельный модуль, репозитории шаблонов живут
        // в fan-template. Мы не хотим тянуть их в services.
        // Поэтому — оставим заглушку, заполнение сделает FanCardService на стороне
        // fan-template позже. Сейчас — заполняем хотя бы имя шаблона.
        card.setFanType(template.getName());
    }
}

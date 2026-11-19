package com.fanproduction.services.impl;

import com.fanproduction.core.entity.product.DynamicCardEntity;
import com.fanproduction.core.entity.product.FanCardFields;
import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.repositories.product.DynamicCardRepository;
import com.fanproduction.services.DynamicCardService;
import com.fanproduction.services.exception.DynamicCardCreationException;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.entity.ProductSeries;
import com.fanproduction.template.entity.ProductSpecies;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductSpeciesRepository;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
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
 * Реализация сервиса карточек, создаваемых по шаблону (dynamic_card).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DynamicCardServiceImpl implements DynamicCardService {

    /**
     * Системные поля {@link DynamicCardEntity} — те, что хранятся в отдельных колонках
     * таблицы {@code dynamic_card}.
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
            "templateVersionId", "productTypeId",
            "productTemplateId"
    );

    private final DynamicCardRepository fanCardRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private final ProductTemplateVersionRepository productTemplateVersionRepository;
    private final ProductSeriesRepository productSeriesRepository;
    private final ProductSpeciesRepository productSpeciesRepository;
    private final MarkingEngine markingEngine;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Определить тип продукции по шаблону:
     * шаблон → серия → вид → тип.
     * <p>
     * Нужен, чтобы у карточки была прямая ссылка {@code product_type_id}
     * (миграция V34), а не только косвенная через шаблон.
     *
     * @return ID типа продукции или {@code null}, если цепочка не полная
     */
    private Long resolveProductTypeId(ProductTemplate template) {
        if (template.getSeriesId() == null) {
            return null;
        }
        return productSeriesRepository.findById(template.getSeriesId())
                .flatMap((ProductSeries series) -> productSpeciesRepository.findById(series.getSpeciesId()))
                .map(ProductSpecies::getTypeId)
                .orElse(null);
    }

    // ==========================================================
    // CREATE
    // ==========================================================

    @Override
    @Transactional
    public DynamicCardEntity createFromTemplate(Long templateVersionId,
                                            Map<String, Object> values,
                                            String createdBy) {
        ProductTemplateVersion version = loadVersionForCreation(templateVersionId);
        ProductTemplate template = loadTemplate(version.getTemplateId());

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        validateTemplateContent(fields, rule);
        validateRequiredFields(values, fields);

        DynamicCardEntity card = new DynamicCardEntity();
        card.setCardType("DYNAMIC");
        card.setTemplateVersionId(templateVersionId);
        card.setProductTemplateId(template.getId());

        card.setCreatedBy(createdBy);

        applyValuesToCard(card, values, "Вентилятор " + template.getName());
        card.setProductTypeId(resolveProductTypeId(template));

        String marking = computeMarking(card, rule, fields, values, templateVersionId);
        ensureMarkingUnique(marking, null);

        card.setFullMarking(marking);
        card.setCode(generateCode(marking));
        card.setName(marking);

        DynamicCardEntity saved = fanCardRepository.save(card);

        publishAudit(createdBy, AuditAction.CREATE_CARD,
                "Создана карточка по шаблону: " + marking);

        log.info("Карточка по шаблону создана: id={}, templateVersionId={}, marking={}",
                saved.getId(), templateVersionId, marking);

        return saved;
    }

    // ==========================================================
    // UPDATE
    // ==========================================================

    @Override
    @Transactional
    public DynamicCardEntity updateFromValues(Long cardId,
                                          Map<String, Object> values,
                                          String updatedBy) {
        DynamicCardEntity card = loadCard(cardId);
        ProductTemplateVersion version = loadVersionForCard(card);

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        validateTemplateContent(fields, rule);
        validateRequiredFields(values, fields);

        String oldMarking = card.getFullMarking();

        applyValuesToCard(card, values, card.getName());

        String newMarking = computeMarking(card, rule, fields, values, version.getId());
        ensureMarkingUnique(newMarking, cardId);

        card.setFullMarking(newMarking);
        card.setCode(generateCode(newMarking));
        card.setName(newMarking);

        DynamicCardEntity saved = fanCardRepository.save(card);

        String auditDetails = oldMarking != null && !oldMarking.equals(newMarking)
                ? "Обновлена карточка по шаблону: маркировка '" + oldMarking + "' → '" + newMarking + "'"
                : "Обновлена карточка по шаблону: " + newMarking;
        publishAudit(updatedBy, AuditAction.UPDATE_CARD, auditDetails);

        log.info("Карточка по шаблону обновлена: id={}, marking={}", cardId, newMarking);

        return saved;
    }

    // ==========================================================
    // RECALCULATE
    // ==========================================================

    @Override
    @Transactional
    public DynamicCardEntity recalculateMarking(Long cardId, String updatedBy) {
        DynamicCardEntity card = loadCard(cardId);
        ProductTemplateVersion version = loadVersionForCard(card);

        List<FieldDefinition> fields = version.getFieldsJson();
        MarkingRule rule = version.getMarkingRuleJson();

        validateTemplateContent(fields, rule);

        String oldMarking = card.getFullMarking();
        String newMarking = computeMarking(card, rule, fields, null, version.getId() );
        ensureMarkingUnique(newMarking, cardId);

        card.setFullMarking(newMarking);
        card.setCode(generateCode(newMarking));
        card.setName(newMarking);

        DynamicCardEntity saved = fanCardRepository.save(card);

        String auditDetails = oldMarking != null && !oldMarking.equals(newMarking)
                ? "Пересчитана маркировка карточки: '" + oldMarking + "' → '" + newMarking + "'"
                : "Пересчитана маркировка карточки: " + newMarking;
        publishAudit(updatedBy, AuditAction.UPDATE_CARD, auditDetails);

        log.info("Маркировка карточки пересчитана: id={}, marking={}", cardId, newMarking);

        return saved;
    }

    // ==========================================================
    // ЗАГРУЗКА И ПРОВЕРКИ
    // ==========================================================

    private DynamicCardEntity loadCard(Long cardId) {
        DynamicCardEntity card = fanCardRepository.findById(cardId)
                .orElseThrow(() -> new DynamicCardCreationException(
                        "Карточка не найдена: " + cardId));

        if (!"DYNAMIC".equals(card.getCardType())) {
            throw new DynamicCardCreationException(
                    "Карточка не является динамической: " + card.getCardType());
        }
        return card;
    }

    private ProductTemplateVersion loadVersionForCreation(Long templateVersionId) {
        ProductTemplateVersion version = productTemplateVersionRepository.findById(templateVersionId)
                .orElseThrow(() -> new DynamicCardCreationException(
                        "Версия шаблона не найдена: " + templateVersionId));

        if (version.getStatus() != TemplateStatus.PUBLISHED
                && version.getStatus() != TemplateStatus.LEGACY) {
            throw new DynamicCardCreationException(
                    "Создавать карточки можно только по версиям PUBLISHED или LEGACY. "
                            + "Текущий статус: " + version.getStatus());
        }
        return version;
    }

    private ProductTemplateVersion loadVersionForCard(DynamicCardEntity card) {
        if (card.getTemplateVersionId() == null) {
            throw new DynamicCardCreationException(
                    "У карточки нет привязки к версии шаблона");
        }

        ProductTemplateVersion version = productTemplateVersionRepository
                .findById(card.getTemplateVersionId())
                .orElseThrow(() -> new DynamicCardCreationException(
                        "Версия шаблона не найдена: " + card.getTemplateVersionId()));

        if (version.getStatus() == TemplateStatus.DRAFT
                || version.getStatus() == TemplateStatus.ARCHIVED) {
            throw new DynamicCardCreationException(
                    "Обновление карточек по версии в статусе " + version.getStatus() + " запрещено");
        }
        return version;
    }

    private ProductTemplate loadTemplate(Long templateId) {
        return productTemplateRepository.findById(templateId)
                .orElseThrow(() -> new DynamicCardCreationException(
                        "Шаблон не найден: " + templateId));
    }

    private void validateTemplateContent(List<FieldDefinition> fields, MarkingRule rule) {
        if (fields == null || fields.isEmpty()) {
            throw new DynamicCardCreationException("У шаблона нет полей");
        }
        if (rule == null || rule.sequence() == null || rule.sequence().isEmpty()) {
            throw new DynamicCardCreationException("У шаблона нет правила маркировки");
        }
    }

    private void validateRequiredFields(Map<String, Object> values, List<FieldDefinition> fields) {
        if (values == null) values = Map.of();

        for (FieldDefinition field : fields) {
            if (!field.required()) continue;
            Object value = values.get(field.key());
            if (value == null || (value instanceof String s && s.isBlank())) {
                throw new DynamicCardCreationException(
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
    private void applyValuesToCard(DynamicCardEntity card,
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

    private void setSystemField(DynamicCardEntity card, String fieldName, Object value) {
        try {
            Field field = DynamicCardEntity.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            Object converted = convertValue(value, field.getType());
            field.set(card, converted);
        } catch (NoSuchFieldException e) {
            log.warn("Поле '{}' не найдено в DynamicCardEntity, игнорируем", fieldName);
        } catch (Exception e) {
            throw new DynamicCardCreationException(
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
    private String computeMarking(DynamicCardEntity card,
                                  MarkingRule rule,
                                  List<FieldDefinition> fields,
                                  Map<String, Object> valuesFromForm,
                                  Long templateVersionId) {
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
            marking = markingEngine.evaluate(rule, markingValues, fields, templateVersionId);
        } catch (Exception e) {
            throw new DynamicCardCreationException(
                    "Ошибка вычисления маркировки: " + e.getMessage(), e);
        }

        if (marking == null || marking.isBlank()) {
            throw new DynamicCardCreationException(
                    "Не удалось вычислить полную маркировку (пустой результат)");
        }
        return marking;
    }

    private Map<String, Object> buildMarkingValues(DynamicCardEntity card,
                                                   Map<String, Object> dynamicFields) {
        Map<String, Object> result = new HashMap<>();
        if (dynamicFields != null) {
            result.putAll(dynamicFields);
        }

        for (String fieldName : SYSTEM_FIELDS) {
            try {
                Field field = DynamicCardEntity.class.getDeclaredField(fieldName);
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
        Optional<DynamicCardEntity> existing = fanCardRepository.findByFullMarkingIgnoreCase(marking);
        if (existing.isPresent()) {
            if (excludeCardId == null || !existing.get().getId().equals(excludeCardId)) {
                throw new DynamicCardCreationException(
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
            throw new DynamicCardCreationException(
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

}

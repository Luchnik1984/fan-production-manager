package com.fanproduction.template.service.impl;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.ProductTemplateDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.service.ProductTemplateService;
import com.fanproduction.template.service.TemplateValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductTemplateServiceImpl implements ProductTemplateService {

    /**
     * Максимум активных версий на шаблон (DRAFT + PUBLISHED + LEGACY).
     * Защищает от «размножения» версий: старые нужно архивировать.
     */
    private static final int MAX_ACTIVE_VERSIONS = 20;

    /**
     * Статусы, которые считаются активными (участвуют в лимите и в выборе
     * версии для новых карточек).
     */
    private static final Set<TemplateStatus> ACTIVE_STATUSES = Set.of(
            TemplateStatus.DRAFT,
            TemplateStatus.PUBLISHED,
            TemplateStatus.LEGACY
    );

    private final ProductTemplateRepository productTemplateRepository;
    private final ProductTemplateVersionRepository productTemplateVersionRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TemplateValidationService templateValidationService;

    // ==========================================================
    // TEMPLATE
    // ==========================================================

    @Override
    public List<ProductTemplateDto> getAllTemplates() {
        return productTemplateRepository.findAllByOrderByNameAsc().stream()
                .map(template -> toTemplateDto(template, null))
                .toList();
    }

    @Override
    public ProductTemplateDto getTemplateById(Long id) {
        return toTemplateDto(findTemplateOrThrow(id), null);
    }

    @Override
    public ProductTemplateDto getTemplateWithVersions(Long id) {
        ProductTemplate template = findTemplateOrThrow(id);
        List<ProductTemplateVersionDto> versions = productTemplateVersionRepository
                .findByTemplateIdOrderByVersionDesc(id).stream()
                .map(this::toVersionDto)
                .toList();
        return toTemplateDto(template, versions);
    }

    @Override
    public ProductTemplateDto getTemplateBySeries(Long seriesId) {
        ProductTemplate template = productTemplateRepository.findBySeriesId(seriesId)
                .orElseThrow(() -> new TemplateValidationException(
                        "Шаблон для серии не найден: " + seriesId));
        return toTemplateDto(template, null);
    }

    @Override
    @Transactional
    public ProductTemplateDto createTemplate(Long seriesId,
                                         String name,
                                         String description,
                                         List<FieldDefinition> fields,
                                         MarkingRule markingRule,
                                         String createdBy) {
        validateNotEmpty(name, "Наименование шаблона");

        if (productTemplateRepository.existsBySeriesId(seriesId)) {
            throw new TemplateValidationException("Для серии уже существует шаблон: " + seriesId);
        }

        ProductTemplate template = new ProductTemplate();
        template.setSeriesId(seriesId);
        template.setName(name.trim());
        template.setDescription(trimOrNull(description));
        template.setCreatedBy(createdBy);
        ProductTemplate savedTemplate = productTemplateRepository.save(template);

        ProductTemplateVersion firstVersion = new ProductTemplateVersion();
        firstVersion.setTemplateId(savedTemplate.getId());
        firstVersion.setVersion(1);
        firstVersion.setStatus(TemplateStatus.DRAFT);
        firstVersion.setFieldsJson(fields != null ? fields : List.of());
        firstVersion.setMarkingRuleJson(markingRule != null ? markingRule : MarkingRule.empty());
        firstVersion.setCreatedBy(createdBy);
        productTemplateVersionRepository.save(firstVersion);

        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создан шаблон: '" + savedTemplate.getName() + "' (серия ID=" + seriesId + ")");

        return toTemplateDto(savedTemplate, null);
    }

    @Override
    @Transactional
    public ProductTemplateDto importTemplate(Long seriesId,
                                             com.fanproduction.template.dto.TemplateExchangeDto dto,
                                             String createdBy) {
        if (dto == null) {
            throw new TemplateValidationException("Пустой файл шаблона");
        }
        if (dto.name() == null || dto.name().isBlank()) {
            throw new TemplateValidationException("В файле не указано наименование шаблона");
        }

        ProductTemplateDto created = createTemplate(
                seriesId,
                dto.name().trim(),
                dto.description(),
                dto.fields(),
                dto.markingRule(),
                createdBy);

        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Импортирован шаблон '" + created.name() + "' в серию ID=" + seriesId);

        log.info("Шаблон импортирован: id={}, seriesId={}", created.id(), seriesId);
        return created;
    }

    @Override
    public com.fanproduction.template.dto.TemplateExchangeDto exportTemplate(Long templateId) {
        ProductTemplate template = findTemplateOrThrow(templateId);

        ProductTemplateVersion source = null;
        if (template.getCurrentVersionId() != null) {
            source = productTemplateVersionRepository.findById(template.getCurrentVersionId())
                    .orElse(null);
        }
        if (source == null) {
            List<ProductTemplateVersion> versions =
                    productTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(templateId);
            source = versions.isEmpty() ? null : versions.get(0);
        }

        return new com.fanproduction.template.dto.TemplateExchangeDto(
                template.getName(),
                template.getDescription(),
                source != null && source.getFieldsJson() != null ? source.getFieldsJson() : List.of(),
                source != null && source.getMarkingRuleJson() != null
                        ? source.getMarkingRuleJson()
                        : MarkingRule.empty()
        );
    }

    @Override
    @Transactional
    public ProductTemplateDto updateTemplate(Long id, String name, String description, String updatedBy) {
        ProductTemplate template = findTemplateOrThrow(id);

        String newName = trimOrNull(name);
        if (newName != null && !newName.equals(template.getName())) {
            template.setName(newName);
        }
        String newDescription = trimOrNull(description);
        if (!Objects.equals(newDescription, template.getDescription())) {
            template.setDescription(newDescription);
        }

        ProductTemplate saved = productTemplateRepository.save(template);
        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён шаблон: '" + saved.getName() + "' (ID=" + id + ")");
        return toTemplateDto(saved, null);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id, String deletedBy) {
        ProductTemplate template = findTemplateOrThrow(id);
        productTemplateRepository.delete(template);
        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалён шаблон: '" + template.getName() + "' (ID=" + id + ")");
    }

    // ==========================================================
    // VERSION — ЧТЕНИЕ
    // ==========================================================

    @Override
    public List<ProductTemplateVersionDto> getVersionsByTemplate(Long templateId) {
        if (!productTemplateRepository.existsById(templateId)) {
            throw new TemplateValidationException("Шаблон не найден: " + templateId);
        }
        return productTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(templateId).stream()
                .map(this::toVersionDto)
                .toList();
    }

    @Override
    public ProductTemplateVersionDto getVersionById(Long versionId) {
        return toVersionDto(findVersionOrThrow(versionId));
    }

    @Override
    public ProductTemplateVersionDto getPublishedVersion(Long templateId) {
        return productTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(templateId, TemplateStatus.PUBLISHED)
                .map(this::toVersionDto)
                .orElse(null);
    }

    @Override
    public List<ProductTemplateVersionDto> getAvailableVersions(Long templateId) {
        return productTemplateVersionRepository
                .findByTemplateIdAndStatusIn(templateId,
                        Set.of(TemplateStatus.PUBLISHED, TemplateStatus.LEGACY))
                .stream()
                .map(this::toVersionDto)
                .toList();
    }

    @Override
    public ProductTemplateVersionDto getDraftVersion(Long templateId) {
        return productTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(templateId, TemplateStatus.DRAFT)
                .map(this::toVersionDto)
                .orElse(null);
    }

    // ==========================================================
    // VERSION — СОЗДАНИЕ И РЕДАКТИРОВАНИЕ
    // ==========================================================

    @Override
    @Transactional
    public ProductTemplateVersionDto createNewVersion(Long templateId,
                                                  List<FieldDefinition> fields,
                                                  MarkingRule markingRule,
                                                  String createdBy) {
        ProductTemplate template = findTemplateOrThrow(templateId);

        if (productTemplateVersionRepository.existsByTemplateIdAndStatus(templateId, TemplateStatus.DRAFT)) {
            throw new TemplateValidationException(
                    "У шаблона уже есть черновик (DRAFT). Сначала опубликуйте или удалите его.");
        }

        ensureActiveVersionsLimit(templateId, "создать новую версию");

        // Автокопирование: если fields/markingRule не переданы — копируем из источника
        List<FieldDefinition> effectiveFields = fields;
        MarkingRule effectiveRule = markingRule;

        if (effectiveFields == null || effectiveRule == null) {
            ProductTemplateVersion source = resolveSourceForNewVersion(template);
            if (source != null) {
                if (effectiveFields == null) {
                    effectiveFields = source.getFieldsJson();
                }
                if (effectiveRule == null) {
                    effectiveRule = source.getMarkingRuleJson();
                }
            }
        }

        int nextVersionNumber = productTemplateVersionRepository.findMaxVersion(templateId)
                .orElse(0) + 1;

        ProductTemplateVersion version = new ProductTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersion(nextVersionNumber);
        version.setStatus(TemplateStatus.DRAFT);
        version.setFieldsJson(effectiveFields != null ? effectiveFields : List.of());
        version.setMarkingRuleJson(effectiveRule != null ? effectiveRule : MarkingRule.empty());
        version.setCreatedBy(createdBy);

        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        String auditDetails = "Создана черновая версия v" + nextVersionNumber
                + " для шаблона '" + template.getName() + "'";
        if (fields == null || markingRule == null) {
            auditDetails += " (поля/правило скопированы из предыдущей версии)";
        }
        publishAudit(createdBy, AuditAction.TEMPLATE_UPDATE, auditDetails);

        log.info("Создана новая версия: templateId={}, version={}, автокопирование={}",
                templateId, nextVersionNumber, fields == null || markingRule == null);

        return toVersionDto(saved);
    }



    @Override
    @Transactional
    public ProductTemplateVersionDto updateDraftVersion(Long versionId,
                                                    List<FieldDefinition> fields,
                                                    MarkingRule markingRule,
                                                    String updatedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateValidationException(
                    "Можно редактировать только DRAFT-версии. Текущий статус: " + version.getStatus());
        }

        if (fields != null) version.setFieldsJson(fields);
        if (markingRule != null) version.setMarkingRuleJson(markingRule);

        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён черновик v" + saved.getVersion() + " шаблона ID=" + saved.getTemplateId());

        return toVersionDto(saved);
    }

    // ==========================================================
    // VERSION — ЖИЗНЕННЫЙ ЦИКЛ
    // ==========================================================

    @Override
    @Transactional
    public ProductTemplateVersionDto publishVersion(Long versionId, String publishedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateValidationException(
                    "Опубликовать можно только DRAFT-версию. Текущий статус: " + version.getStatus());
        }

        templateValidationService.validate(version);

        ProductTemplate template = findTemplateOrThrow(version.getTemplateId());

        // Сначала снимаем PUBLISHED со старой версии и принудительно сбрасываем
        // изменение в БД: уникальный частичный индекс
        // idx_product_template_version_single_published не даст держать две
        // PUBLISHED-версии даже внутри одной транзакции.
        productTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(template.getId(), TemplateStatus.PUBLISHED)
                .ifPresent(oldPublished -> {
                    oldPublished.setStatus(TemplateStatus.LEGACY);
                    productTemplateVersionRepository.saveAndFlush(oldPublished);
                });

        version.setStatus(TemplateStatus.PUBLISHED);
        version.setPublishedAt(LocalDateTime.now());
        version.setPublishedBy(publishedBy);
        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        template.setCurrentVersionId(saved.getId());
        productTemplateRepository.save(template);

        publishAudit(publishedBy, AuditAction.TEMPLATE_PUBLISH,
                "Опубликована версия v" + saved.getVersion()
                        + " шаблона '" + template.getName() + "'");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto deprecateVersion(Long versionId, String updatedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.LEGACY) {
            throw new TemplateValidationException(
                    "Деприкейт возможен только для LEGACY-версии. Текущий: " + version.getStatus());
        }

        version.setStatus(TemplateStatus.DEPRECATED);
        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_DEPRECATE,
                "Версия v" + saved.getVersion() + " помечена как DEPRECATED");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto archiveVersion(Long versionId, String updatedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DEPRECATED) {
            throw new TemplateValidationException(
                    "Архивировать можно только DEPRECATED-версию. Текущий: " + version.getStatus());
        }

        version.setStatus(TemplateStatus.ARCHIVED);
        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Версия v" + saved.getVersion() + " заархивирована");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto restoreVersion(Long versionId, String updatedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.ARCHIVED) {
            throw new TemplateValidationException(
                    "Восстановить можно только ARCHIVED-версию. Текущий статус: " + version.getStatus());
        }

        version.setStatus(TemplateStatus.DEPRECATED);
        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_RESTORE,
                "Версия v" + saved.getVersion() + " восстановлена из архива (ARCHIVED → DEPRECATED)");

        log.info("Версия восстановлена из архива: versionId={}, version={}", versionId, saved.getVersion());

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto undeprecateVersion(Long versionId, String updatedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DEPRECATED) {
            throw new TemplateValidationException(
                    "Снять деприкейт можно только с DEPRECATED-версии. Текущий статус: " + version.getStatus());
        }

        ensureActiveVersionsLimit(version.getTemplateId(), "снять деприкейт");

        version.setStatus(TemplateStatus.LEGACY);
        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UN_DEPRECATE,
                "Версия v" + saved.getVersion() + " возвращена в LEGACY (DEPRECATED → LEGACY)");

        log.info("Деприкейт снят: versionId={}, version={}", versionId, saved.getVersion());

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto createVersionFrom(Long templateId, Long sourceVersionId, String createdBy) {
        ProductTemplate template = findTemplateOrThrow(templateId);

        if (productTemplateVersionRepository.existsByTemplateIdAndStatus(templateId, TemplateStatus.DRAFT)) {
            throw new TemplateValidationException(
                    "У шаблона уже есть черновик (DRAFT). Сначала опубликуйте или удалите его.");
        }

        ProductTemplateVersion source;
        if (sourceVersionId != null) {
            source = findVersionOrThrow(sourceVersionId);
            if (!Objects.equals(source.getTemplateId(), templateId)) {
                throw new TemplateValidationException(
                        "Версия v" + source.getVersion() + " принадлежит другому шаблону");
            }
        } else {
            source = resolveSourceForNewVersion(template);
            if (source == null) {
                throw new TemplateValidationException(
                        "У шаблона нет версий — нечего копировать. Создайте версию с нуля.");
            }
        }

        ensureActiveVersionsLimit(templateId, "создать версию на основе выбранной");

        int nextVersionNumber = productTemplateVersionRepository.findMaxVersion(templateId)
                .orElse(0) + 1;

        ProductTemplateVersion version = new ProductTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersion(nextVersionNumber);
        version.setStatus(TemplateStatus.DRAFT);
        version.setFieldsJson(source.getFieldsJson() != null ? source.getFieldsJson() : List.of());
        version.setMarkingRuleJson(source.getMarkingRuleJson() != null
                ? source.getMarkingRuleJson()
                : MarkingRule.empty());
        version.setCreatedBy(createdBy);

        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        publishAudit(createdBy, AuditAction.TEMPLATE_CLONE,
                "Создана версия v" + nextVersionNumber + " на основе v" + source.getVersion()
                        + " (" + source.getStatus() + ") шаблона '" + template.getName() + "'");

        log.info("Версия создана на основе выбранной: templateId={}, newVersion={}, sourceVersion={}",
                templateId, nextVersionNumber, source.getVersion());

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public void deleteVersion(Long versionId, String deletedBy) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DRAFT
                && version.getStatus() != TemplateStatus.ARCHIVED) {
            throw new TemplateValidationException(
                    "Удалить можно только DRAFT или ARCHIVED. Текущий: " + version.getStatus());
        }

        ProductTemplate template = findTemplateOrThrow(version.getTemplateId());
        if (Objects.equals(template.getCurrentVersionId(), versionId)) {
            throw new TemplateValidationException("Нельзя удалить текущую опубликованную версию");
        }

        productTemplateVersionRepository.delete(version);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалена версия v" + version.getVersion() + " шаблона ID=" + version.getTemplateId());
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    /**
     * Выбрать версию-источник для автокопирования полей и правила.
     * <ol>
     *   <li>{@code current_version_id} — если задан.</li>
     *   <li>Последняя по номеру версии — если нет current.</li>
     * </ol>
     *
     * @return версия-источник или {@code null}, если у шаблона вообще нет версий
     */
    private ProductTemplateVersion resolveSourceForNewVersion(ProductTemplate template) {
        // 1. Текущая опубликованная
        if (template.getCurrentVersionId() != null) {
            Optional<ProductTemplateVersion> current = productTemplateVersionRepository
                    .findById(template.getCurrentVersionId());
            if (current.isPresent()) {
                return current.get();
            }
        }

        // 2. Последняя по номеру версии
        List<ProductTemplateVersion> versions = productTemplateVersionRepository
                .findByTemplateIdOrderByVersionDesc(template.getId());
        return versions.isEmpty() ? null : versions.get(0);
    }

    /**
     * Проверить лимит активных версий шаблона.
     *
     * @param templateId ID шаблона
     * @param action     описание действия для сообщения об ошибке
     * @throws TemplateValidationException если лимит уже достигнут
     */
    private void ensureActiveVersionsLimit(Long templateId, String action) {
        long active = productTemplateVersionRepository
                .findByTemplateIdAndStatusIn(templateId, ACTIVE_STATUSES)
                .size();
        if (active >= MAX_ACTIVE_VERSIONS) {
            throw new TemplateValidationException(
                    "Нельзя " + action + ": у шаблона уже " + active
                            + " активных версий (лимит " + MAX_ACTIVE_VERSIONS + "). "
                            + "Архивируйте устаревшие версии.");
        }
    }

    private ProductTemplate findTemplateOrThrow(Long id) {
        return productTemplateRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Шаблон не найден: " + id));
    }

    private ProductTemplateVersion findVersionOrThrow(Long id) {
        return productTemplateVersionRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Версия шаблона не найдена: " + id));
    }

    private void validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new TemplateValidationException(fieldName + " не может быть пустым");
        }
    }

    private String trimOrNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void publishAudit(String username, AuditAction action, String details) {
        String actor = (username != null && !username.isBlank()) ? username : "system";
        eventPublisher.publishEvent(new AuditEvent(this, actor, action, details));
    }

    private ProductTemplateDto toTemplateDto(ProductTemplate template, List<ProductTemplateVersionDto> versions) {
        return new ProductTemplateDto(
                template.getId(),
                template.getSeriesId(),
                template.getName(),
                template.getDescription(),
                template.getCurrentVersionId(),
                template.getCreatedAt(),
                template.getCreatedBy(),
                versions
        );
    }

    private ProductTemplateVersionDto toVersionDto(ProductTemplateVersion version) {
        return new ProductTemplateVersionDto(
                version.getId(),
                version.getTemplateId(),
                version.getVersion(),
                version.getStatus(),
                version.getFieldsJson(),
                version.getMarkingRuleJson(),
                version.getCreatedAt(),
                version.getCreatedBy(),
                version.getPublishedAt(),
                version.getPublishedBy()
        );
    }

    // ==========================================================
    // VALIDATION
    // ==========================================================

    @Override
    public List<String> validateVersion(Long versionId) {
        ProductTemplateVersion version = findVersionOrThrow(versionId);
        return templateValidationService.collectErrors(version);
    }
}

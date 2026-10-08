package com.fanproduction.template.service.impl;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.FanTemplateDto;
import com.fanproduction.template.dto.FanTemplateVersionDto;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.FanTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FanTemplateServiceImpl implements FanTemplateService {

    private final FanTemplateRepository fanTemplateRepository;
    private final FanTemplateVersionRepository fanTemplateVersionRepository;
    private final ApplicationEventPublisher eventPublisher;

    // ==========================================================
    // TEMPLATE
    // ==========================================================

    @Override
    public List<FanTemplateDto> getAllTemplates() {
        return fanTemplateRepository.findAllByOrderByNameAsc().stream()
                .map(template -> toTemplateDto(template, null))
                .toList();
    }

    @Override
    public FanTemplateDto getTemplateById(Long id) {
        return toTemplateDto(findTemplateOrThrow(id), null);
    }

    @Override
    public FanTemplateDto getTemplateWithVersions(Long id) {
        FanTemplate template = findTemplateOrThrow(id);
        List<FanTemplateVersionDto> versions = fanTemplateVersionRepository
                .findByTemplateIdOrderByVersionDesc(id).stream()
                .map(this::toVersionDto)
                .toList();
        return toTemplateDto(template, versions);
    }

    @Override
    public FanTemplateDto getTemplateBySeries(Long seriesId) {
        FanTemplate template = fanTemplateRepository.findBySeriesId(seriesId)
                .orElseThrow(() -> new TemplateValidationException(
                        "Шаблон для серии не найден: " + seriesId));
        return toTemplateDto(template, null);
    }

    @Override
    @Transactional
    public FanTemplateDto createTemplate(Long seriesId,
                                         String name,
                                         String description,
                                         List<FieldDefinition> fields,
                                         MarkingRule markingRule,
                                         String createdBy) {
        validateNotEmpty(name, "Наименование шаблона");

        if (fanTemplateRepository.existsBySeriesId(seriesId)) {
            throw new TemplateValidationException("Для серии уже существует шаблон: " + seriesId);
        }

        FanTemplate template = new FanTemplate();
        template.setSeriesId(seriesId);
        template.setName(name.trim());
        template.setDescription(trimOrNull(description));
        template.setCreatedBy(createdBy);
        FanTemplate savedTemplate = fanTemplateRepository.save(template);

        FanTemplateVersion firstVersion = new FanTemplateVersion();
        firstVersion.setTemplateId(savedTemplate.getId());
        firstVersion.setVersion(1);
        firstVersion.setStatus(TemplateStatus.DRAFT);
        firstVersion.setFieldsJson(fields != null ? fields : List.of());
        firstVersion.setMarkingRuleJson(markingRule != null ? markingRule : MarkingRule.empty());
        firstVersion.setCreatedBy(createdBy);
        fanTemplateVersionRepository.save(firstVersion);

        publishAudit(createdBy, AuditAction.TEMPLATE_CREATE,
                "Создан шаблон: '" + savedTemplate.getName() + "' (серия ID=" + seriesId + ")");

        return toTemplateDto(savedTemplate, null);
    }

    @Override
    @Transactional
    public FanTemplateDto updateTemplate(Long id, String name, String description, String updatedBy) {
        FanTemplate template = findTemplateOrThrow(id);

        String newName = trimOrNull(name);
        if (newName != null && !newName.equals(template.getName())) {
            template.setName(newName);
        }
        String newDescription = trimOrNull(description);
        if (!Objects.equals(newDescription, template.getDescription())) {
            template.setDescription(newDescription);
        }

        FanTemplate saved = fanTemplateRepository.save(template);
        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён шаблон: '" + saved.getName() + "' (ID=" + id + ")");
        return toTemplateDto(saved, null);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long id, String deletedBy) {
        FanTemplate template = findTemplateOrThrow(id);
        fanTemplateRepository.delete(template);
        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалён шаблон: '" + template.getName() + "' (ID=" + id + ")");
    }

    // ==========================================================
    // VERSION — ЧТЕНИЕ
    // ==========================================================

    @Override
    public List<FanTemplateVersionDto> getVersionsByTemplate(Long templateId) {
        if (!fanTemplateRepository.existsById(templateId)) {
            throw new TemplateValidationException("Шаблон не найден: " + templateId);
        }
        return fanTemplateVersionRepository.findByTemplateIdOrderByVersionDesc(templateId).stream()
                .map(this::toVersionDto)
                .toList();
    }

    @Override
    public FanTemplateVersionDto getVersionById(Long versionId) {
        return toVersionDto(findVersionOrThrow(versionId));
    }

    @Override
    public FanTemplateVersionDto getPublishedVersion(Long templateId) {
        return fanTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(templateId, TemplateStatus.PUBLISHED)
                .map(this::toVersionDto)
                .orElse(null);
    }

    @Override
    public List<FanTemplateVersionDto> getAvailableVersions(Long templateId) {
        return fanTemplateVersionRepository
                .findByTemplateIdAndStatusIn(templateId,
                        Set.of(TemplateStatus.PUBLISHED, TemplateStatus.LEGACY))
                .stream()
                .map(this::toVersionDto)
                .toList();
    }

    @Override
    public FanTemplateVersionDto getDraftVersion(Long templateId) {
        return fanTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(templateId, TemplateStatus.DRAFT)
                .map(this::toVersionDto)
                .orElse(null);
    }

    // ==========================================================
    // VERSION — СОЗДАНИЕ И РЕДАКТИРОВАНИЕ
    // ==========================================================

    @Override
    @Transactional
    public FanTemplateVersionDto createNewVersion(Long templateId,
                                                  List<FieldDefinition> fields,
                                                  MarkingRule markingRule,
                                                  String createdBy) {
        FanTemplate template = findTemplateOrThrow(templateId);

        if (fanTemplateVersionRepository.existsByTemplateIdAndStatus(templateId, TemplateStatus.DRAFT)) {
            throw new TemplateValidationException(
                    "У шаблона уже есть черновик (DRAFT). Сначала опубликуйте или удалите его.");
        }

        int nextVersionNumber = fanTemplateVersionRepository.findMaxVersion(templateId)
                .orElse(0) + 1;

        FanTemplateVersion version = new FanTemplateVersion();
        version.setTemplateId(templateId);
        version.setVersion(nextVersionNumber);
        version.setStatus(TemplateStatus.DRAFT);
        version.setFieldsJson(fields != null ? fields : List.of());
        version.setMarkingRuleJson(markingRule != null ? markingRule : MarkingRule.empty());
        version.setCreatedBy(createdBy);

        FanTemplateVersion saved = fanTemplateVersionRepository.save(version);

        publishAudit(createdBy, AuditAction.TEMPLATE_UPDATE,
                "Создана черновая версия v" + nextVersionNumber
                        + " для шаблона '" + template.getName() + "'");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public FanTemplateVersionDto updateDraftVersion(Long versionId,
                                                    List<FieldDefinition> fields,
                                                    MarkingRule markingRule,
                                                    String updatedBy) {
        FanTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateValidationException(
                    "Можно редактировать только DRAFT-версии. Текущий статус: " + version.getStatus());
        }

        if (fields != null) version.setFieldsJson(fields);
        if (markingRule != null) version.setMarkingRuleJson(markingRule);

        FanTemplateVersion saved = fanTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Обновлён черновик v" + saved.getVersion() + " шаблона ID=" + saved.getTemplateId());

        return toVersionDto(saved);
    }

    // ==========================================================
    // VERSION — ЖИЗНЕННЫЙ ЦИКЛ
    // ==========================================================

    @Override
    @Transactional
    public FanTemplateVersionDto publishVersion(Long versionId, String publishedBy) {
        FanTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateValidationException(
                    "Опубликовать можно только DRAFT-версию. Текущий статус: " + version.getStatus());
        }

        FanTemplate template = findTemplateOrThrow(version.getTemplateId());

        fanTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(template.getId(), TemplateStatus.PUBLISHED)
                .ifPresent(oldPublished -> {
                    oldPublished.setStatus(TemplateStatus.LEGACY);
                    fanTemplateVersionRepository.save(oldPublished);
                });

        version.setStatus(TemplateStatus.PUBLISHED);
        version.setPublishedAt(LocalDateTime.now());
        version.setPublishedBy(publishedBy);
        FanTemplateVersion saved = fanTemplateVersionRepository.save(version);

        template.setCurrentVersionId(saved.getId());
        fanTemplateRepository.save(template);

        publishAudit(publishedBy, AuditAction.TEMPLATE_PUBLISH,
                "Опубликована версия v" + saved.getVersion()
                        + " шаблона '" + template.getName() + "'");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public FanTemplateVersionDto deprecateVersion(Long versionId, String updatedBy) {
        FanTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.LEGACY) {
            throw new TemplateValidationException(
                    "Деприкейт возможен только для LEGACY-версии. Текущий: " + version.getStatus());
        }

        version.setStatus(TemplateStatus.DEPRECATED);
        FanTemplateVersion saved = fanTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_DEPRECATE,
                "Версия v" + saved.getVersion() + " помечена как DEPRECATED");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public FanTemplateVersionDto archiveVersion(Long versionId, String updatedBy) {
        FanTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DEPRECATED) {
            throw new TemplateValidationException(
                    "Архивировать можно только DEPRECATED-версию. Текущий: " + version.getStatus());
        }

        version.setStatus(TemplateStatus.ARCHIVED);
        FanTemplateVersion saved = fanTemplateVersionRepository.save(version);

        publishAudit(updatedBy, AuditAction.TEMPLATE_UPDATE,
                "Версия v" + saved.getVersion() + " заархивирована");

        return toVersionDto(saved);
    }

    @Override
    @Transactional
    public void deleteVersion(Long versionId, String deletedBy) {
        FanTemplateVersion version = findVersionOrThrow(versionId);

        if (version.getStatus() != TemplateStatus.DRAFT
                && version.getStatus() != TemplateStatus.ARCHIVED) {
            throw new TemplateValidationException(
                    "Удалить можно только DRAFT или ARCHIVED. Текущий: " + version.getStatus());
        }

        FanTemplate template = findTemplateOrThrow(version.getTemplateId());
        if (Objects.equals(template.getCurrentVersionId(), versionId)) {
            throw new TemplateValidationException("Нельзя удалить текущую опубликованную версию");
        }

        fanTemplateVersionRepository.delete(version);

        publishAudit(deletedBy, AuditAction.TEMPLATE_DELETE,
                "Удалена версия v" + version.getVersion() + " шаблона ID=" + version.getTemplateId());
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private FanTemplate findTemplateOrThrow(Long id) {
        return fanTemplateRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Шаблон не найден: " + id));
    }

    private FanTemplateVersion findVersionOrThrow(Long id) {
        return fanTemplateVersionRepository.findById(id)
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

    private FanTemplateDto toTemplateDto(FanTemplate template, List<FanTemplateVersionDto> versions) {
        return new FanTemplateDto(
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

    private FanTemplateVersionDto toVersionDto(FanTemplateVersion version) {
        return new FanTemplateVersionDto(
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
}

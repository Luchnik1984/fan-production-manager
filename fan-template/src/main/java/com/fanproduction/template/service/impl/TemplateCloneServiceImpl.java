package com.fanproduction.template.service.impl;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.FanTemplateDto;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.repository.FanSeriesRepository;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.TemplateCloneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Реализация сервиса клонирования шаблонов.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateCloneServiceImpl implements TemplateCloneService {

    private final FanTemplateRepository fanTemplateRepository;
    private final FanTemplateVersionRepository fanTemplateVersionRepository;
    private final FanSeriesRepository fanSeriesRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public FanTemplateDto cloneTemplate(Long sourceTemplateId,
                                        Long targetSeriesId,
                                        String newName,
                                        String createdBy) {
        // 1. Источник
        FanTemplate source = fanTemplateRepository.findById(sourceTemplateId)
                .orElseThrow(() -> new TemplateValidationException(
                        "Шаблон-источник не найден: " + sourceTemplateId));

        // 2. Целевая серия
        if (!fanSeriesRepository.existsById(targetSeriesId)) {
            throw new TemplateValidationException("Целевая серия не найдена: " + targetSeriesId);
        }

        // 3. Целевая серия не должна иметь шаблон
        if (fanTemplateRepository.existsBySeriesId(targetSeriesId)) {
            throw new TemplateValidationException(
                    "У целевой серии уже есть шаблон. Используйте редактирование.");
        }

        // 4. Источник версии
        FanTemplateVersion sourceVersion = resolveSourceVersion(sourceTemplateId);
        if (sourceVersion == null) {
            throw new TemplateValidationException(
                    "У шаблона-источника нет ни одной версии. Клонирование невозможно.");
        }

        // 5. Новый FanTemplate
        FanTemplate target = new FanTemplate();
        target.setSeriesId(targetSeriesId);
        target.setName(resolveTargetName(newName, source));
        target.setDescription(source.getDescription());
        target.setCreatedBy(createdBy);
        FanTemplate savedTarget = fanTemplateRepository.save(target);

        // 6. Первая DRAFT-версия с копией полей и правила
        FanTemplateVersion newVersion = new FanTemplateVersion();
        newVersion.setTemplateId(savedTarget.getId());
        newVersion.setVersion(1);
        newVersion.setStatus(TemplateStatus.DRAFT);
        newVersion.setFieldsJson(sourceVersion.getFieldsJson());
        newVersion.setMarkingRuleJson(sourceVersion.getMarkingRuleJson());
        newVersion.setCreatedBy(createdBy);
        fanTemplateVersionRepository.save(newVersion);

        // 7. Аудит
        publishAudit(createdBy, AuditAction.TEMPLATE_CLONE,
                "Клонирован шаблон '" + source.getName()
                        + "' (ID=" + sourceTemplateId + ") → новый шаблон '"
                        + savedTarget.getName() + "' (ID=" + savedTarget.getId()
                        + ", серия ID=" + targetSeriesId + ")");

        log.info("Шаблон клонирован: source={} → target={}, series={}",
                sourceTemplateId, savedTarget.getId(), targetSeriesId);

        return toTemplateDto(savedTarget);
    }

    // ==========================================================
    // ВЫБОР ВЕРСИИ-ИСТОЧНИКА
    // ==========================================================

    /**
     * Определить версию-источник для клонирования:
     * <ol>
     *   <li>{@code current_version_id} — если задано.</li>
     *   <li>{@code DRAFT} — если есть.</li>
     *   <li>Последняя по номеру версии — в остальных случаях.</li>
     * </ol>
     */
    private FanTemplateVersion resolveSourceVersion(Long templateId) {
        // Текущая опубликованная
        FanTemplate source = fanTemplateRepository.findById(templateId).orElse(null);
        if (source != null && source.getCurrentVersionId() != null) {
            FanTemplateVersion current = fanTemplateVersionRepository
                    .findById(source.getCurrentVersionId())
                    .orElse(null);
            if (current != null) {
                return current;
            }
        }

        // DRAFT
        FanTemplateVersion draft = fanTemplateVersionRepository
                .findFirstByTemplateIdAndStatus(templateId, TemplateStatus.DRAFT)
                .orElse(null);
        if (draft != null) {
            return draft;
        }

        // Последняя по номеру
        List<FanTemplateVersion> versions = fanTemplateVersionRepository
                .findByTemplateIdOrderByVersionDesc(templateId);
        return versions.isEmpty() ? null : versions.get(0);
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private String resolveTargetName(String newName, FanTemplate source) {
        if (newName != null && !newName.isBlank()) {
            return newName.trim();
        }
        return source.getName();
    }

    private void publishAudit(String username, AuditAction action, String details) {
        String actor = (username != null && !username.isBlank()) ? username : "system";
        eventPublisher.publishEvent(new AuditEvent(this, actor, action, details));
    }

    private FanTemplateDto toTemplateDto(FanTemplate template) {
        return new FanTemplateDto(
                template.getId(),
                template.getSeriesId(),
                template.getName(),
                template.getDescription(),
                template.getCurrentVersionId(),
                template.getCreatedAt(),
                template.getCreatedBy(),
                null    // версии не загружаем
        );
    }
}

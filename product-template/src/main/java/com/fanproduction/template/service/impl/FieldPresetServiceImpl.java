package com.fanproduction.template.service.impl;

import com.fanproduction.core.enums.AuditAction;
import com.fanproduction.core.event.AuditEvent;
import com.fanproduction.template.dto.FieldPresetDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.entity.FieldPreset;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.enums.TemplateStatus;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.repository.FieldPresetRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.service.FieldPresetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация сервиса пресетов полей (US10.6).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FieldPresetServiceImpl implements FieldPresetService {

    private final FieldPresetRepository fieldPresetRepository;
    private final ProductTemplateVersionRepository productTemplateVersionRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public List<FieldPresetDto> getAll() {
        return fieldPresetRepository.findAllByOrderByNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public FieldPresetDto getById(Long id) {
        return toDto(findPresetOrThrow(id));
    }

    @Override
    @Transactional
    public FieldPresetDto create(String name, String description,
                                 List<FieldDefinition> fields,
                                 com.fanproduction.template.model.MarkingRule markingRule,
                                 String createdBy) {
        String cleanName = validateName(name);
        if (fieldPresetRepository.existsByNameIgnoreCase(cleanName)) {
            throw new TemplateValidationException("Пресет с названием '" + cleanName + "' уже существует");
        }
        if (fields == null || fields.isEmpty()) {
            throw new TemplateValidationException("Пресет должен содержать хотя бы одно поле");
        }

        FieldPreset preset = new FieldPreset();
        preset.setName(cleanName);
        preset.setDescription(description);
        preset.setFieldsJson(fields);
        preset.setMarkingRuleJson(markingRule != null
                ? markingRule
                : com.fanproduction.template.model.MarkingRule.empty());
        preset.setIsSystem(false);
        preset.setCreatedBy(createdBy);

        FieldPreset saved = fieldPresetRepository.save(preset);
        publishAudit(createdBy, "Создан пресет полей '" + saved.getName()
                + "' (" + fields.size() + " полей)");

        return toDto(saved);
    }

    @Override
    @Transactional
    public FieldPresetDto update(Long id, String name, String description,
                                 List<FieldDefinition> fields,
                                 com.fanproduction.template.model.MarkingRule markingRule,
                                 String updatedBy) {
        FieldPreset preset = findPresetOrThrow(id);
        guardSystem(preset, "изменить");

        if (name != null) {
            String cleanName = validateName(name);
            if (!cleanName.equalsIgnoreCase(preset.getName())
                    && fieldPresetRepository.existsByNameIgnoreCase(cleanName)) {
                throw new TemplateValidationException("Пресет с названием '" + cleanName + "' уже существует");
            }
            preset.setName(cleanName);
        }
        if (description != null) {
            preset.setDescription(description);
        }
        if (fields != null) {
            if (fields.isEmpty()) {
                throw new TemplateValidationException("Пресет должен содержать хотя бы одно поле");
            }
            preset.setFieldsJson(fields);
        }
        if (markingRule != null) {
            preset.setMarkingRuleJson(markingRule);
        }

        FieldPreset saved = fieldPresetRepository.save(preset);
        publishAudit(updatedBy, "Обновлён пресет полей '" + saved.getName() + "'");

        return toDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, String deletedBy) {
        FieldPreset preset = findPresetOrThrow(id);
        guardSystem(preset, "удалить");

        fieldPresetRepository.delete(preset);
        publishAudit(deletedBy, "Удалён пресет полей '" + preset.getName() + "'");
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto applyToVersion(Long versionId, Long presetId, String updatedBy) {
        return applyToVersion(versionId, presetId, false, updatedBy);
    }

    @Override
    @Transactional
    public ProductTemplateVersionDto applyToVersion(Long versionId, Long presetId,
                                                    boolean replace, String updatedBy) {
        ProductTemplateVersion version = productTemplateVersionRepository.findById(versionId)
                .orElseThrow(() -> new TemplateValidationException("Версия шаблона не найдена: " + versionId));

        if (version.getStatus() != TemplateStatus.DRAFT) {
            throw new TemplateValidationException(
                    "Пресет можно применить только к DRAFT-версии. Текущий статус: " + version.getStatus());
        }

        FieldPreset preset = findPresetOrThrow(presetId);

        List<FieldDefinition> current = replace
                ? new ArrayList<>()
                : (version.getFieldsJson() != null
                        ? new ArrayList<>(version.getFieldsJson())
                        : new ArrayList<>());

        Set<String> existingKeys = current.stream()
                .map(FieldDefinition::key)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        int added = 0;
        for (FieldDefinition field : preset.getFieldsJson()) {
            if (field.key() == null || existingKeys.contains(field.key())) {
                continue;
            }
            current.add(field);
            existingKeys.add(field.key());
            added++;
        }

        version.setFieldsJson(current);

        // US10.7.3: правило из пресета применяется, только если у версии его нет —
        // чтобы не перетереть работу администратора
        boolean ruleApplied = false;
        if (isRuleEmpty(version.getMarkingRuleJson()) && !isRuleEmpty(preset.getMarkingRuleJson())) {
            version.setMarkingRuleJson(preset.getMarkingRuleJson());
            ruleApplied = true;
        }

        ProductTemplateVersion saved = productTemplateVersionRepository.save(version);

        String mode = replace ? "режим замены" : "режим добавления";
        if (ruleApplied) {
            mode += ", правило маркировки скопировано";
        }
        publishAudit(updatedBy, "К версии v" + saved.getVersion() + " применён пресет '"
                + preset.getName() + "' (" + mode + "): добавлено полей — " + added);

        log.info("Пресет применён: versionId={}, presetId={}, replace={}, added={}",
                versionId, presetId, replace, added);

        return toVersionDto(saved);
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private FieldPreset findPresetOrThrow(Long id) {
        return fieldPresetRepository.findById(id)
                .orElseThrow(() -> new TemplateValidationException("Пресет не найден: " + id));
    }

    private void guardSystem(FieldPreset preset, String action) {
        if (Boolean.TRUE.equals(preset.getIsSystem())) {
            throw new TemplateValidationException(
                    "Нельзя " + action + " системный пресет '" + preset.getName() + "'");
        }
    }

    /** Правило пустое, если в нём нет ни одного элемента. */
    private boolean isRuleEmpty(com.fanproduction.template.model.MarkingRule rule) {
        return rule == null || rule.sequence() == null || rule.sequence().isEmpty();
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new TemplateValidationException("Название пресета обязательно");
        }
        return name.trim();
    }

    private void publishAudit(String username, String details) {
        String actor = (username != null && !username.isBlank()) ? username : "system";
        eventPublisher.publishEvent(new AuditEvent(this, actor, AuditAction.TEMPLATE_UPDATE, details));
    }

    private FieldPresetDto toDto(FieldPreset preset) {
        return new FieldPresetDto(
                preset.getId(),
                preset.getName(),
                preset.getDescription(),
                preset.getFieldsJson(),
                preset.getMarkingRuleJson(),
                preset.getIsSystem(),
                preset.getCreatedAt(),
                preset.getCreatedBy()
        );
    }

    private ProductTemplateVersionDto toVersionDto(ProductTemplateVersion v) {
        return new ProductTemplateVersionDto(
                v.getId(),
                v.getTemplateId(),
                v.getVersion(),
                v.getStatus(),
                v.getFieldsJson(),
                v.getMarkingRuleJson(),
                v.getCreatedAt(),
                v.getCreatedBy(),
                v.getPublishedAt(),
                v.getPublishedBy()
        );
    }
}

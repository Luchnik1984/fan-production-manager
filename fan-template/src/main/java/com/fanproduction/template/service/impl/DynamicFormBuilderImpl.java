package com.fanproduction.template.service.impl;

import com.fanproduction.template.dto.DynamicFormFieldDto;
import com.fanproduction.template.dto.DynamicFormMetadataDto;
import com.fanproduction.template.entity.FanTemplate;
import com.fanproduction.template.entity.FanTemplateVersion;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.repository.FanTemplateRepository;
import com.fanproduction.template.repository.FanTemplateVersionRepository;
import com.fanproduction.template.service.DynamicFormBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Реализация построителя метаданных динамической формы.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DynamicFormBuilderImpl implements DynamicFormBuilder {

    private final FanTemplateRepository fanTemplateRepository;
    private final FanTemplateVersionRepository fanTemplateVersionRepository;

    @Override
    public DynamicFormMetadataDto buildFormMetadata(Long templateVersionId) {
        if (templateVersionId == null) {
            throw new TemplateValidationException("ID версии шаблона не может быть пустым");
        }

        FanTemplateVersion version = fanTemplateVersionRepository.findById(templateVersionId)
                .orElseThrow(() -> new TemplateValidationException(
                        "Версия шаблона не найдена: " + templateVersionId));

        FanTemplate template = fanTemplateRepository.findById(version.getTemplateId())
                .orElseThrow(() -> new TemplateValidationException(
                        "Шаблон не найден: " + version.getTemplateId()));

        List<DynamicFormFieldDto> fields = buildFields(version.getFieldsJson());

        log.debug("Построены метаданные формы: templateVersionId={}, полей={}",
                templateVersionId, fields.size());

        return new DynamicFormMetadataDto(
                template.getId(),
                version.getId(),
                version.getVersion(),
                version.getStatus(),
                template.getName(),
                fields
        );
    }

    // ==========================================================
    // ПОСТРОЕНИЕ СПИСКА ПОЛЕЙ
    // ==========================================================

    private List<DynamicFormFieldDto> buildFields(List<FieldDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return List.of();
        }

        return definitions.stream()
                // Исключаем служебные типы: SEPARATOR и HIDDEN
                .filter(def -> def.type() != FieldType.SEPARATOR
                        && def.type() != FieldType.HIDDEN)
                // Сортировка: сначала по displayOrder, null — в конце
                .sorted(Comparator.comparing(
                        FieldDefinition::displayOrder,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(this::toFieldDto)
                .toList();
    }

    private DynamicFormFieldDto toFieldDto(FieldDefinition def) {
        return new DynamicFormFieldDto(
                def.key(),
                def.label(),
                def.type(),
                def.required(),
                def.defaultValue(),
                def.options(),
                def.hint(),
                def.referenceType(),
                def.targetFieldName(),
                def.role(),
                def.addToProduct(),
                def.pullFrom(),
                def.visibleIf(),
                def.requiredIf(),
                def.onSelect()
        );
    }
}

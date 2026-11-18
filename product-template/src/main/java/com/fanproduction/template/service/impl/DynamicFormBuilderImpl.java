package com.fanproduction.template.service.impl;

import com.fanproduction.template.dto.DynamicFormFieldDto;
import com.fanproduction.template.dto.DynamicFormMetadataDto;
import com.fanproduction.template.entity.ProductTemplate;
import com.fanproduction.template.entity.ProductTemplateVersion;
import com.fanproduction.template.entity.ProductSeries;
import com.fanproduction.template.entity.ProductSpecies;
import com.fanproduction.template.entity.ProductType;
import com.fanproduction.template.enums.FieldType;
import com.fanproduction.template.exception.TemplateValidationException;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.repository.ProductTemplateRepository;
import com.fanproduction.template.repository.ProductTemplateVersionRepository;
import com.fanproduction.template.repository.ProductSeriesRepository;
import com.fanproduction.template.repository.ProductSpeciesRepository;
import com.fanproduction.template.repository.ProductTypeRepository;
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

    private final ProductTemplateRepository productTemplateRepository;
    private final ProductTemplateVersionRepository productTemplateVersionRepository;
    private final ProductSeriesRepository productSeriesRepository;
    private final ProductSpeciesRepository productSpeciesRepository;
    private final ProductTypeRepository productTypeRepository;

    @Override
    public DynamicFormMetadataDto buildFormMetadata(Long templateVersionId) {
        if (templateVersionId == null) {
            throw new TemplateValidationException("ID версии шаблона не может быть пустым");
        }

        ProductTemplateVersion version = productTemplateVersionRepository.findById(templateVersionId)
                .orElseThrow(() -> new TemplateValidationException(
                        "Версия шаблона не найдена: " + templateVersionId));

        ProductTemplate template = productTemplateRepository.findById(version.getTemplateId())
                .orElseThrow(() -> new TemplateValidationException(
                        "Шаблон не найден: " + version.getTemplateId()));

        // Цепочка: series → species → type
        Long typeId = null;
        String typeName = null;
        String typeDesignation = null;
        Long speciesId = null;
        String speciesName = null;
        Long seriesId = null;
        String seriesName = null;

        ProductSeries series = productSeriesRepository.findById(template.getSeriesId()).orElse(null);
        if (series != null) {
            seriesId = series.getId();
            seriesName = series.getName();

            ProductSpecies species = productSpeciesRepository.findById(series.getSpeciesId()).orElse(null);
            if (species != null) {
                speciesId = species.getId();
                speciesName = species.getName();

                ProductType type = productTypeRepository.findById(species.getTypeId()).orElse(null);
                if (type != null) {
                    typeId = type.getId();
                    typeName = type.getName();
                    typeDesignation = type.getDesignation();
                }
            }
        }

        List<DynamicFormFieldDto> fields = buildFields(version.getFieldsJson());

        log.debug("Построены метаданные формы: versionId={}, type={}, series={}, полей={}",
                templateVersionId, typeName, seriesName, fields.size());

        return new DynamicFormMetadataDto(
                template.getId(),
                version.getId(),
                version.getVersion(),
                version.getStatus(),
                template.getName(),
                typeId,
                typeName,
                typeDesignation,
                speciesId,
                speciesName,
                seriesId,
                seriesName,
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

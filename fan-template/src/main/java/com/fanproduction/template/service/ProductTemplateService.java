package com.fanproduction.template.service;

import com.fanproduction.template.dto.ProductTemplateDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;

/**
 * Сервис управления шаблонами вентиляторов и их версиями.
 */
public interface ProductTemplateService {

    // ========== Template ==========

    List<ProductTemplateDto> getAllTemplates();

    ProductTemplateDto getTemplateById(Long id);

    ProductTemplateDto getTemplateWithVersions(Long id);

    ProductTemplateDto getTemplateBySeries(Long seriesId);

    ProductTemplateDto createTemplate(Long seriesId,
                                  String name,
                                  String description,
                                  List<FieldDefinition> fields,
                                  MarkingRule markingRule,
                                  String createdBy);

    ProductTemplateDto updateTemplate(Long id, String name, String description, String updatedBy);

    void deleteTemplate(Long id, String deletedBy);

    // ========== Version ==========

    List<ProductTemplateVersionDto> getVersionsByTemplate(Long templateId);

    ProductTemplateVersionDto getVersionById(Long versionId);

    ProductTemplateVersionDto getPublishedVersion(Long templateId);

    List<ProductTemplateVersionDto> getAvailableVersions(Long templateId);

    ProductTemplateVersionDto getDraftVersion(Long templateId);

    ProductTemplateVersionDto createNewVersion(Long templateId,
                                           List<FieldDefinition> fields,
                                           MarkingRule markingRule,
                                           String createdBy);

    ProductTemplateVersionDto updateDraftVersion(Long versionId,
                                             List<FieldDefinition> fields,
                                             MarkingRule markingRule,
                                             String updatedBy);

    ProductTemplateVersionDto publishVersion(Long versionId, String publishedBy);

    ProductTemplateVersionDto deprecateVersion(Long versionId, String updatedBy);

    ProductTemplateVersionDto archiveVersion(Long versionId, String updatedBy);

    void deleteVersion(Long versionId, String deletedBy);

    // ========== Validation ==========

    /**
     * Собрать список ошибок валидации версии шаблона.
     * Используется GUI для предварительной проверки перед публикацией.
     */
    List<String> validateVersion(Long versionId);
}

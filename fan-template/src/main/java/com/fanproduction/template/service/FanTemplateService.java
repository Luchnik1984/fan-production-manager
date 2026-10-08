package com.fanproduction.template.service;

import com.fanproduction.template.dto.FanTemplateDto;
import com.fanproduction.template.dto.FanTemplateVersionDto;
import com.fanproduction.template.model.FieldDefinition;
import com.fanproduction.template.model.MarkingRule;

import java.util.List;

/**
 * Сервис управления шаблонами вентиляторов и их версиями.
 */
public interface FanTemplateService {

    // ========== Template ==========

    List<FanTemplateDto> getAllTemplates();

    FanTemplateDto getTemplateById(Long id);

    FanTemplateDto getTemplateWithVersions(Long id);

    FanTemplateDto getTemplateBySeries(Long seriesId);

    FanTemplateDto createTemplate(Long seriesId,
                                  String name,
                                  String description,
                                  List<FieldDefinition> fields,
                                  MarkingRule markingRule,
                                  String createdBy);

    FanTemplateDto updateTemplate(Long id, String name, String description, String updatedBy);

    void deleteTemplate(Long id, String deletedBy);

    // ========== Version ==========

    List<FanTemplateVersionDto> getVersionsByTemplate(Long templateId);

    FanTemplateVersionDto getVersionById(Long versionId);

    FanTemplateVersionDto getPublishedVersion(Long templateId);

    List<FanTemplateVersionDto> getAvailableVersions(Long templateId);

    FanTemplateVersionDto getDraftVersion(Long templateId);

    FanTemplateVersionDto createNewVersion(Long templateId,
                                           List<FieldDefinition> fields,
                                           MarkingRule markingRule,
                                           String createdBy);

    FanTemplateVersionDto updateDraftVersion(Long versionId,
                                             List<FieldDefinition> fields,
                                             MarkingRule markingRule,
                                             String updatedBy);

    FanTemplateVersionDto publishVersion(Long versionId, String publishedBy);

    FanTemplateVersionDto deprecateVersion(Long versionId, String updatedBy);

    FanTemplateVersionDto archiveVersion(Long versionId, String updatedBy);

    void deleteVersion(Long versionId, String deletedBy);
}

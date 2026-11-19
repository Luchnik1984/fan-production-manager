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

    /**
     * Восстановить версию из архива: {@code ARCHIVED → DEPRECATED}.
     * <p>
     * Нужно, если администратор ошибся с архивацией. Возврат возможен только
     * на один шаг назад — в «мёртвый» статус, а не в редактируемый.
     *
     * @param versionId ID версии в статусе ARCHIVED
     * @param updatedBy email администратора (для аудита)
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если версия не в статусе ARCHIVED
     */
    ProductTemplateVersionDto restoreVersion(Long versionId, String updatedBy);

    /**
     * Снять деприкейт: {@code DEPRECATED → LEGACY}.
     * <p>
     * Разрешено, только если количество активных версий шаблона
     * (DRAFT + PUBLISHED + LEGACY) меньше лимита.
     *
     * @param versionId ID версии в статусе DEPRECATED
     * @param updatedBy email администратора (для аудита)
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если версия не в статусе DEPRECATED или превышен лимит активных версий
     */
    ProductTemplateVersionDto undeprecateVersion(Long versionId, String updatedBy);

    /**
     * Создать DRAFT-версию на основе выбранной версии шаблона.
     * <p>
     * Позволяет «отредактировать старую версию», не нарушая иммутабельность:
     * исходная версия остаётся в своём статусе (LEGACY/DEPRECATED/ARCHIVED),
     * а новая создаётся как копия её полей и правила маркировки.
     *
     * @param templateId      ID шаблона
     * @param sourceVersionId версия-источник; {@code null} — текущая/последняя
     * @param createdBy       email администратора (для аудита)
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если у шаблона уже есть DRAFT, источник принадлежит другому шаблону
     *         или превышен лимит активных версий
     */
    ProductTemplateVersionDto createVersionFrom(Long templateId, Long sourceVersionId, String createdBy);

    void deleteVersion(Long versionId, String deletedBy);

    // ========== Validation ==========

    /**
     * Собрать список ошибок валидации версии шаблона.
     * Используется GUI для предварительной проверки перед публикацией.
     */
    List<String> validateVersion(Long versionId);
}

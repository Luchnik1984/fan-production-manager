package com.fanproduction.template.service;

import com.fanproduction.template.dto.FanTemplateDto;
import com.fanproduction.template.dto.FanTemplateVersionDto;

import java.util.List;
import java.util.Map;

/**
 * Сервис управления шаблонами вентиляторов и их версиями.
 * <p>
 * Особенности:
 * <ul>
 *   <li>При создании шаблона автоматически создаётся первая версия со статусом DRAFT.</li>
 *   <li>У шаблона может быть только одна DRAFT-версия одновременно.</li>
 *   <li>При публикации версии предыдущая PUBLISHED автоматически переходит в LEGACY.</li>
 *   <li>Версия в статусе PUBLISHED, LEGACY, DEPRECATED не может быть изменена.</li>
 *   <li>Удалить можно только DRAFT и ARCHIVED версии.</li>
 *   <li>Все операции пишут событие аудита.</li>
 * </ul>
 */
public interface FanTemplateService {

    // ========== Template ==========

    /**
     * Все шаблоны (без версий).
     */
    List<FanTemplateDto> getAllTemplates();

    /**
     * Найти шаблон по ID (без версий).
     */
    FanTemplateDto getTemplateById(Long id);

    /**
     * Найти шаблон по ID вместе со всеми версиями.
     */
    FanTemplateDto getTemplateWithVersions(Long id);

    /**
     * Найти шаблон по серии.
     */
    FanTemplateDto getTemplateBySeries(Long seriesId);

    /**
     * Создать шаблон с первой DRAFT-версией.
     *
     * @param seriesId      ID серии
     * @param name          наименование шаблона
     * @param description   описание (опционально)
     * @param fieldsJson    список полей (JSONB, пока Map)
     * @param markingRuleJson правило маркировки (JSONB, пока Map)
     * @param createdBy     email создателя
     */
    FanTemplateDto createTemplate(Long seriesId,
                                  String name,
                                  String description,
                                  Map<String, Object> fieldsJson,
                                  Map<String, Object> markingRuleJson,
                                  String createdBy);

    /**
     * Обновить метаданные шаблона (name, description).
     * Не изменяет версии.
     */
    FanTemplateDto updateTemplate(Long id, String name, String description, String updatedBy);

    /**
     * Удалить шаблон вместе со всеми версиями.
     * Запрещено, если на шаблон ссылаются карточки (проверка — на уровне выше, TODO).
     */
    void deleteTemplate(Long id, String deletedBy);

    // ========== Version ==========

    /**
     * Все версии шаблона (от новых к старым).
     */
    List<FanTemplateVersionDto> getVersionsByTemplate(Long templateId);

    /**
     * Найти версию по ID.
     */
    FanTemplateVersionDto getVersionById(Long versionId);

    /**
     * Текущая PUBLISHED версия шаблона (если есть).
     */
    FanTemplateVersionDto getPublishedVersion(Long templateId);

    /**
     * Версии, доступные для создания карточек: PUBLISHED + LEGACY.
     */
    List<FanTemplateVersionDto> getAvailableVersions(Long templateId);

    /**
     * Найти DRAFT-версию шаблона (если есть).
     */
    FanTemplateVersionDto getDraftVersion(Long templateId);

    /**
     * Создать новую DRAFT-версию.
     * Запрещено, если у шаблона уже есть DRAFT.
     * Номер новой версии = max(version) + 1.
     */
    FanTemplateVersionDto createNewVersion(Long templateId,
                                           Map<String, Object> fieldsJson,
                                           Map<String, Object> markingRuleJson,
                                           String createdBy);

    /**
     * Обновить содержимое DRAFT-версии.
     * Запрещено для версий в других статусах.
     */
    FanTemplateVersionDto updateDraftVersion(Long versionId,
                                             Map<String, Object> fieldsJson,
                                             Map<String, Object> markingRuleJson,
                                             String updatedBy);

    /**
     * Опубликовать DRAFT-версию.
     * Предыдущая PUBLISHED автоматически становится LEGACY.
     * Обновляется current_version_id шаблона.
     */
    FanTemplateVersionDto publishVersion(Long versionId, String publishedBy);

    /**
     * Пометить LEGACY-версию как DEPRECATED.
     */
    FanTemplateVersionDto deprecateVersion(Long versionId, String updatedBy);

    /**
     * Архивировать DEPRECATED-версию.
     */
    FanTemplateVersionDto archiveVersion(Long versionId, String updatedBy);

    /**
     * Удалить DRAFT или ARCHIVED версию.
     * Запрещено, если это current_version_id шаблона.
     */
    void deleteVersion(Long versionId, String deletedBy);
}

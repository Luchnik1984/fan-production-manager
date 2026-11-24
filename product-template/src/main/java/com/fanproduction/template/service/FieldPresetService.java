package com.fanproduction.template.service;

import com.fanproduction.template.dto.FieldPresetDto;
import com.fanproduction.template.dto.ProductTemplateVersionDto;
import com.fanproduction.template.model.FieldDefinition;

import java.util.List;

/**
 * Сервис пресетов полей шаблона (US10.6).
 * <p>
 * Пресет — готовый набор полей для типового изделия. Применение к версии
 * шаблона <b>добавляет недостающие поля</b> и не трогает уже существующие
 * с тем же ключом, поэтому пресет можно применять повторно.
 */
public interface FieldPresetService {

    /**
     * Все пресеты в алфавитном порядке.
     */
    List<FieldPresetDto> getAll();

    /**
     * Пресет по ID.
     */
    FieldPresetDto getById(Long id);

    /**
     * Создать пользовательский пресет.
     *
     * @param name        название (уникальное)
     * @param description описание (необязательно)
     * @param fields      набор полей
     * @param createdBy   email автора
     */
    FieldPresetDto create(String name, String description,
                        List<FieldDefinition> fields, com.fanproduction.template.model.MarkingRule markingRule,
                        String createdBy);

    /**
     * Изменить пользовательский пресет.
     * Системные пресеты редактировать нельзя.
     */
    FieldPresetDto update(Long id, String name, String description,
                          List<FieldDefinition> fields, com.fanproduction.template.model.MarkingRule markingRule,
                               String updatedBy);

    /**
     * Удалить пользовательский пресет.
     * Системные удалять нельзя.
     */
    void delete(Long id, String deletedBy);

    /**
     * Применить пресет к DRAFT-версии шаблона.
     * <p>
     * Поля пресета, которых ещё нет в версии (по {@code key}), добавляются
     * в конец. Существующие поля не изменяются.
     *
     * @param versionId ID версии в статусе DRAFT
     * @param presetId  ID пресета
     * @param updatedBy email администратора (для аудита)
     * @return обновлённая версия с итоговым набором полей
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если версия не найдена, не в статусе DRAFT или пресет не найден
     */
    ProductTemplateVersionDto applyToVersion(Long versionId, Long presetId, String updatedBy);

    /**
     * Применить пресет к DRAFT-версии шаблона с выбором режима.
     *
     * @param replace {@code true} — заменить все поля полями пресета;
     *                {@code false} — добавить только недостающие (по {@code key})
     */
    ProductTemplateVersionDto applyToVersion(Long versionId, Long presetId, boolean replace, String updatedBy);
}

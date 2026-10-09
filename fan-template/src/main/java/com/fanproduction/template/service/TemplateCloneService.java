package com.fanproduction.template.service;

import com.fanproduction.template.dto.FanTemplateDto;

/**
 * Сервис клонирования шаблонов вентиляторов.
 * <p>
 * Позволяет создать новый шаблон для другой серии на основе существующего.
 * Копируются поля ({@code fields_json}) и правило маркировки ({@code marking_rule_json}).
 * <p>
 * Новый шаблон получает первую версию со статусом {@code DRAFT}.
 * Администратор затем правит её под новую серию и публикует.
 * <p>
 * См. FR4.5 (клонирование шаблонов).
 */
public interface TemplateCloneService {

    /**
     * Клонировать шаблон для другой серии.
     * <p>
     * Источник выбирается так:
     * <ol>
     *   <li>Если у источника есть {@code current_version_id} — берётся эта версия.</li>
     *   <li>Иначе — {@code DRAFT}.</li>
     *   <li>Иначе — последняя по номеру версии.</li>
     * </ol>
     *
     * @param sourceTemplateId ID шаблона-источника
     * @param targetSeriesId   ID целевой серии
     * @param newName          наименование нового шаблона
     *                         (если {@code null} — берётся из источника)
     * @param createdBy        email создателя (для аудита)
     * @return DTO нового шаблона
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если источник не найден, целевая серия не найдена,
     *         целевая серия уже имеет шаблон, или у источника нет версий
     */
    FanTemplateDto cloneTemplate(Long sourceTemplateId,
                                 Long targetSeriesId,
                                 String newName,
                                 String createdBy);
}

package com.fanproduction.template.service;

import com.fanproduction.template.dto.DynamicFormMetadataDto;

/**
 * Построитель метаданных динамической формы карточки вентилятора.
 * <p>
 * По версии шаблона строит DTO, содержащий список полей формы,
 * готовый для отрисовки в GUI.
 * <p>
 * Правила:
 * <ul>
 *   <li>Поля сортируются по {@code displayOrder}. Поля с {@code null} —
 *       в конце в порядке появления в шаблоне.</li>
 *   <li>Поля типа {@code SEPARATOR} и {@code HIDDEN} не включаются
 *       в список полей формы (SEPARATOR → секции, HIDDEN → скрытые).</li>
 *   <li>{@code visibleIf} и {@code requiredIf} не применяются —
 *       передаются клиенту «как есть», GUI сам решает.</li>
 * </ul>
 */
public interface DynamicFormBuilder {

    /**
     * Построить метаданные формы по ID версии шаблона.
     *
     * @param templateVersionId ID версии шаблона
     * @return метаданные формы
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если версия не найдена
     */
    DynamicFormMetadataDto buildFormMetadata(Long templateVersionId);
}

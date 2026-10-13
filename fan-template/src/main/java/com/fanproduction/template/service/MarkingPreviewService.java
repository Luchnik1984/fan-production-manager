package com.fanproduction.template.service;

import java.util.Map;

/**
 * Сервис предпросмотра маркировки.
 * <p>
 * Вычисляет полную маркировку по значениям полей и версии шаблона,
 * без сохранения карточки. Используется GUI для real-time обновления
 * поля «Полная маркировка».
 * <p>
 * Отличается от {@code FanCardService.createFromTemplate} тем, что
 * не создаёт карточку и не делает проверок уникальности.
 */
public interface MarkingPreviewService {

    /**
     * Вычислить маркировку по значениям и версии шаблона.
     *
     * @param templateVersionId ID версии шаблона
     * @param values            значения полей
     * @return вычисленная маркировка или пустая строка, если не удалось
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если версия не найдена
     */
    String preview(Long templateVersionId, Map<String, Object> values);
}

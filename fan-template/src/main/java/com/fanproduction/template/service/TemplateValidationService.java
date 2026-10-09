package com.fanproduction.template.service;

import com.fanproduction.template.entity.FanTemplateVersion;

import java.util.List;

/**
 * Сервис валидации шаблонов.
 * <p>
 * Проверяет корректность {@code fields_json} и {@code marking_rule_json}
 * перед публикацией версии шаблона.
 * <p>
 * См. FR4.5.6.
 */
public interface TemplateValidationService {

    /**
     * Проверить версию шаблона.
     * <p>
     * Если найдены ошибки — бросает {@link com.fanproduction.template.exception.TemplateValidationException}
     * с перечислением всех ошибок.
     *
     * @param version версия шаблона
     * @throws com.fanproduction.template.exception.TemplateValidationException при ошибках
     */
    void validate(FanTemplateVersion version);

    /**
     * Собрать список ошибок без бросания исключения.
     * <p>
     * Используется GUI для показа списка ошибок перед публикацией.
     *
     * @param version версия шаблона
     * @return список сообщений об ошибках (пустой, если всё корректно)
     */
    List<String> collectErrors(FanTemplateVersion version);
}

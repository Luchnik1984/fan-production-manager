package com.fanproduction.services;

import com.fanproduction.core.entity.product.FanCardEntity;

import java.util.Map;

/**
 * Сервис управления карточками вентиляторов (FAN).
 * <p>
 * Отвечает за:
 * <ul>
 *   <li>Создание карточки по шаблону (с раскладкой полей на системные
 *       и динамические, вычислением {@code full_marking}).</li>
 *   <li>Обновление карточки (пересчёт маркировки, обновление полей).</li>
 *   <li>Пересчёт маркировки без изменения полей (для миграции версий).</li>
 * </ul>
 * <p>
 * Использует:
 * <ul>
 *   <li>{@code FanTemplateVersion} — определение полей и правила.</li>
 *   <li>{@code MarkingEngine} — вычисление маркировки.</li>
 *   <li>{@code FanCardRepository} — сохранение.</li>
 * </ul>
 */
public interface FanCardService {

    /**
     * Создать карточку FAN по версии шаблона.
     *
     * @param templateVersionId ID версии шаблона
     * @param values            значения полей из формы
     * @param createdBy         email создателя
     * @return сохранённая карточка
     * @throws com.fanproduction.services.exception.FanCardCreationException
     *         при ошибках валидации
     */
    FanCardEntity createFromTemplate(Long templateVersionId,
                                     Map<String, Object> values,
                                     String createdBy);
}

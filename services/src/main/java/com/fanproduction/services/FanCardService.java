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

    /**
     * Обновить существующую FAN-карточку.
     * <p>
     * Значения из {@code values} применяются «как есть»: поля, которых нет
     * в {@code values}, будут очищены. Это соответствует семантике
     * «форма отправляется целиком».
     * <p>
     * Маркировка пересчитывается по текущей версии шаблона карточки.
     * <p>
     * Технические поля {@code templateVersionId} и {@code fanTemplateId}
     * не меняются.
     *
     * @param cardId    ID карточки
     * @param values    новые значения полей из формы
     * @param updatedBy email пользователя
     * @return сохранённая карточка
     * @throws com.fanproduction.services.exception.FanCardCreationException
     *         при ошибках валидации
     */
    FanCardEntity updateFromValues(Long cardId,
                                   Map<String, Object> values,
                                   String updatedBy);

    /**
     * Пересчитать полную маркировку карточки без изменения полей.
     * <p>
     * Используется при:
     * <ul>
     *   <li>Миграции карточки на новую версию шаблона (после смены
     *       {@code templateVersionId}).</li>
     *   <li>«Починке» некорректной маркировки.</li>
     * </ul>
     * <p>
     * Значения полей (системные из колонок + динамические из JSONB) собираются
     * из текущего состояния карточки, маркировка пересчитывается через
     * {@code MarkingEngine}, проверяется уникальность.
     *
     * @param cardId    ID карточки
     * @param updatedBy email пользователя
     * @return сохранённая карточка
     * @throws com.fanproduction.services.exception.FanCardCreationException
     *         если карточка не найдена или маркировка не может быть вычислена
     */
    FanCardEntity recalculateMarking(Long cardId, String updatedBy);
}

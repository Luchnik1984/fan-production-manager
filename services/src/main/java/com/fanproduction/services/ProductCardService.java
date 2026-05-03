package com.fanproduction.services;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.entity.product.MotorCardEntity;
import com.fanproduction.core.entity.product.MotorWheelCardEntity;
import com.fanproduction.core.entity.product.RadialWheelCardEntity;
import com.fanproduction.core.enums.CardTemplateType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления карточками продукции.
 */
public interface ProductCardService {

    /**
     * Создание новой карточки
     *
     * @param cardType тип карточки
     * @param fields   поля карточки
     * @param createdBy email создателя
     * @return созданная карточка
     */
    BaseProductCard createCard(CardTemplateType cardType, Map<String, Object> fields, String createdBy);

    /**
     * Обновление существующей карточки
     *
     * @param id      ID карточки
     * @param fields  поля для обновления
     * @return обновлённая карточка
     */
    BaseProductCard updateCard(Long id, Map<String, Object> fields);

    /**
     * Получение карточки по ID
     */
    Optional<BaseProductCard> getCardById(Long id);

    /**
     * Получение всех карточек определённого типа
     */
    List<BaseProductCard> getCardsByType(CardTemplateType cardType);

    /**
     * Получение всех карточек с пагинацией
     */
    Page<BaseProductCard> getAllCards(Pageable pageable);

    /**
     * Получение карточек по типу с пагинацией
     */
    Page<BaseProductCard> getCardsByType(CardTemplateType cardType, Pageable pageable);

    /**
     * Поиск карточек по имени
     */
    List<BaseProductCard> searchByName(String name);

    /**
     * Удаление карточки
     */
    void deleteCard(Long id);

    /**
     * Генерация уникального кода для карточки
     */
    String generateCode(BaseProductCard card);

    /**
     * Проверка уникальности кода
     */
    boolean isCodeUnique(String code);

    /**
     * Получение электродвигателя по ID (если карточка — двигатель)
     */
    Optional<MotorCardEntity> getMotorById(Long id);

    /**
     * Получение мотор-колеса по ID
     */
    Optional<MotorWheelCardEntity> getMotorWheelById(Long id);

    /**
     * Получение радиального колеса по ID
     */
    Optional<RadialWheelCardEntity> getRadialWheelById(Long id);

    List<BaseProductCard> searchByFields(String query);
}

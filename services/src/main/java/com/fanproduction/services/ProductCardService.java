package com.fanproduction.services;

import com.fanproduction.core.entity.product.*;
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

    List<BaseProductCard> searchByFields(String query);

    /**
     * Проверяет, используется ли сборочный узел в других карточках продукции
     * @param cardId ID карточки
     * @return true если используется
     */
    boolean isAssemblyUnitUsedInFanCards(Long cardId);

    /**
     * Получает информацию об использовании карточки
     * @param cardId сборочного узла
     * @return список строк с описанием использования
     */
    List<String> getAssemblyUnitUsageInfo(Long cardId);

    /**
     * Получить FAN-карточки с фильтром по имени типа вентилятора.
     * <p>
     * Например, {@code fanType = "Вентилятор канальный"}.
     */
    Page<BaseProductCard> getFanCardsByFanType(String fanType, Pageable pageable);

    /**
     * Создать карточку продукции со связями (компоненты и материалы)
     * в одной транзакции.
     * <p>
     * Используется endpoint'ом {@code POST /api/products/full}.
     *
     * @param cardType   тип карточки
     * @param name       наименование
     * @param fields     поля карточки
     * @param components компоненты
     * @param materials  материалы
     * @param createdBy  email создателя
     * @return сохранённая карточка
     */
    BaseProductCard createFullCard(CardTemplateType cardType,
                                   String name,
                                   Map<String, Object> fields,
                                   List<com.fanproduction.services.dto.ProductComponentItemData> components,
                                   List<com.fanproduction.services.dto.ProductMaterialItemData> materials,
                                   String createdBy);

    /**
     * Обновить карточку продукции со связями (компоненты и материалы)
     * в одной транзакции.
     * <p>
     * Используется endpoint'ом {@code PUT /api/products/{id}/full}.
     *
     * @param id                    ID карточки
     * @param name                  наименование
     * @param fields                поля карточки
     * @param components            компоненты для добавления/обновления
     * @param componentsToRemove    ID компонентов для удаления
     * @param materials             материалы для добавления/обновления
     * @param materialsToRemove     ID материалов для удаления
     * @return обновлённая карточка
     */
    BaseProductCard updateFullCard(Long id,
                                   String name,
                                   Map<String, Object> fields,
                                   List<com.fanproduction.services.dto.ProductComponentItemData> components,
                                   List<Long> componentsToRemove,
                                   List<com.fanproduction.services.dto.ProductMaterialItemData> materials,
                                   List<Long> materialsToRemove);
}

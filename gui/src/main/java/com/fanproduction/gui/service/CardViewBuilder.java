package com.fanproduction.gui.service;

import com.fanproduction.gui.dto.response.ProductCardDto;
import com.fanproduction.gui.dto.view.CardViewLayout;
import com.fanproduction.gui.dto.view.CardViewSection;
import com.fanproduction.gui.dto.view.CardViewSubsection;

import java.util.List;

/**
 * Построитель разметки окна просмотра карточки.
 * <p>
 * Разделён на несколько методов, потому что часть данных доступна
 * сразу (поля карточки, метаданные шаблона), а часть — требует
 * REST-запросов (связанные элементы, компоненты, материалы).
 * <p>
 * Основные методы — {@link #buildForFan(ProductCardDto)} и
 * {@link #buildForSystemCard(ProductCardDto)} — синхронные, строят
 * шапку и «Технические характеристики». Остальные методы вызываются
 * асинхронно из {@code CardViewController}.
 */
public interface CardViewBuilder {

    /**
     * Построить базовую разметку для FAN-карточки: заголовок окна,
     * вкладка «Основные поля» с шапкой и «Техническими характеристиками».
     * <p>
     * Вкладки «Компоненты» и «Материалы» создаются пустыми — их
     * заполнение идёт асинхронно через {@link #buildComponentsSection(Long)}
     * и {@link #buildMaterialsSection(Long)}.
     */
    CardViewLayout buildForFan(ProductCardDto card);

    /**
     * Построить базовую разметку для системной карточки
     * (MOTOR, MOTOR_WHEEL, RADIAL_WHEEL, AXIAL_WHEEL, CUP, ACCESSORY).
     * <p>
     * Использует {@link FieldMetadataService#getFieldsForType(String)} для
     * порядка и лейблов полей. Специфичные правила (fireproof/explosion)
     * применяются так же, как в старой {@code CardViewController}.
     * <p>
     * ID-поля ссылок на компоненты ({@code hubComponentId},
     * {@code bladeComponentId}, {@code wheelHubComponentId}) в ТХ
     * <b>не включаются</b> — они загружаются отдельно через
     * {@link #buildLinkedElements(ProductCardDto)} и показываются
     * как подсекции.
     */
    CardViewLayout buildForSystemCard(ProductCardDto card);

    /**
     * Загрузить связанные элементы и вернуть подсекции для вставки
     * в «Технические характеристики».
     * <p>
     * Для FAN: электродвигатель, осевое/радиальное колесо, мотор-колесо.
     * Для системных карточек: компоненты, на которые ссылаются поля
     * {@code hubComponentId}, {@code bladeComponentId},
     * {@code wheelHubComponentId} (RADIAL_WHEEL, AXIAL_WHEEL).
     * <p>
     * Асинхронно вызывается из {@code CardViewController}. Если ни один
     * элемент не задан или REST-запрос упал — возвращается пустой список.
     */
    List<CardViewSubsection> buildLinkedElements(ProductCardDto card);

    /**
     * Загрузить компоненты карточки.
     * <p>
     * Асинхронно вызывается из {@code CardViewController}.
     */
    CardViewSection buildComponentsSection(Long cardId);

    /**
     * Загрузить материалы карточки.
     * <p>
     * Асинхронно вызывается из {@code CardViewController}.
     */
    CardViewSection buildMaterialsSection(Long cardId);
}

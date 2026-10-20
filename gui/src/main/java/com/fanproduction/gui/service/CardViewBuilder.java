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
 * Основной метод {@link #buildForFan(ProductCardDto)} — синхронный,
 * строит шапку и «Технические характеристики». Остальные методы
 * вызываются асинхронно из {@code CardViewController}.
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
     * Загрузить связанные элементы (электродвигатель, колесо) и вернуть
     * подсекции для вставки в «Технические характеристики».
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

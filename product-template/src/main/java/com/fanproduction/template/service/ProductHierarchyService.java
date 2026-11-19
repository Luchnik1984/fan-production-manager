package com.fanproduction.template.service;


import com.fanproduction.template.dto.ProductSectionDto;
import com.fanproduction.template.dto.ProductSeriesDto;
import com.fanproduction.template.dto.ProductSpeciesDto;
import com.fanproduction.template.dto.ProductTypeDto;
import com.fanproduction.template.dto.ProductTypeShortDto;

import java.util.List;

/**
 * Сервис управления иерархией вентиляторов: типы → виды → серии.
 * <p>
 * Особенности:
 * <ul>
 *   <li>Уникальность обозначения типа — регистронезависимая.</li>
 *   <li>Уникальность имени вида в рамках типа — регистронезависимая.</li>
 *   <li>Уникальность имени серии в рамках вида — регистронезависимая.</li>
 *   <li>Один вид без имени (No_species) допускается на тип.</li>
 *   <li>Одна серия без имени (No_series) допускается на вид.</li>
 *   <li>Все операции создания/обновления/удаления пишут событие аудита.</li>
 * </ul>
 */
public interface ProductHierarchyService {

    // ========== Section ==========

    /**
     * Дерево разделов: Раздел → Подраздел → Тип → Вид → Серия.
     * <p>
     * В каждом узле {@code sections} содержит подразделы, а {@code types} —
     * типы продукции этого раздела вместе с видами и сериями.
     */
    List<ProductSectionDto> getSectionTree();

    /**
     * Плоский список всех разделов (без вложенных типов и подразделов).
     * Используется для выпадающих списков.
     */
    List<ProductSectionDto> getAllSections();

    /**
     * Найти раздел по ID.
     */
    ProductSectionDto getSectionById(Long id);

    /**
     * Создать раздел.
     *
     * @param parentId  родительский раздел ({@code null} — корневой)
     * @param name      наименование (обязательное, уникальное среди соседей)
     * @param sortOrder порядок отображения ({@code null} — в конец)
     * @param createdBy email создателя (для аудита)
     */
    ProductSectionDto createSection(Long parentId, String name, Integer sortOrder, String createdBy);

    /**
     * Обновить наименование и порядок раздела.
     */
    ProductSectionDto updateSection(Long id, String name, Integer sortOrder, String updatedBy);

    /**
     * Переместить раздел в другого родителя.
     * <p>
     * Запрещено перемещать раздел внутрь самого себя или своего потомка —
     * это создало бы цикл в дереве.
     */
    void moveSection(Long id, Long newParentId, String movedBy);

    /**
     * Удалить раздел.
     * <p>
     * Запрещено для системных разделов, а также если в разделе есть
     * подразделы или типы продукции.
     */
    void deleteSection(Long id, String deletedBy);

    // ========== Type ==========

    /**
     * Все типы, отсортированные по обозначению.
     * Поле {@code species} в DTO равно {@code null}.
     */
    List<ProductTypeDto> getAllTypes();

    /**
     * Найти тип по ID.
     */
    ProductTypeDto getTypeById(Long id);

    /**
     * Создать тип.
     *
     * @param name        наименование (может быть пустым)
     * @param designation обозначение (обязательное, уникальное)
     * @param createdBy   email создателя (для аудита)
     */
    ProductTypeDto createType(String name, String designation, String createdBy);

    /**
     * Создать тип в конкретном разделе продукции.
     *
     * @param name        наименование (может быть пустым)
     * @param designation обозначение (обязательное, уникальное)
     * @param sectionId   раздел продукции ({@code null} — не распределён)
     * @param createdBy   email создателя (для аудита)
     */
    ProductTypeDto createType(String name, String designation, Long sectionId, String createdBy);

    /**
     * Переместить тип продукции в другой раздел.
     * <p>
     * Используется drag-and-drop в дереве конструктора и кнопкой
     * «Переместить в раздел…». Смена раздела не влияет на виды, серии
     * и шаблоны типа.
     *
     * @param typeId    ID типа продукции
     * @param sectionId новый раздел ({@code null} — убрать из разделов)
     * @param updatedBy email администратора (для аудита)
     * @throws com.fanproduction.template.exception.TemplateValidationException
     *         если тип или раздел не найдены
     */
    ProductTypeDto moveTypeToSection(Long typeId, Long sectionId, String updatedBy);

    /**
     * Обновить тип.
     */
    ProductTypeDto updateType(Long id, String name, String designation, String updatedBy);

    /**
     * Удалить тип.
     * <p>
     * Запрещено, если у типа есть виды.
     */
    void deleteType(Long id, String deletedBy);

    // ========== Species ==========

    /**
     * Все виды данного типа, отсортированные по имени.
     */
    List<ProductSpeciesDto> getSpeciesByType(Long typeId);

    /**
     * Найти вид по ID.
     */
    ProductSpeciesDto getSpeciesById(Long id);

    /**
     * Создать вид.
     *
     * @param typeId    ID типа (обязательно)
     * @param name      наименование (может быть {@code null} — No_species)
     * @param createdBy email создателя
     */
    ProductSpeciesDto createSpecies(Long typeId, String name, String createdBy);

    /**
     * Обновить вид.
     */
    ProductSpeciesDto updateSpecies(Long id, String name, String updatedBy);

    /**
     * Удалить вид.
     * <p>
     * Запрещено, если у вида есть серии.
     */
    void deleteSpecies(Long id, String deletedBy);

    // ========== Series ==========

    /**
     * Все серии данного вида, отсортированные по имени.
     */
    List<ProductSeriesDto> getSeriesBySpecies(Long speciesId);

    /**
     * Найти серию по ID.
     */
    ProductSeriesDto getSeriesById(Long id);

    /**
     * Создать серию.
     *
     * @param speciesId ID вида (обязательно)
     * @param name      наименование (может быть {@code null} — No_series)
     * @param createdBy email создателя
     */
    ProductSeriesDto createSeries(Long speciesId, String name, String createdBy);

    /**
     * Обновить серию.
     */
    ProductSeriesDto updateSeries(Long id, String name, String updatedBy);

    /**
     * Удалить серию.
     */
    void deleteSeries(Long id, String deletedBy);

    // ========== Tree ==========

    /**
     * Полное дерево: Тип → Вид → Серия.
     * <p>
     * Все уровни заполнены (species и series не null).
     */
    List<ProductTypeDto> getTree();

    /**
     * Список типов продукции для ComboBox.
     * <p>
     * Возвращает {@code id, displayName, filterValue}.
     */
    List<ProductTypeShortDto> getShortList();
}
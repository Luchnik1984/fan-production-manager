package com.fanproduction.template.service;


import com.fanproduction.template.dto.FanSeriesDto;
import com.fanproduction.template.dto.FanSpeciesDto;
import com.fanproduction.template.dto.FanTypeDto;

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
public interface FanHierarchyService {

    // ========== Type ==========

    /**
     * Все типы, отсортированные по обозначению.
     * Поле {@code species} в DTO равно {@code null}.
     */
    List<FanTypeDto> getAllTypes();

    /**
     * Найти тип по ID.
     */
    FanTypeDto getTypeById(Long id);

    /**
     * Создать тип.
     *
     * @param name        наименование (может быть пустым)
     * @param designation обозначение (обязательное, уникальное)
     * @param createdBy   email создателя (для аудита)
     */
    FanTypeDto createType(String name, String designation, String createdBy);

    /**
     * Обновить тип.
     */
    FanTypeDto updateType(Long id, String name, String designation, String updatedBy);

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
    List<FanSpeciesDto> getSpeciesByType(Long typeId);

    /**
     * Найти вид по ID.
     */
    FanSpeciesDto getSpeciesById(Long id);

    /**
     * Создать вид.
     *
     * @param typeId    ID типа (обязательно)
     * @param name      наименование (может быть {@code null} — No_species)
     * @param createdBy email создателя
     */
    FanSpeciesDto createSpecies(Long typeId, String name, String createdBy);

    /**
     * Обновить вид.
     */
    FanSpeciesDto updateSpecies(Long id, String name, String updatedBy);

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
    List<FanSeriesDto> getSeriesBySpecies(Long speciesId);

    /**
     * Найти серию по ID.
     */
    FanSeriesDto getSeriesById(Long id);

    /**
     * Создать серию.
     *
     * @param speciesId ID вида (обязательно)
     * @param name      наименование (может быть {@code null} — No_series)
     * @param createdBy email создателя
     */
    FanSeriesDto createSeries(Long speciesId, String name, String createdBy);

    /**
     * Обновить серию.
     */
    FanSeriesDto updateSeries(Long id, String name, String updatedBy);

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
    List<FanTypeDto> getTree();
}
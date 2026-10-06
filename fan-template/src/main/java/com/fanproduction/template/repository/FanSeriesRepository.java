package com.fanproduction.template.repository;

import com.fanproduction.template.entity.FanSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий серий вентиляторов (третий уровень иерархии).
 */
@Repository
public interface FanSeriesRepository extends JpaRepository<FanSeries, Long> {

    /**
     * Все серии данного вида, отсортированные по имени.
     */
    List<FanSeries> findBySpeciesIdOrderByNameAsc(Long speciesId);

    /**
     * Найти серию по имени в рамках вида (точное совпадение, регистронезависимо).
     */
    Optional<FanSeries> findBySpeciesIdAndNameIgnoreCase(Long speciesId, String name);

    /**
     * Проверить существование серии с таким именем в рамках вида (регистронезависимо).
     */
    boolean existsBySpeciesIdAndNameIgnoreCase(Long speciesId, String name);

    /**
     * Проверить, есть ли в "виде" серия с name = NULL (No_series).
     * Нужно, чтобы запретить создание нескольких No_series в одном виде.
     */
    boolean existsBySpeciesIdAndNameIsNull(Long speciesId);

    /**
     * Количество серий в виде.
     */
    long countBySpeciesId(Long speciesId);

    /**
     * Удалить все серии данного вида.
     */
    void deleteBySpeciesId(Long speciesId);
}

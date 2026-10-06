package com.fanproduction.template.repository;

import com.fanproduction.template.entity.FanSpecies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий видов вентиляторов (второй уровень иерархии).
 */
@Repository
public interface FanSpeciesRepository extends JpaRepository<FanSpecies, Long> {

    /**
     * Все виды данного типа, отсортированные по имени.
     * Виды с name = NULL (No_species) будут в конце или в начале в зависимости от СУБД.
     */
    List<FanSpecies> findByTypeIdOrderByNameAsc(Long typeId);

    /**
     * Найти вид по имени в рамках типа (точное совпадение, регистронезависимо).
     */
    Optional<FanSpecies> findByTypeIdAndNameIgnoreCase(Long typeId, String name);

    /**
     * Проверить существование вида с таким именем в рамках типа (регистронезависимо).
     * Используется для проверки уникальности при создании/обновлении.
     */
    boolean existsByTypeIdAndNameIgnoreCase(Long typeId, String name);

    /**
     * Проверить, есть ли в типе вид с name = NULL (No_species).
     * Нужно, чтобы запретить создание нескольких No_species в одном типе.
     */
    boolean existsByTypeIdAndNameIsNull(Long typeId);

    /**
     * Количество видов в типе.
     * Используется для проверки перед удалением типа.
     */
    long countByTypeId(Long typeId);

    /**
     * Удалить все виды данного типа.
     * Не используется напрямую — каскадное удаление настроено в БД.
     */
    void deleteByTypeId(Long typeId);
}

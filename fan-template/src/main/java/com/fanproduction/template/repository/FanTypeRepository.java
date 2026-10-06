package com.fanproduction.template.repository;

import com.fanproduction.template.entity.FanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий типов вентиляторов (верхний уровень иерархии).
 */
@Repository
public interface FanTypeRepository extends JpaRepository<FanType, Long> {

    /**
     * Найти тип по обозначению (точное совпадение, регистронезависимо).
     * Обозначение уникально — вернёт один результат или пусто.
     */
    Optional<FanType> findByDesignationIgnoreCase(String designation);

    /**
     * Проверить существование типа с таким обозначением (регистронезависимо).
     * Используется для проверки уникальности при создании/обновлении.
     */
    boolean existsByDesignationIgnoreCase(String designation);

    /**
     * Найти тип по наименованию (точное совпадение).
     */
    Optional<FanType> findByName(String name);

    /**
     * Поиск по части наименования (регистронезависимо).
     * Для автодополнения при вводе.
     */
    List<FanType> findByNameContainingIgnoreCase(String namePart);

    /**
     * Все типы, отсортированные по обозначению.
     * Используется для отображения в UI.
     */
    List<FanType> findAllByOrderByDesignationAsc();
}

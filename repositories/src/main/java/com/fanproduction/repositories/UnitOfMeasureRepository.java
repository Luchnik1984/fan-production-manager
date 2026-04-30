package com.fanproduction.repositories;

import com.fanproduction.core.entity.UnitOfMeasureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с единицами измерения.
 */
@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasureEntity, Long> {

    /**
     * Найти единицу измерения по коду (например, "шт.", "м")
     */
    Optional<UnitOfMeasureEntity> findByCode(String code);

    /**
     * Получить единицу измерения по умолчанию
     */
    Optional<UnitOfMeasureEntity> findByIsDefaultTrue();

    /**
     * Найти все единицы, отсортированные по коду
     */
    List<UnitOfMeasureEntity> findAllByOrderByCodeAsc();
}

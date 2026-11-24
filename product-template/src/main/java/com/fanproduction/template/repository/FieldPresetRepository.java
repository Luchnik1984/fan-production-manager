package com.fanproduction.template.repository;

import com.fanproduction.template.entity.FieldPreset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий пресетов полей шаблона.
 */
@Repository
public interface FieldPresetRepository extends JpaRepository<FieldPreset, Long> {

    /**
     * Все пресеты в алфавитном порядке.
     */
    List<FieldPreset> findAllByOrderByNameAsc();

    /**
     * Поиск по названию (регистронезависимо).
     */
    Optional<FieldPreset> findByNameIgnoreCase(String name);

    /**
     * Проверка уникальности названия.
     */
    boolean existsByNameIgnoreCase(String name);
}

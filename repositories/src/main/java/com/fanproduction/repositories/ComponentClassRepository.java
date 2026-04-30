package com.fanproduction.repositories;

import com.fanproduction.core.entity.ComponentClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с классами компонентов.
 */
@Repository
public interface ComponentClassRepository extends JpaRepository<ComponentClassEntity, Long> {

    /**
     * Найти класс компонента по имени (уникально)
     */
    Optional<ComponentClassEntity> findByName(String name);

    /**
     * Проверить существование класса по имени
     */
    boolean existsByName(String name);

    /**
     * Найти все классы, отсортированные по имени
     */
    List<ComponentClassEntity> findAllByOrderByNameAsc();
}

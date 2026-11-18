package com.fanproduction.template.repository;

import com.fanproduction.template.entity.ProductTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий логических шаблонов карточек вентиляторов.
 */
@Repository
public interface ProductTemplateRepository extends JpaRepository<ProductTemplate, Long> {

    /**
     * Найти шаблон по серии.
     * Один шаблон на серию (UNIQUE INDEX в V22).
     */
    Optional<ProductTemplate> findBySeriesId(Long seriesId);

    /**
     * Проверить, есть ли шаблон для серии.
     */
    boolean existsBySeriesId(Long seriesId);

    /**
     * Все шаблоны, отсортированные по имени.
     */
    List<ProductTemplate> findAllByOrderByNameAsc();
}

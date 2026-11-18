package com.fanproduction.template.repository;

import com.fanproduction.template.entity.ProductSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий разделов продукции (верхний уровень иерархии).
 */
@Repository
public interface ProductSectionRepository extends JpaRepository<ProductSection, Long> {

    /**
     * Все разделы в порядке отображения.
     */
    List<ProductSection> findAllByOrderBySortOrderAscIdAsc();

    /**
     * Дочерние разделы указанного родителя.
     */
    List<ProductSection> findByParentIdOrderBySortOrderAscIdAsc(Long parentId);

    /**
     * Корневые разделы.
     */
    List<ProductSection> findByParentIdIsNullOrderBySortOrderAscIdAsc();

    /**
     * Количество дочерних разделов.
     */
    long countByParentId(Long parentId);

    /**
     * Проверка уникальности имени среди дочерних разделов.
     */
    boolean existsByParentIdAndNameIgnoreCase(Long parentId, String name);

    /**
     * Проверка уникальности имени среди корневых разделов.
     */
    boolean existsByParentIdIsNullAndNameIgnoreCase(String name);

    /**
     * Найти дочерний раздел по имени (регистронезависимо).
     */
    Optional<ProductSection> findByParentIdAndNameIgnoreCase(Long parentId, String name);

    /**
     * Найти корневой раздел по имени (регистронезависимо).
     */
    Optional<ProductSection> findByParentIdIsNullAndNameIgnoreCase(String name);
}

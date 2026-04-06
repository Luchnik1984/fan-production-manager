package com.fanproduction.repositories;

import com.fanproduction.core.entity.MaterialCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialCategoryRepository extends JpaRepository<MaterialCategoryEntity, Long> {

    /**
     * Получить все категории, отсортированные по пути
     */
    List<MaterialCategoryEntity> findAllByOrderByPathAsc();

    /**
     * Получить дочерние категории
     */
    List<MaterialCategoryEntity> findByParentIdOrderBySortOrderAsc(Long parentId);

    /**
     * Получить корневые категории
     */
    List<MaterialCategoryEntity> findByParentIdIsNullOrderBySortOrderAsc();

    /**
     * Проверить существование категории с таким именем среди дочерних
     */
    @Query("SELECT COUNT(m) > 0 FROM MaterialCategoryEntity m " +
            "WHERE m.parentId = :parentId AND LOWER(m.name) = LOWER(:name)")
    boolean existsByParentIdAndName(@Param("parentId") Long parentId,
                                    @Param("name") String name);

    /**
     * Получить максимальный порядок сортировки для родителя
     */
    @Query("SELECT MAX(m.sortOrder) FROM MaterialCategoryEntity m WHERE m.parentId = :parentId")
    Integer getMaxSortOrderByParentId(@Param("parentId") Long parentId);

    /**
     * Получить все категории, у которых путь начинается с указанного (для поиска по дереву)
     */
    List<MaterialCategoryEntity> findByPathStartingWith(String path);
}

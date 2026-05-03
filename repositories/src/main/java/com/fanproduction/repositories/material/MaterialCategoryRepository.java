package com.fanproduction.repositories.material;

import com.fanproduction.core.entity.material.MaterialCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialCategoryRepository extends JpaRepository<MaterialCategoryEntity, Long> {

    List<MaterialCategoryEntity> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<MaterialCategoryEntity> findByParentIdIsNullOrderBySortOrderAsc();

    List<MaterialCategoryEntity> findAllByOrderByPathAsc();

    Optional<MaterialCategoryEntity> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT COUNT(m) > 0 FROM MaterialCategoryEntity m " +
            "WHERE m.parentId = :parentId AND LOWER(m.name) = LOWER(:name)")
    boolean existsByParentIdAndName(@Param("parentId") Long parentId,
                                    @Param("name") String name);

    @Query("SELECT MAX(m.sortOrder) FROM MaterialCategoryEntity m WHERE m.parentId = :parentId")
    Integer getMaxSortOrderByParentId(@Param("parentId") Long parentId);

    List<MaterialCategoryEntity> findByPathStartingWith(String path);
}

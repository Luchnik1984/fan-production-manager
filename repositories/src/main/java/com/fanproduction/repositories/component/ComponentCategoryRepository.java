package com.fanproduction.repositories.component;

import com.fanproduction.core.entity.component.ComponentCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentCategoryRepository extends JpaRepository<ComponentCategoryEntity, Long> {

    List<ComponentCategoryEntity> findAllByOrderByPathAsc();

    List<ComponentCategoryEntity> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<ComponentCategoryEntity> findByParentIdIsNullOrderBySortOrderAsc();

    Optional<ComponentCategoryEntity> findByName(String name);

    boolean existsByName(String name);

    long countByParentId(Long parentId);
}
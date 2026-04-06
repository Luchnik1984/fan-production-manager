package com.fanproduction.repositories;

import com.fanproduction.core.entity.MaterialEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

    /**
     * Поиск материалов по категории
     */
    List<MaterialEntity> findByCategoryId(Long categoryId);

    /**
     * Поиск с пагинацией по категории
     */
    Page<MaterialEntity> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Поиск по названию (содержит)
     */
    List<MaterialEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Поиск по стандарту
     */
    List<MaterialEntity> findByStandardContainingIgnoreCase(String standard);

    /**
     * Поиск по типу материала
     */
    List<MaterialEntity> findByMaterialType(String materialType);

    /**
     * Поиск по нескольким категориям (для поиска по дереву)
     */
    @Query("SELECT m FROM MaterialEntity m WHERE m.categoryId IN :categoryIds")
    List<MaterialEntity> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds);
}

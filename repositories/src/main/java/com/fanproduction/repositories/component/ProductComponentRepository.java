package com.fanproduction.repositories.component;

import com.fanproduction.core.entity.component.ProductComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductComponentRepository extends JpaRepository<ProductComponentEntity, Long> {

    List<ProductComponentEntity> findByProductCardId(Long productCardId);

    Optional<ProductComponentEntity> findByProductCardIdAndComponentId(Long productCardId, Long componentId);

    @Modifying
    @Transactional
    void deleteByProductCardId(Long productCardId);

    @Modifying
    @Transactional
    void deleteByProductCardIdAndComponentId(Long productCardId, Long componentId);

    List<ProductComponentEntity> findByComponentId(Long componentId);

    @Query("SELECT pc FROM ProductComponentEntity pc WHERE pc.productCardId IN :productCardIds")
    List<ProductComponentEntity> findByProductCardIds(@Param("productCardIds") List<Long> productCardIds);
}

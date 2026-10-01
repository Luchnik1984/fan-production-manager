package com.fanproduction.repositories.material;

import com.fanproduction.core.entity.material.ProductMaterialRequirementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductMaterialRequirementRepository extends JpaRepository<ProductMaterialRequirementEntity, Long> {

    List<ProductMaterialRequirementEntity> findByProductCardId(Long productCardId);

    Optional<ProductMaterialRequirementEntity> findByProductCardIdAndMaterialId(Long productCardId, Long materialId);

    @Modifying
    @Transactional
    void deleteByProductCardId(Long productCardId);

    @Modifying
    @Transactional
    void deleteByProductCardIdAndMaterialId(Long productCardId, Long materialId);

    List<ProductMaterialRequirementEntity> findByMaterialId(Long materialId);
}

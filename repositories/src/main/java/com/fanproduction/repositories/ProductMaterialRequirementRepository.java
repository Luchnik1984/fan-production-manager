package com.fanproduction.repositories;

import com.fanproduction.core.entity.ProductMaterialRequirementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductMaterialRequirementRepository extends JpaRepository<ProductMaterialRequirementEntity, Long> {

    /**
     * Получить все материалы для карточки продукции
     */
    List<ProductMaterialRequirementEntity> findByProductCardId(Long productCardId);

    /**
     * Удалить все материалы для карточки продукции
     */
    @Modifying
    void deleteByProductCardId(Long productCardId);

    /**
     * Удалить конкретный материал из карточки
     */
    @Modifying
    void deleteByProductCardIdAndMaterialId(Long productCardId, Long materialId);

    /**
     * Получить сумму материалов для нескольких карточек (для расчёта потребности на заказ)
     */
    @Query("SELECT pm.materialId, SUM(pm.quantityPerUnit) as totalQuantity " +
            "FROM ProductMaterialRequirementEntity pm " +
            "WHERE pm.productCardId IN :productCardIds " +
            "GROUP BY pm.materialId")
    List<Object[]> sumMaterialsForProductCards(@Param("productCardIds") List<Long> productCardIds);
}

package com.fanproduction.repositories;

import com.fanproduction.core.entity.ProductComponentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для связи продукции с компонентами.
 */
@Repository
public interface ProductComponentRepository extends JpaRepository<ProductComponentEntity, Long> {

    /**
     * Найти все компоненты, привязанные к карточке продукции
     */
    List<ProductComponentEntity> findByProductCardId(Long productCardId);

    /**
     * Найти конкретную связь (продукт + компонент)
     */
    Optional<ProductComponentEntity> findByProductCardIdAndComponentId(Long productCardId, Long componentId);

    /**
     * Удалить все компоненты, привязанные к карточке продукции
     */
    @Modifying
    @Transactional
    void deleteByProductCardId(Long productCardId);

    /**
     * Удалить конкретную связь
     */
    @Modifying
    @Transactional
    void deleteByProductCardIdAndComponentId(Long productCardId, Long componentId);

    /**
     * Получить все компоненты для нескольких карточек (для массового расчёта)
     */
    @Query("SELECT pc FROM ProductComponentEntity pc WHERE pc.productCardId IN :productCardIds")
    List<ProductComponentEntity> findByProductCardIds(@Param("productCardIds") List<Long> productCardIds);

    List<ProductComponentEntity> findByComponentId(Long componentId);
}

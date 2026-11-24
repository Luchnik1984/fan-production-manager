package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.DynamicCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Repository
public interface DynamicCardRepository extends JpaRepository<DynamicCardEntity, Long> {

    /**
     * Поиск вентиляторов по типоразмеру
     */
    List<DynamicCardEntity> findBySize(Double size);

    /**
     * Поиск по классу (общеобменный, дымоудаление)
     */
    List<DynamicCardEntity> findByFanClass(String fanClass);

    /**
     * Поиск по типу вентилятора (осевой, радиальный, канальный).
     * <p>
     * Легаси-поле {@code fan_type} в {@code dynamic_card}: строковое обозначение
     * типа, унаследованное от «вентиляторной» модели. Новый фильтр по типу
     * продукции — {@link #findByProductTypeId(Long, Pageable)}.
     */
    List<DynamicCardEntity> findByFanType(String fanType);


    /**
     * Поиск по диапазону типоразмеров
     */
    @Query("SELECT f FROM DynamicCardEntity f WHERE f.size BETWEEN :minSize AND :maxSize")
    List<DynamicCardEntity> findBySizeRange(@Param("minSize") Double minSize,
                                        @Param("maxSize") Double maxSize);

    /**
     * Поиск по полной маркировке (регистронезависимо).
     */
    Optional<DynamicCardEntity> findByFullMarkingIgnoreCase(String fullMarking);

    /**
     * Проверить существование карточки с такой маркировкой.
     */
    boolean existsByFullMarkingIgnoreCase(String fullMarking);

    /**
     * Поиск динамических карточек по легаси-полю {@code fan_type}.
     * <p>
     * Пример значения: {@code "Вентилятор канальный"}.
     */
    Page<DynamicCardEntity> findByFanType(String fanType, Pageable pageable);

    /**
     * Поиск динамических карточек по типу продукции (product_type).
     */
    Page<DynamicCardEntity> findByProductTypeId(Long productTypeId, Pageable pageable);

    /** Карточки нескольких типов — используется для выбора по роли (US10.7.2). */
    List<DynamicCardEntity> findByProductTypeIdIn(java.util.Collection<Long> productTypeIds);
}

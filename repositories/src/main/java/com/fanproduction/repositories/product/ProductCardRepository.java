package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.BaseProductCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCardRepository extends JpaRepository<BaseProductCard, Long> {

    /**
     * Поиск по типу карточки
     */
    List<BaseProductCard> findByCardType(String cardType);

    /**
     * Поиск с пагинацией по типу карточки
     */
    Page<BaseProductCard> findByCardType(String cardType, Pageable pageable);

    /**
     * Поиск по имени (содержит)
     */
    List<BaseProductCard> findByNameContainingIgnoreCase(String name);

    /**
     * Поиск по коду
     */
    Optional<BaseProductCard> findByCode(String code);

    /**
     * Проверка существования кода
     */
    boolean existsByCode(String code);

    /**
     * Поиск по создателю
     */
    List<BaseProductCard> findByCreatedBy(String createdBy);

    /**
     * Подсчёт карточек по типу
     */
    @Query("SELECT COUNT(p) FROM BaseProductCard p WHERE p.cardType = :cardType")
    long countByCardType(@Param("cardType") String cardType);

    /**
     * Поиск временных карточек
     */
    List<BaseProductCard> findByIsTemporaryTrue();


    /**
     * Найти все вентиляторы, использующие данный сборочный узел
     */
    @Query(value = """
        SELECT b.* FROM base_product_card b
        INNER JOIN fan_card f ON b.id = f.id
        WHERE b.card_type IN (:fanCardTypes)
        AND f.:fieldName = :unitId
    """, nativeQuery = true)
    List<BaseProductCard> findFanCardsUsingUnit(
            @Param("unitId") Long unitId,
            @Param("fieldName") String fieldName,
            @Param("fanCardTypes") List<String> fanCardTypes
    );

    /**
     * Подсчитать количество вентиляторов, использующих данный сборочный узел
     */
    @Query(value = """
        SELECT COUNT(*) FROM base_product_card b
        INNER JOIN fan_card f ON b.id = f.id
        WHERE b.card_type IN (:fanCardTypes)
        AND f.:fieldName = :unitId
    """, nativeQuery = true)
    long countFanCardsUsingUnit(
            @Param("unitId") Long unitId,
            @Param("fieldName") String fieldName,
            @Param("fanCardTypes") List<String> fanCardTypes
    );
}




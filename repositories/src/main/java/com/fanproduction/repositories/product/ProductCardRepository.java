package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.BaseProductCard;
import com.fanproduction.core.entity.product.FanCardEntity;
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


    // ==========================================================
    // МЕТОДЫ ДЛЯ ПРОВЕРКИ ИСПОЛЬЗОВАНИЯ СБОРОЧНЫХ УЗЛОВ
    // ==========================================================

    /**
     * Подсчитать количество вентиляторов, использующих электродвигатель
     */
    @Query("SELECT COUNT(f) FROM FanCardEntity f WHERE f.motorId = :motorId")
    long countFanCardsUsingMotor(@Param("motorId") Long motorId);

    /**
     * Подсчитать количество вентиляторов, использующих мотор-колесо
     */
    @Query("SELECT COUNT(f) FROM FanCardEntity f WHERE f.motorWheelId = :motorWheelId")
    long countFanCardsUsingMotorWheel(@Param("motorWheelId") Long motorWheelId);

    /**
     * Подсчитать количество вентиляторов, использующих радиальное колесо
     */
    @Query("SELECT COUNT(f) FROM FanCardEntity f WHERE f.radialWheelId = :radialWheelId")
    long countFanCardsUsingRadialWheel(@Param("radialWheelId") Long radialWheelId);

    /**
     * Подсчитать количество вентиляторов, использующих осевое колесо
     */
    @Query("SELECT COUNT(f) FROM FanCardEntity f WHERE f.axialWheelId = :axialWheelId")
    long countFanCardsUsingAxialWheel(@Param("axialWheelId") Long axialWheelId);

    /**
     * Найти все вентиляторы, использующие данный сборочный узел
     * (для получения списка использования)
     */
    @Query("SELECT f FROM FanCardEntity f WHERE f.motorId = :unitId")
    List<FanCardEntity> findFanCardsByMotorId(@Param("unitId") Long unitId);

    @Query("SELECT f FROM FanCardEntity f WHERE f.motorWheelId = :unitId")
    List<FanCardEntity> findFanCardsByMotorWheelId(@Param("unitId") Long unitId);

    @Query("SELECT f FROM FanCardEntity f WHERE f.radialWheelId = :unitId")
    List<FanCardEntity> findFanCardsByRadialWheelId(@Param("unitId") Long unitId);

    @Query("SELECT f FROM FanCardEntity f WHERE f.axialWheelId = :unitId")
    List<FanCardEntity> findFanCardsByAxialWheelId(@Param("unitId") Long unitId);
}





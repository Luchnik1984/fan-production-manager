package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.RadialWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RadialWheelCardRepository extends JpaRepository<RadialWheelCardEntity, Long> {

    /**
     * Поиск по размеру
     */
    List<RadialWheelCardEntity> findBySize(Double size);

    /**
     * Поиск по маркировке
     */
    List<RadialWheelCardEntity> findByMarkingContainingIgnoreCase(String marking);

    /**
     * Поиск по типу лопаток
     */
    List<RadialWheelCardEntity> findByBladeType(String bladeType);

    /**
     * Поиск по полям радиального колеса
     */
    @Query("SELECT r FROM RadialWheelCardEntity r WHERE " +
            "LOWER(r.manufacturer) LIKE LOWER(:pattern) OR " +
            "LOWER(r.marking) LIKE LOWER(:pattern) OR " +
            "LOWER(r.bladeType) LIKE LOWER(:pattern)")
    List<RadialWheelCardEntity> searchByFields(@Param("pattern") String pattern);
}
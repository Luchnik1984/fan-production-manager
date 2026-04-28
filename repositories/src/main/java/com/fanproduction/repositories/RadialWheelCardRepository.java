package com.fanproduction.repositories;

import com.fanproduction.core.entity.RadialWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RadialWheelCardRepository extends JpaRepository<RadialWheelCardEntity, Long> {

    List<RadialWheelCardEntity> findBySize(Double size);
    List<RadialWheelCardEntity> findByMarkingContainingIgnoreCase(String marking);
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
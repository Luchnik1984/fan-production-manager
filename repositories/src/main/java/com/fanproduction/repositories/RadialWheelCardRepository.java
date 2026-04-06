package com.fanproduction.repositories;

import com.fanproduction.core.entity.RadialWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
}

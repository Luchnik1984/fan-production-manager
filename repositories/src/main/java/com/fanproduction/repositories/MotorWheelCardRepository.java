package com.fanproduction.repositories;

import com.fanproduction.core.entity.MotorWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotorWheelCardRepository extends JpaRepository<MotorWheelCardEntity, Long> {

    /**
     * Поиск по размеру
     */
    List<MotorWheelCardEntity> findBySize(Integer size);

    /**
     * Поиск по типу лопаток
     */
    List<MotorWheelCardEntity> findByBladeType(String bladeType);

    /**
     * Поиск по количеству полюсов
     */
    List<MotorWheelCardEntity> findByPoles(Integer poles);
}


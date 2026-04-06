package com.fanproduction.repositories;

import com.fanproduction.core.entity.MotorCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotorCardRepository extends JpaRepository<MotorCardEntity, Long> {

    /**
     * Поиск по типу электродвигателя
     */
    List<MotorCardEntity> findByMotorTypeContainingIgnoreCase(String motorType);

    /**
     * Поиск по мощности
     */
    List<MotorCardEntity> findByPowerKwBetween(Double minPower, Double maxPower);

    /**
     * Поиск по количеству полюсов
     */
    List<MotorCardEntity> findByPoles(Integer poles);

    /**
     * Поиск по напряжению
     */
    List<MotorCardEntity> findByVoltage(Integer voltage);

    /**
     * Поиск по типу монтажа
     */
    List<MotorCardEntity> findByMountingTypeContainingIgnoreCase(String mountingType);
}

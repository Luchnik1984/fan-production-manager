package com.fanproduction.repositories;

import com.fanproduction.core.entity.MotorCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Поиск по полям электродвигателя (серия, тип, полная маркировка)
     */
    @Query("SELECT m FROM MotorCardEntity m WHERE " +
            "LOWER(m.series) LIKE LOWER(:pattern) OR " +
            "LOWER(m.motorType) LIKE LOWER(:pattern) OR " +
            "LOWER(m.fullMarking) LIKE LOWER(:pattern)")
    List<MotorCardEntity> searchByFields(@Param("pattern") String pattern);
}

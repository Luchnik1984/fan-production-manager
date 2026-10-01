package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.AerodynamicDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AerodynamicDataRepository extends JpaRepository<AerodynamicDataEntity, Long> {

    /**
     * Получить все характеристики для вентилятора
     */
    List<AerodynamicDataEntity> findByFanCardIdOrderByAirflowAsc(Long fanCardId);

    /**
     * Получить характеристику для конкретной скорости вращения
     */
    List<AerodynamicDataEntity> findByFanCardIdAndSpeedRpm(Long fanCardId, Integer speedRpm);

    /**
     * Удалить все характеристики вентилятора
     */
    void deleteByFanCardId(Long fanCardId);

    /**
     * Получить все доступные скорости вращения для вентилятора
     */
    @Query("SELECT DISTINCT a.speedRpm FROM AerodynamicDataEntity a WHERE a.fanCardId = :fanCardId")
    List<Integer> findDistinctSpeedsByFanCardId(@Param("fanCardId") Long fanCardId);
}
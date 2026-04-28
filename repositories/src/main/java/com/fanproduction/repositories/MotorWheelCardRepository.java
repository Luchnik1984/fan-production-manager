package com.fanproduction.repositories;

import com.fanproduction.core.entity.MotorWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MotorWheelCardRepository extends JpaRepository<MotorWheelCardEntity, Long> {

    List<MotorWheelCardEntity> findBySize(Integer size);
    List<MotorWheelCardEntity> findByBladeType(String bladeType);
    List<MotorWheelCardEntity> findByPoles(Integer poles);

    /**
     * Поиск по полям мотор-колеса
     */
    @Query("SELECT m FROM MotorWheelCardEntity m WHERE " +
            "LOWER(m.manufacturer) LIKE LOWER(:pattern) OR " +
            "LOWER(m.bladeType) LIKE LOWER(:pattern)")
    List<MotorWheelCardEntity> searchByFields(@Param("pattern") String pattern);
}


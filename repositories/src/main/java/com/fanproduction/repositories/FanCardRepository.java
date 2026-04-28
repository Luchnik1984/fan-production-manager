package com.fanproduction.repositories;

import com.fanproduction.core.entity.FanCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FanCardRepository extends JpaRepository<FanCardEntity, Long> {

    /**
     * Поиск вентиляторов по типоразмеру
     */
    List<FanCardEntity> findBySize(Double size);

    /**
     * Поиск по классу (общеобменный, дымоудаление)
     */
    List<FanCardEntity> findByFanClass(String fanClass);

    /**
     * Поиск по типу вентилятора (осевой, радиальный, канальный)
     */
    List<FanCardEntity> findByFanType(String fanType);

    /**
     * Поиск по установленному электродвигателю
     */
    List<FanCardEntity> findByMotorId(Long motorId);

    /**
     * Поиск по диапазону типоразмеров
     */
    @Query("SELECT f FROM FanCardEntity f WHERE f.size BETWEEN :minSize AND :maxSize")
    List<FanCardEntity> findBySizeRange(@Param("minSize") Double minSize,
                                        @Param("maxSize") Double maxSize);

}

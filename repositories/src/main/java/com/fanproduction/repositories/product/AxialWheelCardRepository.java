package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.AxialWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AxialWheelCardRepository extends JpaRepository<AxialWheelCardEntity, Long> {

    @Query("SELECT a FROM AxialWheelCardEntity a WHERE " +
            "LOWER(a.manufacturer) LIKE LOWER(CONCAT('%', :pattern, '%')) OR " +
            "LOWER(a.marking) LIKE LOWER(CONCAT('%', :pattern, '%')) OR " +
            "LOWER(a.bladeType) LIKE LOWER(CONCAT('%', :pattern, '%'))")
    List<AxialWheelCardEntity> searchByFields(@Param("pattern") String pattern);
}

package com.fanproduction.repositories.product;

import com.fanproduction.core.entity.product.AxialWheelCardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AxialWheelCardRepository extends JpaRepository<AxialWheelCardEntity, Long> {
}

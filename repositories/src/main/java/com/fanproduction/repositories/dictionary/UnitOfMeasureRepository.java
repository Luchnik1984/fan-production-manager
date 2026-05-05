package com.fanproduction.repositories.dictionary;

import com.fanproduction.core.entity.dictionary.UnitOfMeasureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnitOfMeasureRepository extends JpaRepository<UnitOfMeasureEntity, Long> {
    Optional<UnitOfMeasureEntity> findByCode(String code);
    Optional<UnitOfMeasureEntity> findByIsDefaultTrue();
    List<UnitOfMeasureEntity> findAllByOrderByCodeAsc();
    List<UnitOfMeasureEntity> findByCategoryOrderByCodeAsc(String category);
}

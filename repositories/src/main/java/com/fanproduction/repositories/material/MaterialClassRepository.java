package com.fanproduction.repositories.material;

import com.fanproduction.core.entity.material.MaterialClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialClassRepository extends JpaRepository<MaterialClassEntity, Long> {

    Optional<MaterialClassEntity> findByName(String name);

    boolean existsByName(String name);

    long countByCategoryId(Long categoryId);

    List<MaterialClassEntity> findByCategoryId(Long categoryId);

    List<MaterialClassEntity> findAllByOrderByNameAsc();

}
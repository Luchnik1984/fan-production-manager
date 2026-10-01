package com.fanproduction.repositories.component;

import com.fanproduction.core.entity.component.ComponentClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentClassRepository extends JpaRepository<ComponentClassEntity, Long> {

    Optional<ComponentClassEntity> findByName(String name);

    boolean existsByName(String name);

    List<ComponentClassEntity> findByCategoryId(Long categoryId);

    List<ComponentClassEntity> findAllByOrderByNameAsc();

    long countByCategoryId(Long categoryId);
}

package com.fanproduction.template.repository;

import com.fanproduction.template.entity.CardRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий ролей продукции (US10.7.2).
 */
@Repository
public interface CardRoleRepository extends JpaRepository<CardRole, Long> {

    List<CardRole> findAllByOrderBySortOrderAscIdAsc();

    Optional<CardRole> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);
}

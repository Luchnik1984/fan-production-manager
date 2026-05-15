package com.fanproduction.repositories.component;

import com.fanproduction.core.entity.component.ComponentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComponentRepository extends JpaRepository<ComponentEntity, Long> {

    /**
     * Найти все компоненты определённого класса
     */
    List<ComponentEntity> findByClassId(Long classId);

    /**
     * Найти компоненты класса с пагинацией
     */
    Page<ComponentEntity> findByClassId(Long classId, Pageable pageable);

    /**
     * Найти компонент по имени в рамках класса
     */
    Optional<ComponentEntity> findByClassIdAndName(Long classId, String name);

    /**
     * Поиск компонентов по имени (содержит)
     */
    List<ComponentEntity> findByNameContainingIgnoreCase(String name);

    /**
     * Поиск компонентов по артикулу
     */
    List<ComponentEntity> findByVendorCodeContainingIgnoreCase(String vendorCode);

    /**
     * Поиск компонентов по классу и имени (для автодополнения)
     */
    @Query("SELECT c FROM ComponentEntity c WHERE c.classId = :classId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<ComponentEntity> searchByClassAndName(@Param("classId") Long classId, @Param("search") String search);

    /**
     * Подсчёт компонентов в классе
     */
    long countByClassId(Long classId);

    /**
     * Поиск по артикулу
     */
    Optional<ComponentEntity> findByVendorCode(String vendorCode);


}
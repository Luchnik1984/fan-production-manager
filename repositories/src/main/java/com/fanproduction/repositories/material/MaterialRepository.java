package com.fanproduction.repositories.material;

import com.fanproduction.core.entity.material.MaterialEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MaterialRepository extends JpaRepository<MaterialEntity, Long> {

    List<MaterialEntity> findByClassId(Long classId);

    Page<MaterialEntity> findByClassId(Long classId, Pageable pageable);

    Optional<MaterialEntity> findByClassIdAndName(Long classId, String name);

    List<MaterialEntity> findByNameContainingIgnoreCase(String name);

    List<MaterialEntity> findByStandardContainingIgnoreCase(String standard);

    List<MaterialEntity> findByMaterialType(String materialType);

    List<MaterialEntity> findByVendorCodeContainingIgnoreCase(String vendorCode);

    @Query("SELECT m FROM MaterialEntity m WHERE m.classId = :classId AND LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<MaterialEntity> searchByClassAndName(@Param("classId") Long classId, @Param("search") String search);

    long countByClassId(Long classId);
}

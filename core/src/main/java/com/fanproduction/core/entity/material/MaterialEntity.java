package com.fanproduction.core.entity.material;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Конкретный материал (уровень 4 иерархии).
 * Пример: "Болт М6х12 ГОСТ 7798-70"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "material")
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "designation", nullable = false, length = 100)
    private String designation;

    @Column(name = "standard", length = 100)
    private String standard;

    @Column(name = "specification", length = 200)
    private String specification;

    @Column(name = "material_type", length = 50)
    private String materialType;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "density")
    private Double density;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "min_order")
    private Double minOrder;

    @Column(name = "description", length = 500)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "technical_specs", columnDefinition = "jsonb")
    private Map<String, Object> technicalSpecs;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

package com.fanproduction.core.entity.component;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Конкретный компонент (уровень 4 иерархии).
 * Пример: "Ступица SM1210"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "component")
public class ComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "class_id", nullable = false)
    private Long classId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "designation", length = 100)
    private String designation;

    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "description", length = 500)
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "technical_specs", columnDefinition = "jsonb")
    private Map<String, Object> technicalSpecs;

    @Column(name = "weight_kg")
    private Double weightKg;

    @Column(name = "material", length = 100)
    private String material;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
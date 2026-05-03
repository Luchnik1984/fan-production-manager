package com.fanproduction.core.entity.material;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Связь продукции с материалами.
 * Определяет, сколько какого материала требуется для изделия.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_material_requirement")
public class ProductMaterialRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_card_id", nullable = false)
    private Long productCardId;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "quantity_per_unit", nullable = false)
    private Double quantityPerUnit = 1.0;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
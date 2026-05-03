package com.fanproduction.core.entity.material;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Категория материалов (уровни 1-2 иерархии).
 * Пример: "Метизы" → "Болты"
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "material_category")
public class MaterialCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "level", nullable = false)
    private Integer level = 1;

    @Column(name = "path", length = 500)
    private String path;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Transient
    private List<MaterialCategoryEntity> children = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

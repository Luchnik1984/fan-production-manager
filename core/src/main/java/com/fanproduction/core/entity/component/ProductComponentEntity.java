package com.fanproduction.core.entity.component;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Связь продукции с компонентами.
 * Определяет, сколько какого компонента требуется для изделия.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_component")
public class ProductComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_card_id", nullable = false)
    private Long productCardId;

    @Column(name = "component_id", nullable = false)
    private Long componentId;

    @Column(name = "quantity", nullable = false)
    private Double quantity = 1.0;

    @Column(name = "position", length = 50)
    private String position;

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

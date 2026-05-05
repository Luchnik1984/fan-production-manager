package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Абстрактная базовая сущность для всех карточек продукции.
 * Использует стратегию наследования JOINED.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "base_product_card")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BaseProductCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование продукции
     */
    @Column(nullable = false, length = 200)
    private String name;

    /**
     * Уникальный код для заказа (формируется автоматически)
     */
    @Column(unique = true, length = 50)
    private String code;

    /**
     * Тип карточки (MOTOR, AXIAL_FAN, RADIAL_FAN, DUCT_FAN, CUP, ACCESSORY)
     */
    @Column(name = "card_type", nullable = false, length = 50)
    private String cardType;

    /**
     * Дата создания
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата последнего обновления
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Кто создал (email пользователя)
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

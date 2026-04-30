package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Конкретный компонент (экземпляр).
 * Пример: "PG21" (класс "Кабельные вводы"), "BF2012" (класс "Ступицы").
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

    /**
     * Ссылка на класс компонента
     */
    @Column(name = "class_id", nullable = false)
    private Long classId;

    /**
     * Наименование компонента (в рамках класса)
     * Пример: "PG21", "PG16", "M6x50"
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Артикул производителя
     */
    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    /**
     * Ссылка на единицу измерения
     */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    /**
     * Количество на единицу изделия (обычно 1)
     */
    @Column(name = "quantity_per_unit")
    private Double quantityPerUnit = 1.0;

    /**
     * Описание
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Дата создания
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Кто создал (email пользователя)
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

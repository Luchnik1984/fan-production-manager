package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Единица измерения.
 * Примеры: штука (шт), метр (м), килограмм (кг).
 * Пользователь (администратор) может добавлять новые единицы.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "unit_of_measure")
public class UnitOfMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Код единицы измерения (уникальный)
     * Пример: "шт", "м", "кг", "г", "л"
     */
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private String code;

    /**
     * Полное название
     * Пример: "штука", "метр", "килограмм"
     */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /**
     * Символ (краткое обозначение)
     * Пример: "шт", "м", "кг"
     */
    @Column(name = "symbol", length = 10)
    private String symbol;

    /**
     * Является ли единицей по умолчанию
     */
    @Column(name = "is_default")
    private Boolean isDefault = false;

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

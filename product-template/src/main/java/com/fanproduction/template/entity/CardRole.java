package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Роль продукции (US10.7.2).
 * <p>
 * Отвечает на вопрос «чем это изделие является для другого изделия»:
 * осевое колесо, радиальное колесо, клапан, теплообменник…
 * <p>
 * Справочник редактируемый: системные роли (MOTOR, MOTOR_WHEEL,
 * AXIAL_WHEEL, RADIAL_WHEEL) заводятся миграцией, остальные добавляет
 * администратор. Код роли совпадает со значением {@code referenceType}
 * в поле шаблона, поэтому селектор ищет карточки по роли, а не по типу.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "card_role")
public class CardRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Код роли: AXIAL_WHEEL, RADIAL_WHEEL, VALVE… Уникален (регистронезависимо).
     */
    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isSystem == null) {
            isSystem = false;
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}

package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Тип вентилятора (верхний уровень иерархии).
 * <p>
 * Пример: «Вентилятор канальный (VRK-PatAIR)».
 * <p>
 * Тип участвует в маркировке всех вентиляторов этого типа:
 * они начинаются с designation (например, «VRK-PatAIR-...»).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fan_type")
public class FanType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Наименование типа, например «Вентилятор канальный».
     * Может быть пустым — тогда отображаемое имя = designation.
     */
    @Column(name = "name", length = 200)
    private String name;

    /**
     * Обозначение типа, например «VRK-PatAIR».
     * Уникальное (регистронезависимо). Обязательное.
     */
    @Column(name = "designation", nullable = false, length = 50)
    private String designation;

    /**
     * Системный тип — нельзя удалить.
     */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

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
    }
}

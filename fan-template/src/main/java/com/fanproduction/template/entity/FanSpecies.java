package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Вид вентилятора (второй уровень иерархии).
 * <p>
 * Пример: «Прямоугольный», «Круглый», «Спиральный».
 * <p>
 * Вид не участвует в маркировке — используется только для систематизации.
 * Если name = NULL, в дереве отображается как «No_species».
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fan_species")
public class FanSpecies {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на тип.
     */
    @Column(name = "type_id", nullable = false)
    private Long typeId;

    /**
     * Наименование вида. NULL = «No_species».
     * Уникально в рамках типа (регистронезависимо).
     */
    @Column(name = "name", length = 100)
    private String name;

    /**
     * Системный вид — нельзя удалить.
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

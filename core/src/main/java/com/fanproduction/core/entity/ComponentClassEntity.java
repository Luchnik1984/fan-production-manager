package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * Класс компонента (тип комплектующего).
 * Примеры: "Кабельные вводы", "Ступицы", "Муфты", "Втулки".
 * Пользователь может создавать новые классы динамически.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "component_class")
public class ComponentClassEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Название класса компонента (уникальное)
     * Пример: "Кабельные вводы"
     */
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Описание класса (необязательное)
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
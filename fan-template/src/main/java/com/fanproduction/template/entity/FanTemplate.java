package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Логический шаблон карточки вентилятора.
 * <p>
 * Привязан к серии. Один шаблон на серию.
 * Имеет много версий ({@link FanTemplateVersion}).
 * Хранит ссылку на текущую PUBLISHED-версию.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fan_template")
public class FanTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на серию. Один шаблон на серию.
     */
    @Column(name = "series_id", nullable = false)
    private Long seriesId;

    /**
     * Наименование шаблона, например «Шаблон P-серии».
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Описание (опционально).
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Ссылка на текущую PUBLISHED-версию.
     * Может быть NULL, пока первая версия не опубликована.
     */
    @Column(name = "current_version_id")
    private Long currentVersionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

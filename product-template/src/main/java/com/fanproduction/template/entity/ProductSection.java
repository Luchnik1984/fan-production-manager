package com.fanproduction.template.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Раздел продукции — верхний уровень иерархии.
 * <p>
 * Дерево разделов самодостаточно ({@code parentId}), поэтому администратор может
 * создавать подразделы: «Комплектующие → Колёса», «Вентиляторы → Канальные».
 * <p>
 * Пример корневых разделов: «Комплектующие», «Вентиляторы», «Стаканы», «Клапаны».
 * Системные разделы ({@code isSystem = true}) нельзя удалить.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_section")
public class ProductSection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Родительский раздел. {@code null} — корневой раздел.
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * Наименование раздела, например «Вентиляторы».
     * Уникально в рамках родителя (регистронезависимо).
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Порядок отображения среди соседей (по возрастанию).
     */
    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /**
     * Системный раздел — нельзя удалить.
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
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }
}

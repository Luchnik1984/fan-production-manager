package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Категория материалов (иерархическая структура).
 * Пример:
 * - Метизы (уровень 1)
 *   - Болты (уровень 2)
 *     - ГОСТ 7798-70 (уровень 3)
 * - Металл (уровень 1)
 *   - Листовой (уровень 2)
 *     - Оцинкованный (уровень 3)
 *       - 08пс ГОСТ 14918-80 (уровень 4)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "material_category")
public class MaterialCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на родительскую категорию (NULL для корневых)
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * Название категории
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Уровень вложенности (1, 2, 3...)
     */
    @Column(name = "level")
    private Integer level;

    /**
     * Путь для быстрого поиска (например: "/Метизы/Болты/")
     */
    @Column(name = "path", length = 500)
    private String path;

    /**
     * Порядок сортировки внутри уровня
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /**
     * Дочерние категории (не хранится в БД, используется для UI)
     */
    @Transient
    private List<MaterialCategoryEntity> children = new ArrayList<>();
}

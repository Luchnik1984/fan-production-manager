package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Конкретный материал.
 * Пример:
 * - Наименование: Болт ГОСТ 7798-70
 * - Стандарт: ГОСТ 7798-70
 * - Характеристика: M6-6gx50
 * - Тип: металл
 * - Единица измерения: шт
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "material")
public class MaterialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на категорию материала
     */
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    /**
     * Наименование материала
     */
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * Стандарт (ГОСТ, ТУ)
     */
    @Column(name = "standard", length = 100)
    private String standard;

    /**
     * Характеристика (размер, марка, покрытие)
     */
    @Column(name = "specification", length = 200)
    private String specification;

    /**
     * Тип материала (металл, пластик, резина, краска, крепёж)
     */
    @Column(name = "material_type", length = 50)
    private String materialType;

    /**
     * Единица измерения (шт, кг, м, пог.м, м², л)
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Плотность (кг/м³) — для расчёта массы
     */
    @Column(name = "density")
    private Double density;

    /**
     * Артикул поставщика
     */
    @Column(name = "vendor_code", length = 100)
    private String vendorCode;

    /**
     * Минимальный заказ
     */
    @Column(name = "min_order")
    private Double minOrder;

    /**
     * Краткое описание
     */
    @Column(name = "description", length = 500)
    private String description;
}

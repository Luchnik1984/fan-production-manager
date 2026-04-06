package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Связь между карточкой продукции и материалом.
 * Определяет, сколько какого материала требуется для изготовления единицы продукции.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_material_requirement")
public class ProductMaterialRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на карточку продукции (BaseProductCard)
     */
    @Column(name = "product_card_id", nullable = false)
    private Long productCardId;

    /**
     * Ссылка на материал
     */
    @Column(name = "material_id", nullable = false)
    private Long materialId;

    /**
     * Количество на единицу изделия
     */
    @Column(name = "quantity_per_unit")
    private Double quantityPerUnit;

    /**
     * Единица измерения количества (если отличается от единицы материала)
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Примечание (например: "с запасом 5%")
     */
    @Column(name = "note", length = 255)
    private String note;
}
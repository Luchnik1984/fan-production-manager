package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка комплектующего изделия.
 * (кронштейны, виброизоляторы, решётки, фильтры и т.д.)
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "accessory_card")
public class AccessoryCardEntity extends BaseProductCard {

    /**
     * Тип комплектующего (кронштейн, виброизолятор, решётка, фильтр)
     */
    @Column(name = "accessory_type", length = 50)
    private String accessoryType;

    /**
     * Совместимые модели вентиляторов (JSON массив или строка через запятую)
     */
    @Column(name = "compatible_models", length = 500)
    private String compatibleModels;

    /**
     * Артикул производителя
     */
    @Column(name = "vendor_code", length = 50)
    private String vendorCode;

    /**
     * Единица измерения (шт, комплект, м)
     */
    @Column(name = "unit", length = 20)
    private String unit;

    /**
     * Цена (опционально, для смет)
     */
    @Column(name = "price")
    private Double price;
}

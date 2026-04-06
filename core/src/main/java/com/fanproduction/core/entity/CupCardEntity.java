package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка стакана (переходного элемента для монтажа вентилятора).
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cup_card")
public class CupCardEntity extends BaseProductCard {

    /**
     * Диаметр стакана (мм)
     */
    @Column(name = "diameter")
    private Integer diameter;

    /**
     * Высота стакана (мм)
     */
    @Column(name = "height")
    private Integer height;

    /**
     * Материал изготовления
     */
    @Column(name = "material", length = 50)
    private String material;

    /**
     * Толщина стенки (мм)
     */
    @Column(name = "thickness")
    private Double thickness;
}

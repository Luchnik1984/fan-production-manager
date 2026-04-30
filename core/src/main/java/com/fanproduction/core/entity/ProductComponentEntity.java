package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Связь между карточкой продукции и компонентом.
 * Определяет, сколько какого компонента требуется для изготовления единицы продукции.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_component")
public class ProductComponentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на карточку продукции (BaseProductCard)
     */
    @Column(name = "product_card_id", nullable = false)
    private Long productCardId;

    /**
     * Ссылка на компонент
     */
    @Column(name = "component_id", nullable = false)
    private Long componentId;

    /**
     * Количество на единицу изделия
     * Может быть дробным (например, 3.5 метра кабеля)
     */
    @Column(name = "quantity")
    private Double quantity = 1.0;

    /**
     * Примечание (например: "с запасом 10%")
     */
    @Column(name = "note", length = 255)
    private String note;
}


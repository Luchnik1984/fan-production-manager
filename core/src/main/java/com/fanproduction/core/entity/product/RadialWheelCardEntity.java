package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "radial_wheel_card")
public class RadialWheelCardEntity extends BaseProductCard {

    // ========== РАЗДЕЛ 1: ОСНОВНАЯ ИНФОРМАЦИЯ ==========

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "series", length = 50)
    private String series;                     // НОВОЕ ПОЛЕ: серия колеса (КЦ, РК)

    @Column(name = "size")
    private Double size;                       // размер колеса (220, 560)

    @Column(name = "marking", length = 100)
    private String marking;                    // маркировка колеса (КЦ-220) - ОБЯЗАТЕЛЬНОЕ

    // ========== РАЗДЕЛ 2: ХАРАКТЕРИСТИКИ КОЛЕСА ==========

    /**
     * Тип лопаток (хранится маркировка):
     * V - впередзагнутые
     * N - назадзагнутые
     * RO - радиальнооканчивающиеся
     */
    @Column(name = "blade_type", length = 30)
    private String bladeType;

    /**
     * Ссылка на компонент "Ступица" из таблицы component
     */
    @Column(name = "hub_component_id")
    private Long hubComponentId;

    @Column(name = "max_speed_rpm")
    private Integer maxSpeedRpm;

    @Column(name = "weight_kg")
    private Double weightKg;

    // ========== РАЗДЕЛ 3: ДОПОЛНИТЕЛЬНЫЕ ПАРАМЕТРЫ ==========

    @Column(name = "wheel_formula", length = 200)
    private String wheelFormula;

    @Column(name = "wheel_code", length = 50)
    private String wheelCode;

    @Column(name = "blade_mod", length = 50)
    private String bladeMod;

    @Column(name = "front_disk_mod", length = 10)
    private String frontDiskMod;

    @Column(name = "wheel_width")
    private Double wheelWidth;

    @Column(name = "blade_length_coeff")
    private Double bladeLengthCoeff;

    @Column(name = "blade_count")
    private Integer bladeCount;

    @Column(name = "diameter")
    private Integer diameter;                  // максимальный диаметр колеса (справочно)

    // ========== РАЗДЕЛ 4: ИСПОЛНЕНИЕ ==========

    @Column(name = "general_purpose")
    private Boolean generalPurpose = true;

    @Column(name = "fireproof")
    private Boolean fireproof = false;

    @Column(name = "fireproof_marking", length = 50)
    private String fireproofMarking;

    @Column(name = "max_temperature")
    private Integer maxTemperature;

    @Column(name = "explosion_proof")
    private Boolean explosionProof = false;

    @Column(name = "explosion_marking", length = 100)
    private String explosionMarking;

    // ========== ПОЛНАЯ МАРКИРОВКА ==========

    @Column(name = "full_marking", length = 500)
    private String fullMarking;
}

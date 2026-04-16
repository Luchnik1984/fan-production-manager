package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "axial_wheel_card")
public class AxialWheelCardEntity extends BaseProductCard {

    // Основная информация
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "marking", length = 100)
    private String marking;

    // Характеристики колеса
    @Column(name = "blade_type", length = 20)
    private String bladeType;           // 4Z, 5Z, 109_50, 76_14

    @Column(name = "size")
    private Double size;                 // типоразмер (6,3)

    @Column(name = "execution", length = 20)
    private String execution;            // C, Ex, F

    @Column(name = "trim_coefficient")
    private Double trimCoefficient;      // коэффициент подрезки (%)

    // Ступица
    @Column(name = "hub_type", length = 50)
    private String hubType;

    // Параметры колеса
    @Column(name = "blade_count")
    private Integer bladeCount;          // количество лопаток

    @Column(name = "blade_slots")
    private Integer bladeSlots;          // посадочных мест

    @Column(name = "blade_shape", length = 50)
    private String bladeShape;           // форма лопатки

    @Column(name = "blade_angle")
    private Integer bladeAngle;          // угол установки

    @Column(name = "blade_material", length = 10)
    private String bladeMaterial;        // материал лопатки (PAG, ST)

    // Расчётные поля
    @Column(name = "wheel_diameter")
    private Integer wheelDiameter;       // диаметр колеса (расчётный)

    @Column(name = "wheel_formula", length = 200)
    private String wheelFormula;         // формула колеса

    // Специальные поля
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

    @Column(name = "full_marking", length = 500)
    private String fullMarking;
}
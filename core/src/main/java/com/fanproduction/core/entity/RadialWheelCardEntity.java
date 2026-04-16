package com.fanproduction.core.entity;

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

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "marking", length = 100)
    private String marking;

    @Column(name = "blade_type", length = 30)
    private String bladeType;

    @Column(name = "blade_mod", length = 50)
    private String bladeMod;

    @Column(name = "size")
    private Double size;

    @Column(name = "wheel_formula", length = 100)
    private String wheelFormula;

    @Column(name = "blade_count")
    private Integer bladeCount;

    @Column(name = "hub_type", length = 50)
    private String hubType;

    @Column(name = "max_speed_rpm")
    private Integer maxSpeedRpm;

    @Column(name = "weight_kg")
    private Double weightKg;

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

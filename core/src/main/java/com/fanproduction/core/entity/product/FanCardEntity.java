package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Абстрактная сущность для общих полей вентиляторов.
 * Используется для осевых, радиальных, канальных и крышных вентиляторов.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fan_card")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FanCardEntity extends BaseProductCard {

    // ========== ОБЩИЕ ПОЛЯ ==========

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Column(name = "series", length = 50)
    private String series;

    @Column(name = "size")
    private Double size;

    @Column(name = "marking", length = 100)
    private String marking;

    @Column(name = "climate_type", length = 10)
    private String climateType;

    @Column(name = "motor_id")
    private Long motorId;

    @Column(name = "motor_wheel_id")
    private Long motorWheelId;

    @Column(name = "radial_wheel_id")
    private Long radialWheelId;

    @Column(name = "axial_wheel_id")
    private Long axialWheelId;

    @Column(name = "hub_type", length = 50)
    private String hubType;

    @Column(name = "wheel_formula", length = 100)
    private String wheelFormula;

    @Column(name = "wheel_diameter")
    private Double wheelDiameter;

    @Column(name = "cable_spec", length = 100)
    private String cableSpec;

    @Column(name = "has_ha")
    private Boolean hasHa = false;

    @Column(name = "has_ca")
    private Boolean hasCa = false;

    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    @Column(name = "fan_class", length = 20)
    private String fanClass;

    @Column(name = "fan_type", length = 30)
    private String fanType;

    @Column(name = "fan_subtype", length = 30)
    private String fanSubtype;

    @Column(name = "is_partner_production")
    private Boolean isPartnerProduction = false;

    @Column(name = "is_own_production")
    private Boolean isOwnProduction = false;

    @Column(name = "max_speed_rpm")
    private Integer maxSpeedRpm;

    // ========== ЭЛЕКТРИЧЕСКИЕ ПАРАМЕТРЫ ==========

    @Column(name = "power_kw")
    private Double powerKw;

    @Column(name = "poles")
    private Integer poles;

    @Column(name = "voltage")
    private Integer voltage;

    @Column(name = "voltage_code", length = 10)
    private String voltageCode;

    @Column(name = "rated_speed_rpm")
    private Integer ratedSpeedRpm;

    @Column(name = "actual_speed_rpm")
    private Integer actualSpeedRpm;

    // ========== ИСПОЛНЕНИЕ ПО НАЗНАЧЕНИЮ ==========

    @Column(name = "general_purpose")
    private Boolean generalPurpose = true;

    @Column(name = "fireproof")
    private Boolean fireproof = false;

    @Column(name = "fireproof_marking", length = 100)
    private String fireproofMarking;

    @Column(name = "max_temperature")
    private Integer maxTemperature;

    @Column(name = "explosion_proof")
    private Boolean explosionProof = false;

    @Column(name = "explosion_marking", length = 100)
    private String explosionMarking;

    // ========== ПОЛНАЯ МАРКИРОВКА ==========

    @Column(name = "full_marking", length = 200, unique = true)
    private String fullMarking;

    // ========== МАРКИРОВКА КОМПОНЕНТОВ ==========

    @Column(name = "motor_wheel_full_marking", length = 200)
    private String motorWheelFullMarking;

    @Column(name = "radial_wheel_full_marking", length = 200)
    private String radialWheelFullMarking;

    @Column(name = "axial_wheel_full_marking", length = 200)
    private String axialWheelFullMarking;

    @Column(name = "motor_full_marking", length = 200)
    private String motorFullMarking;
}
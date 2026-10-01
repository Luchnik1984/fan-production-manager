package com.fanproduction.core.entity.product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка электродвигателя.
 * Содержит все технические характеристики электродвигателя.
 * Пример: "100L2 5,5КВт 3000 об/мин IM1081 У1"
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "motor_card")
public class MotorCardEntity extends BaseProductCard {

    @Column(name = "series", length = 50)
    private String series;

    @Column(name = "motor_type", length = 20)
    private String motorType;

    @Column(name = "poles")
    private Integer poles;

    @Column(name = "power_kw")
    private Double powerKw;

    @Column(name = "rated_speed_rpm")
    private Integer ratedSpeedRpm;

    @Column(name = "actual_speed_rpm")
    private Integer actualSpeedRpm;

    @Column(name = "shaft_size")
    private Integer shaftSize;

    @Column(name = "mounting_type", length = 50)
    private String mountingType;

    @Column(name = "climate_type", length = 10)
    private String climateType;

    @Column(name = "voltage")
    private Integer voltage;

    @Column(name = "operation_mode", length = 10)
    private String operationMode;

    @Column(name = "weight_kg")
    private Double weightKg;


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

    @Column(name = "full_marking", length = 200)
    private String fullMarking;
}


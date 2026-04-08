package com.fanproduction.core.entity;

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

    /**
     * Серия электродвигателя (АИР, 5АИ, ВАО и т.д.)
     */
    @Column(name = "series", length = 50)
    private String series;

    /**
     * Тип электродвигателя (100L, 112M)
     */
    @Column(name = "motor_type", length = 20)
    private String motorType;

    /**
     * Количество полюсов (2, 4, 6, 8)
     */
    @Column(name = "poles")
    private Integer poles;

    /**
     * Мощность (КВт)
     */
    @Column(name = "power_kw")
    private Double powerKw;

    /**
     * Скорость вращения для маркировки (об/мин) = 6000 / poles
     */
    @Column(name = "rated_speed_rpm")
    private Integer ratedSpeedRpm;

    /**
     * Фактическая скорость вращения (об/мин)
     */
    @Column(name = "actual_speed_rpm")
    private Integer actualSpeedRpm;

    /**
     * Размер вала (мм)
     */
    @Column(name = "shaft_size")
    private Integer shaftSize;

    /**
     * Исполнение по способу монтажа (IM1081, IM3081, IM B14)
     */
    @Column(name = "mounting_type", length = 50)
    private String mountingType;

    /**
     * Климатическое исполнение (У1, УХЛ, У2)
     */
    @Column(name = "climate_type", length = 10)
    private String climateType;

    /**
     * Рабочее напряжение (220, 380)
     */
    @Column(name = "voltage")
    private Integer voltage;

    /**
     * Режим работы (S1, S4)
     */
    @Column(name = "operation_mode", length = 10)
    private String operationMode;

    /**
     * Масса (кг)
     */
    @Column(name = "weight_kg")
    private Double weightKg;

    /**
     * Общего применения
     */
    @Column(name = "general_purpose")
    private Boolean generalPurpose = true;

    /**
     * Огнестойкий
     */
    @Column(name = "fireproof")
    private Boolean fireproof = false;

    /**
     * Предельная температура (°C)
     */
    @Column(name = "max_temperature")
    private Integer maxTemperature;

    /**
     * Взрывозащищённый
     */
    @Column(name = "explosion_proof")
    private Boolean explosionProof = false;

    /**
     * Маркировка взрывозащиты
     */
    @Column(name = "explosion_marking", length = 100)
    private String explosionMarking;

    /**
     * Полная маркировка (формируется автоматически)
     */
    @Column(name = "full_marking", length = 200)
    private String fullMarking;
}


package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Абстрактная карточка вентилятора.
 * Содержит общие для всех типов вентиляторов поля.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "fan_card")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class FanCardEntity extends BaseProductCard {

    /**
     * Типоразмер вентилятора (например: 5,6)
     */
    @Column(name = "size")
    private Double size;

    /**
     * Исполнение (C, Ex, F-2/600 и т.д.)
     */
    @Column(name = "execution", length = 50)
    private String execution;

    /**
     * Коэффициент подрезки для исполнения
     */
    @Column(name = "trim_coefficient")
    private Double trimCoefficient;

    /**
     * Климатическое исполнение (У1, У2, УХЛ)
     */
    @Column(name = "climate_type", length = 10)
    private String climateType;

    /**
     * Ссылка на электродвигатель (ID из motor_card)
     */
    @Column(name = "motor_id")
    private Long motorId;

    /**
     * Установленная ступица (SM 1610, BF 2012)
     */
    @Column(name = "hub_type", length = 50)
    private String hubType;

    /**
     * Формула колеса
     */
    @Column(name = "wheel_formula", length = 100)
    private String wheelFormula;

    /**
     * Диаметр колеса (мм)
     */
    @Column(name = "wheel_diameter")
    private Double wheelDiameter;

    /**
     * Кабель подключения (например: "2,5х4 длина 500мм")
     */
    @Column(name = "cable_spec", length = 100)
    private String cableSpec;

    /**
     * Наличие направляющего аппарата (HA)
     */
    @Column(name = "has_ha")
    private Boolean hasHa = false;

    /**
     * Наличие спрямляющего аппарата (CA)
     */
    @Column(name = "has_ca")
    private Boolean hasCa = false;

    /**
     * Номер декларации или сертификата соответствия
     */
    @Column(name = "certificate_number", length = 100)
    private String certificateNumber;

    /**
     * Класс вентилятора (ОБЩЕОБМЕННЫЙ, ДЫМОУДАЛЕНИЕ)
     */
    @Column(name = "fan_class", length = 20)
    private String fanClass;

    /**
     * Тип вентилятора (осевой, радиальный, канальный)
     */
    @Column(name = "fan_type", length = 30)
    private String fanType;

    /**
     * Подтип вентилятора (крышный, улитка, пристенный, свободное колесо)
     */
    @Column(name = "fan_subtype", length = 30)
    private String fanSubtype;
}

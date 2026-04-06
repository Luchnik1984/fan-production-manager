package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка радиального колеса.
 * Используется в радиальных и канальных вентиляторах (назад-загнутые лопатки).
 * Пример: КЦ-220 C1
 * Позволяет переиспользовать одно и то же колесо в разных вентиляторах
 * с пересчётом аэродинамических характеристик на разные скорости вращения.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "radial_wheel_card")
public class RadialWheelCardEntity extends BaseProductCard {

    /**
     * Производитель
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    /**
     * Маркировка колеса (например: КЦ-220 C1)
     */
    @Column(name = "marking", length = 100)
    private String marking;

    /**
     * Тип лопаток (впередзагнутые, назадзагнутые)
     */
    @Column(name = "blade_type", length = 30)
    private String bladeType;

    /**
     * Размер колеса (например: 5,6)
     */
    @Column(name = "size")
    private Double size;

    /**
     * Установленная ступица (SM 1610, BF 2012)
     */
    @Column(name = "hub_type", length = 50)
    private String hubType;

    /**
     * Максимальная скорость вращения (об/мин)
     */
    @Column(name = "max_speed_rpm")
    private Integer maxSpeedRpm;

    /**
     * Масса (кг)
     */
    @Column(name = "weight_kg")
    private Double weightKg;

    /**
     * Формула колеса (например: 5,6_B14) — опционально
     */
    @Column(name = "wheel_formula", length = 100)
    private String wheelFormula;

    /**
     * Модификация переднего диска (A, B) — опционально
     */
    @Column(name = "front_disk_mod", length = 10)
    private String frontDiskMod;

    /**
     * Модификация лопатки (14, 12U, 1.03, a1.01) — опционально
     */
    @Column(name = "blade_mod", length = 50)
    private String bladeMod;

    /**
     * Количество лопаток (6, 7, 9) — опционально
     */
    @Column(name = "blade_count")
    private Integer bladeCount;

    /**
     * Диаметр колеса (мм) — опционально
     */
    @Column(name = "diameter")
    private Integer diameter;

    /**
     * Ширина колеса (мм) — опционально
     */
    @Column(name = "width")
    private Integer width;
}

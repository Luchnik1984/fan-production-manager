package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка канального вентилятора.
 * Типы:
 * - Тип A: с мотор-колесом (вперед-загнутые лопатки)
 *   Пример: VRK-PatAIR-P-40-20-4-220
 * - Тип B: с электродвигателем + радиальное колесо (назад-загнутые лопатки)
 *   Пример: VRK-PatAIR-PKV-60-30/25-2D
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "duct_fan_card")
public class DuctFanCardEntity extends FanCardEntity {

    /**
     * Наименование серии
     */
    @Column(name = "series_name", length = 100)
    private String seriesName;

    /**
     * Исполнение (P, PS, PKV, PRV, KpM, KpMS)
     */
    @Column(name = "execution_type", length = 20)
    private String executionType;

    /**
     * Тип вентилятора (MOTOR_WHEEL - с мотор-колесом, RADIAL_WHEEL - с радиальным колесом)
     */
    @Column(name = "duct_fan_type", length = 20)
    private String ductFanType;

    /**
     * Ссылка на мотор-колесо (если тип MOTOR_WHEEL)
     */
    @Column(name = "motor_wheel_id")
    private Long motorWheelId;

    /**
     * Ссылка на радиальное колесо (если тип RADIAL_WHEEL)
     */
    @Column(name = "radial_wheel_id")
    private Long radialWheelId;

    /**
     * Размер установленного колеса (для типа B)
     */
    @Column(name = "wheel_size")
    private Double wheelSize;

    /**
     * Полюсность электродвигателя (2D, 4D и т.д.)
     */
    @Column(name = "motor_poles_code", length = 10)
    private String motorPolesCode;

    /**
     * Тип корпуса (круглый, квадратный)
     */
    @Column(name = "housing_type", length = 20)
    private String housingType;

    /**
     * Уровень шума (dB)
     */
    @Column(name = "noise_level")
    private Integer noiseLevel;

    /**
     * Ссылка на стакан (для крышных исполнений)
     */
    @Column(name = "cup_id")
    private Long cupId;
}

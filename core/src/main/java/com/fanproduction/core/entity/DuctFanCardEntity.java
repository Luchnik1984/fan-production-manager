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
 * - Тип B: с электродвигателем + колесо радиальное (назад-загнутые лопатки)
 *   Пример: VRK-PatAIR-PKV-60-30/25-2D
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "duct_fan_card")
public class DuctFanCardEntity extends FanCardEntity {

    @Column(name = "series_name", length = 100)
    private String seriesName;

    @Column(name = "duct_size", length = 50)           // ← переименовали с size на duct_size
    private String ductSize;                           // типоразмер (40-20, 50-30)

    @Column(name = "duct_fan_type", length = 20)
    private String ductFanType;

    @Column(name = "execution_type", length = 20)
    private String executionType;

    @Column(name = "motor_wheel_id")
    private Long motorWheelId;

    @Column(name = "radial_wheel_id")
    private Long radialWheelId;

    @Column(name = "motor_id")
    private Long motorId;

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

    @Column(name = "wheel_size")
    private Integer wheelSize;

    @Column(name = "full_marking", length = 200)
    private String fullMarking;
}

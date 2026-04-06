package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Аэродинамическая характеристика вентилятора.
 * Связь: один вентилятор (FanCardEntity) — много точек характеристики.
 *
 * Позволяет хранить несколько точек (расход, давление, мощность)
 * для построения графика характеристики.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "aerodynamic_data")
public class AerodynamicDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Ссылка на карточку вентилятора (FanCardEntity)
     */
    @Column(name = "fan_card_id", nullable = false)
    private Long fanCardId;

    /**
     * Расход (Q, м³/ч)
     */
    @Column(name = "airflow")
    private Double airflow;

    /**
     * Статическое давление (Psv, Па)
     */
    @Column(name = "static_pressure")
    private Double staticPressure;

    /**
     * Полное давление (Pv, Па)
     */
    @Column(name = "total_pressure")
    private Double totalPressure;

    /**
     * Потребляемая мощность (N, КВт)
     */
    @Column(name = "power_kw")
    private Double powerKw;

    /**
     * Скорость вращения, для которой рассчитана характеристика (об/мин)
     * Если NULL — характеристика при номинальной скорости
     */
    @Column(name = "speed_rpm")
    private Integer speedRpm;

    /**
     * Примечание (например: "рассчёт на номинальную скорость вращения")
     */
    @Column(name = "note", length = 255)
    private String note;
}

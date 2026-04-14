package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка осевого вентилятора.
 * Пример: VO-PatAIR-5.6-C-3/9-5.5-2-У1
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "axial_fan_card")
public class AxialFanCardEntity extends FanCardEntity {

    /**
     * Наименование серии (например: VO-PatAIR)
     */
    @Column(name = "series_name", length = 100)
    private String seriesName;

    /**
     * Положение вентилятора (Г - горизонтальное, В - вертикальное, С - смешанное)
     */
    @Column(name = "position", length = 10)
    private String position;

    /**
     * Ссылка на компонент - Колесо осевое
     */
    @Column(name = "axial_wheel_id")
    private Long axialWheelId;

    /**
     * Ссылка на компонент - электродвигатель
     */
    @Column(name = "motor_id")
    private Long motorId;
}
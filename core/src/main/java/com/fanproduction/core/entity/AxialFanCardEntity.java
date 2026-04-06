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
     * Количество устанавливаемых лопаток (например: 9)
     */
    @Column(name = "blade_count")
    private Integer bladeCount;

    /**
     * Размер колеса по количеству посадочных мест под лопатки (например: 9)
     */
    @Column(name = "blade_slots")
    private Integer bladeSlots;

    /**
     * Форма лопатки (4Z, 5Z, 109_50, 76_14)
     */
    @Column(name = "blade_shape", length = 50)
    private String bladeShape;

    /**
     * Угол установки лопаток (градусы, например: 27)
     */
    @Column(name = "blade_angle")
    private Integer bladeAngle;
}
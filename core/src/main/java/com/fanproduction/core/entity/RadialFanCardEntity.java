package com.fanproduction.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Карточка радиального вентилятора.
 * Примеры:
 * - VR-PatAIR-CALDERA-5.6-F-2/600-B14-4-4-У1
 * - VR-PatAIR-Vn-5.6-F-2/600-A1.03-4-4-У1-90-п
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "radial_fan_card")
public class RadialFanCardEntity extends FanCardEntity {

    /**
     * Наименование серии (VR-PatAIR-CALDERA, VR-PatAIR-Vn)
     */
    @Column(name = "series_name", length = 100)
    private String seriesName;

    /**
     * Модификация переднего диска (A, B)
     */
    @Column(name = "front_disk_mod", length = 10)
    private String frontDiskMod;

    /**
     * Модификация лопатки (14, 12U, 1.03, a1.01)
     */
    @Column(name = "blade_mod", length = 50)
    private String bladeMod;

    /**
     * Количество лопаток (6, 7, 9)
     */
    @Column(name = "blade_count")
    private Integer bladeCount;

    /**
     * Угол установки корпуса (градусы, например: 90)
     */
    @Column(name = "housing_angle")
    private Integer housingAngle;

    /**
     * Направление вращения колеса (п - правое, л - левое)
     */
    @Column(name = "rotation_direction", length = 10)
    private String rotationDirection;
}

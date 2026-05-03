package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "radial_fan_card")
public class RadialFanCardEntity extends FanCardEntity {

    @Column(name = "series_name", length = 100)
    private String seriesName;

    @Column(name = "radial_wheel_id")
    private Long radialWheelId;

    @Column(name = "housing_angle")
    private Integer housingAngle;

    @Column(name = "rotation_direction", length = 10)
    private String rotationDirection;
}


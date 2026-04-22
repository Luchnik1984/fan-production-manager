package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roof_radial_fan")
public class RoofRadialFanCardEntity extends FanCardEntity {

    @Column(name = "series_name", length = 100)
    private String seriesName = "VR-PatAIR";

    @Column(name = "execution_type", length = 20)
    private String executionType;

    @Column(name = "roof_size", length = 50)
    private String roofSize;

    @Column(name = "climate_type", length = 10)
    private String climateType = "У1";

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

    @Column(name = "full_marking", length = 200)
    private String fullMarking;
}

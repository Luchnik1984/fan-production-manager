package com.fanproduction.core.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roof_low_profile_fan")
public class RoofLowProfileFanCardEntity extends FanCardEntity {

    @Column(name = "series_name", length = 100)
    private String seriesName = "VR-PatAIR";

    @Column(name = "execution_type", length = 20)
    private String executionType;

    @Column(name = "roof_size", length = 50)
    private String roofSize;

    @Column(name = "climate_type", length = 10)
    private String climateType = "У1";

    @Column(name = "motor_wheel_id")
    private Long motorWheelId;

    @Column(name = "poles")
    private Integer poles;

    @Column(name = "voltage")
    private Integer voltage;

    @Column(name = "full_marking", length = 200)
    private String fullMarking;
}

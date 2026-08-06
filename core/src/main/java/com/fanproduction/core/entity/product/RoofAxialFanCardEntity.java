package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "roof_axial_fan")
public class RoofAxialFanCardEntity extends FanCardEntity {

    @Column(name = "series_name", length = 100)
    private String seriesName = "VO-PatAIR";

    @Column(name = "execution_type", length = 20)
    private String executionType;

    @Column(name = "roof_size", length = 50)
    private String roofSize;

    @Column(name = "climate_type", length = 10)
    private String climateType = "У1";

    @Column(name = "axial_wheel_full_marking", length = 200)
    private String axialWheelFullMarking;
}

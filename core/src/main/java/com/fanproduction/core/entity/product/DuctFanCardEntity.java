package com.fanproduction.core.entity.product;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "duct_fan_card")
public class DuctFanCardEntity extends FanCardEntity {

    @Column(name = "series_name", length = 100)
    private String seriesName = "VRK-PatAIR";

    @Column(name = "duct_size", length = 50)
    private String ductSize;

    @Column(name = "series", length = 20)
    private String series;

    @Column(name = "duct_fan_type", length = 20)
    private String ductFanType;

    @Column(name = "hub_component_id")
    private Long hubComponentId;

    @Column(name = "wheel_size")
    private Integer wheelSize;
}
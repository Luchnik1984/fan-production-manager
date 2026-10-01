package com.fanproduction.gui.dto.response;

import lombok.Data;

@Data
public class RadialWheelDto {
    private Long id;
    private String name;
    private String manufacturer;
    private String marking;
    private String bladeType;
    private Double size;
    private String hubType;
    private Integer maxSpeedRpm;
    private Double weightKg;
    private String fullMarking;
    private String bladeMod;

    @Override
    public String toString() {
        return fullMarking != null ? fullMarking : name;
    }
}
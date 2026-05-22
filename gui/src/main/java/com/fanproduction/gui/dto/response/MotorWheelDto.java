package com.fanproduction.gui.dto.response;

import lombok.Data;

@Data
public class MotorWheelDto {
    private Long id;
    private String name;
    private String manufacturer;
    private String bladeType;
    private Integer size;
    private Integer poles;
    private String voltageCode;
    private Double powerKw;
    private Integer ratedSpeedRpm;
    private Integer actualSpeedRpm;
    private Integer voltage;
    private Double weightKg;
    private String fullMarking;
    private String motorCode;

    @Override
    public String toString() {
        return fullMarking != null ? fullMarking : name;
    }
}

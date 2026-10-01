package com.fanproduction.gui.dto.response;

import lombok.Data;

@Data
public class MotorDto {
    private Long id;
    private String name;
    private String series;
    private String motorType;
    private Integer poles;
    private Double powerKw;
    private Integer ratedSpeedRpm;
    private Integer actualSpeedRpm;
    private Integer shaftSize;
    private String mountingType;
    private String climateType;
    private Integer voltage;
    private String operationMode;
    private Double weightKg;
    private String fullMarking;

    @Override
    public String toString() {
        return fullMarking != null ? fullMarking : name;
    }
}

package com.fanproduction.gui.dto.response;

import lombok.Data;

@Data
public class AxialWheelDto {
    private Long id;
    private String name;
    private String manufacturer;
    private String marking;
    private String bladeType;
    private Double size;
    private String execution;
    private Double trimCoefficient;
    private String hubType;
    private Integer bladeCount;
    private Integer bladeSlots;
    private String bladeShape;
    private Integer bladeAngle;
    private String bladeMaterial;
    private Integer wheelDiameter;
    private String wheelFormula;
    private Boolean generalPurpose;
    private Boolean fireproof;
    private String fireproofMarking;
    private Integer maxTemperature;
    private Boolean explosionProof;
    private String explosionMarking;
    private String fullMarking;
    private String code;

    @Override
    public String toString() {
        return fullMarking != null ? fullMarking : name;
    }
}

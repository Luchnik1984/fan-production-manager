package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.AxialWheelDto;
import com.fanproduction.gui.util.CardFieldExtractor;

import java.util.List;
import java.util.Map;

public class AxialWheelClient extends BaseCardClient<AxialWheelDto> {

    private static final AxialWheelClient INSTANCE = new AxialWheelClient();

    public static List<AxialWheelDto> getAll() throws Exception {
        return INSTANCE.fetchAll();
    }

    public static AxialWheelDto getById(Long id) throws Exception {
        return INSTANCE.fetchById(id);
    }

    @Override
    protected String getCardType() {
        return "AXIAL_WHEEL";
    }

    @Override
    protected AxialWheelDto createDto() {
        return new AxialWheelDto();
    }

    @Override
    protected void populateFromFields(AxialWheelDto dto, Map<String, Object> f) {
        dto.setManufacturer(CardFieldExtractor.getString(f, "manufacturer"));
        dto.setMarking(CardFieldExtractor.getString(f, "marking"));
        dto.setBladeType(CardFieldExtractor.getString(f, "bladeType"));
        dto.setSize(CardFieldExtractor.getDouble(f, "size"));
        dto.setExecution(CardFieldExtractor.getString(f, "execution"));
        dto.setTrimCoefficient(CardFieldExtractor.getDouble(f, "trimCoefficient"));
        dto.setHubType(CardFieldExtractor.getString(f, "hubType"));
        dto.setBladeCount(CardFieldExtractor.getInteger(f, "bladeCount"));
        dto.setBladeSlots(CardFieldExtractor.getInteger(f, "bladeSlots"));
        dto.setBladeShape(CardFieldExtractor.getString(f, "bladeShape"));
        dto.setBladeAngle(CardFieldExtractor.getInteger(f, "bladeAngle"));
        dto.setBladeMaterial(CardFieldExtractor.getString(f, "bladeMaterial"));
        dto.setWheelDiameter(CardFieldExtractor.getInteger(f, "wheelDiameter"));
        dto.setWheelFormula(CardFieldExtractor.getString(f, "wheelFormula"));
        dto.setGeneralPurpose(CardFieldExtractor.getBoolean(f, "generalPurpose"));
        dto.setFireproof(CardFieldExtractor.getBoolean(f, "fireproof"));
        dto.setFireproofMarking(CardFieldExtractor.getString(f, "fireproofMarking"));
        dto.setMaxTemperature(CardFieldExtractor.getInteger(f, "maxTemperature"));
        dto.setExplosionProof(CardFieldExtractor.getBoolean(f, "explosionProof"));
        dto.setExplosionMarking(CardFieldExtractor.getString(f, "explosionMarking"));
        dto.setFullMarking(CardFieldExtractor.getString(f, "fullMarking"));
    }
}

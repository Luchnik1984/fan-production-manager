package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.RadialWheelDto;
import com.fanproduction.gui.util.CardFieldExtractor;

import java.util.List;
import java.util.Map;

public class RadialWheelClient extends BaseCardClient<RadialWheelDto> {

    private static final RadialWheelClient INSTANCE = new RadialWheelClient();

    public static List<RadialWheelDto> getAll() throws Exception {
        return INSTANCE.fetchAll();
    }

    public static RadialWheelDto getById(Long id) throws Exception {
        return INSTANCE.fetchById(id);
    }

    @Override
    protected String getCardType() {
        return "RADIAL_WHEEL";
    }

    @Override
    protected RadialWheelDto createDto() {
        return new RadialWheelDto();
    }

    @Override
    protected void populateFromFields(RadialWheelDto dto, Map<String, Object> f) {
        dto.setManufacturer(CardFieldExtractor.getString(f, "manufacturer"));
        dto.setMarking(CardFieldExtractor.getString(f, "marking"));
        dto.setBladeType(CardFieldExtractor.getString(f, "bladeType"));
        dto.setSize(CardFieldExtractor.getDouble(f, "size"));
        dto.setHubType(CardFieldExtractor.getString(f, "hubType"));
        dto.setMaxSpeedRpm(CardFieldExtractor.getInteger(f, "maxSpeedRpm"));
        dto.setWeightKg(CardFieldExtractor.getDouble(f, "weightKg"));
        dto.setFullMarking(CardFieldExtractor.getString(f, "fullMarking"));
        dto.setBladeMod(CardFieldExtractor.getString(f, "bladeMod"));
    }
}
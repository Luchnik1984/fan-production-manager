package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.MotorDto;
import com.fanproduction.gui.util.CardFieldExtractor;

import java.util.List;
import java.util.Map;

public class MotorClient extends BaseCardClient<MotorDto> {

    private static final MotorClient INSTANCE = new MotorClient();

    // ==========================================================
    // STATIC API (для обратной совместимости)
    // ==========================================================

    public static List<MotorDto> getAll() throws Exception {
        return INSTANCE.fetchAll();
    }

    public static MotorDto getById(Long id) throws Exception {
        return INSTANCE.fetchById(id);
    }

    // ==========================================================
    // BaseCardClient
    // ==========================================================

    @Override
    protected String getCardType() {
        return "MOTOR";
    }

    @Override
    protected MotorDto createDto() {
        return new MotorDto();
    }

    @Override
    protected void populateFromFields(MotorDto dto, Map<String, Object> f) {
        dto.setSeries(CardFieldExtractor.getString(f, "series"));
        dto.setMotorType(CardFieldExtractor.getString(f, "motorType"));
        dto.setPoles(CardFieldExtractor.getInteger(f, "poles"));
        dto.setPowerKw(CardFieldExtractor.getDouble(f, "powerKw"));
        dto.setRatedSpeedRpm(CardFieldExtractor.getInteger(f, "ratedSpeedRpm"));
        dto.setActualSpeedRpm(CardFieldExtractor.getInteger(f, "actualSpeedRpm"));
        dto.setShaftSize(CardFieldExtractor.getInteger(f, "shaftSize"));
        dto.setMountingType(CardFieldExtractor.getString(f, "mountingType"));
        dto.setClimateType(CardFieldExtractor.getString(f, "climateType"));
        dto.setVoltage(CardFieldExtractor.getInteger(f, "voltage"));
        dto.setOperationMode(CardFieldExtractor.getString(f, "operationMode"));
        dto.setWeightKg(CardFieldExtractor.getDouble(f, "weightKg"));
        dto.setFullMarking(CardFieldExtractor.getString(f, "fullMarking"));
    }
}

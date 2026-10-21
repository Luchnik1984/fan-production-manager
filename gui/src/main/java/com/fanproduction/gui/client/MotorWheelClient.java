package com.fanproduction.gui.client;

import com.fanproduction.gui.dto.response.MotorWheelDto;
import com.fanproduction.gui.util.CardFieldExtractor;

import java.util.List;
import java.util.Map;

public class MotorWheelClient extends BaseCardClient<MotorWheelDto> {

    private static final MotorWheelClient INSTANCE = new MotorWheelClient();

    public static List<MotorWheelDto> getAll() throws Exception {
        return INSTANCE.fetchAll();
    }

    public static MotorWheelDto getById(Long id) throws Exception {
        return INSTANCE.fetchById(id);
    }

    @Override
    protected String getCardType() {
        return "MOTOR_WHEEL";
    }

    @Override
    protected MotorWheelDto createDto() {
        return new MotorWheelDto();
    }

    @Override
    protected void populateFromFields(MotorWheelDto dto, Map<String, Object> f) {
        dto.setManufacturer(CardFieldExtractor.getString(f, "manufacturer"));
        dto.setBladeType(CardFieldExtractor.getString(f, "bladeType"));
        dto.setSize(CardFieldExtractor.getInteger(f, "size"));
        dto.setPoles(CardFieldExtractor.getInteger(f, "poles"));
        dto.setVoltageCode(CardFieldExtractor.getString(f, "voltageCode"));
        dto.setPowerKw(CardFieldExtractor.getDouble(f, "powerKw"));
        dto.setRatedSpeedRpm(CardFieldExtractor.getInteger(f, "ratedSpeedRpm"));
        dto.setActualSpeedRpm(CardFieldExtractor.getInteger(f, "actualSpeedRpm"));
        dto.setVoltage(CardFieldExtractor.getInteger(f, "voltage"));
        dto.setWeightKg(CardFieldExtractor.getDouble(f, "weightKg"));
        dto.setFullMarking(CardFieldExtractor.getString(f, "fullMarking"));
        dto.setMotorCode(CardFieldExtractor.getString(f, "motorCode"));
    }
}

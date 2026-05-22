package com.fanproduction.gui.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fanproduction.gui.dto.response.ApiResponse;
import com.fanproduction.gui.dto.response.MotorWheelDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MotorWheelClient {
    @SuppressWarnings("unchecked")

    public static List<MotorWheelDto> getAll() throws Exception {
        TypeReference<ApiResponse<Map<String, Object>>> typeRef = new TypeReference<>() {};
        ApiResponse<Map<String, Object>> response = ApiClient.get("/products?cardType=MOTOR_WHEEL&size=1000", typeRef);

        List<MotorWheelDto> result = new ArrayList<>();
        if (response.isSuccess() && response.getData() != null) {
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getData().get("content");
            for (Map<String, Object> item : content) {
                MotorWheelDto dto = new MotorWheelDto();
                dto.setId(((Number) item.get("id")).longValue());

                Map<String, Object> fields = (Map<String, Object>) item.get("fields");
                dto.setManufacturer((String) fields.get("manufacturer"));
                dto.setBladeType((String) fields.get("bladeType"));
                dto.setSize(fields.get("size") != null ? ((Number) fields.get("size")).intValue() : null);
                dto.setPoles(fields.get("poles") != null ? ((Number) fields.get("poles")).intValue() : null);
                dto.setVoltageCode((String) fields.get("voltageCode"));
                dto.setPowerKw(fields.get("powerKw") != null ? ((Number) fields.get("powerKw")).doubleValue() : null);
                dto.setRatedSpeedRpm(fields.get("ratedSpeedRpm") != null ? ((Number) fields.get("ratedSpeedRpm")).intValue() : null);
                dto.setActualSpeedRpm(fields.get("actualSpeedRpm") != null ? ((Number) fields.get("actualSpeedRpm")).intValue() : null);
                dto.setVoltage(fields.get("voltage") != null ? ((Number) fields.get("voltage")).intValue() : null);
                dto.setWeightKg(fields.get("weightKg") != null ? ((Number) fields.get("weightKg")).doubleValue() : null);
                dto.setFullMarking((String) fields.get("fullMarking"));
                dto.setMotorCode((String) fields.get("motorCode"));

                result.add(dto);
            }
        }
        return result;
    }
}
